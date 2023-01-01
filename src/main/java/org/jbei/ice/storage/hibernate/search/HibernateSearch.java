package org.jbei.ice.storage.hibernate.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.Session;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.PhrasePredicateFieldStep;
import org.hibernate.search.engine.search.sort.SearchSort;
import org.hibernate.search.engine.search.sort.dsl.ScoreSortOptionsStep;
import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.entry.Visibility;
import org.jbei.ice.dto.search.FieldFilter;
import org.jbei.ice.dto.search.SearchQuery;
import org.jbei.ice.dto.search.SearchResult;
import org.jbei.ice.dto.search.SearchResults;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.search.QueryType;
import org.jbei.ice.search.filter.SearchFieldFactory;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.Entry;

import java.util.*;

/**
 * Apache Lucene full text library functionality in Hibernate.
 * Implemented as a singleton
 *
 * @author Hector Plahar, Elena Aravina
 */
public class HibernateSearch {

    private HibernateSearch() {
    }

    /**
     * Retrieve the singleton instance of this class.
     *
     * @return HibernateSearch instance.
     */
    public static HibernateSearch getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static String cleanQuery(String query) {
        if (query == null)
            return null;
        String cleanedQuery = query;
        cleanedQuery = cleanedQuery.replace(":", " ");
        cleanedQuery = cleanedQuery.replace(";", " ");
        cleanedQuery = cleanedQuery.replace("\\", " ");
        cleanedQuery = cleanedQuery.replace("/", " ");
        cleanedQuery = cleanedQuery.replace("!", " ");
        cleanedQuery = cleanedQuery.replace("[", "\\[");
        cleanedQuery = cleanedQuery.replace("]", "\\]");
        cleanedQuery = cleanedQuery.replace("{", "\\{");
        cleanedQuery = cleanedQuery.replace("}", "\\}");
        cleanedQuery = cleanedQuery.replace("(", "\\(");
        cleanedQuery = cleanedQuery.replace(")", "\\)");
        cleanedQuery = cleanedQuery.replace("+", "\\+");
        cleanedQuery = cleanedQuery.replace("-", "\\-");
        cleanedQuery = cleanedQuery.replace("'", "\\'");
//        cleanedQuery = cleanedQuery.replace("\"", "\\\"");
        cleanedQuery = cleanedQuery.replace("^", "\\^");
        cleanedQuery = cleanedQuery.replace("&", "\\&");

        cleanedQuery = cleanedQuery.endsWith("'") ? cleanedQuery.substring(0, cleanedQuery.length() - 1) : cleanedQuery;
        cleanedQuery = (cleanedQuery.endsWith("\\") ? cleanedQuery.substring(0,
            cleanedQuery.length() - 1) : cleanedQuery);
        return cleanedQuery;
    }

    public SearchResults executeSearchNoTerms(String userId, HashMap<String, SearchResult> blastResults,
                                              SearchQuery searchQuery) {
        ArrayList<EntryType> entryTypes = searchQuery.getEntryTypes();
        if (entryTypes == null || entryTypes.isEmpty()) {
            entryTypes = new ArrayList<>(Arrays.asList(EntryType.values()));
        }

        Session session = HibernateConfiguration.getCurrentSession();
        SearchSession searchSession = Search.session(session);
        SearchScope<Entry> scope = searchSession.scope(Entry.class);

        // phrase query
        PhrasePredicateFieldStep<?> step = scope.predicate().phrase();
        BooleanPredicateClausesStep<?> boolStep = scope.predicate().bool();

        step.fields("a", "b"); // tODO


//        List<Entry> entries = searchSession.search(scope)
//            .where(scope.predicate().match());
//            .field("field").matching("search").toPredicate())
//            .fetchHits(20);


        // type for matches should not include those that are not specified
        for (EntryType type : EntryType.values()) {
            if (entryTypes.contains(type))
                continue;
            boolStep.mustNot(scope.predicate().match().field("type").matching(type.getName()));
        }

        // visibility for matches must be "OK"
        boolStep.must(scope.predicate().match().field("visibility").matching(Visibility.OK.getValue()));

        // must match bioSafety level if specified
        BioSafetyOption option = searchQuery.getBioSafetyOption();
        if (option != null) {
            boolStep.must(scope.predicate().match().field("bioSafetyLevel").matching(option.getIntValue()));
        }

        // check filter filters
        if (searchQuery.getFieldFilters() != null && !searchQuery.getFieldFilters().isEmpty()) {
            for (FieldFilter fieldFilter : searchQuery.getFieldFilters()) {
                String searchField = SearchFieldFactory.searchFieldForEntryField(fieldFilter.getField());
                if (StringUtils.isEmpty(searchField))
                    continue;

                boolStep.must(scope.predicate().match().field(searchField).matching(fieldFilter.getFilter()));
            }
        }

        // check if there is a blast results
        createBlastFilterQuery(blastResults, scope, boolStep);

        // get sorting values
        SearchSort searchSort = getSort(scope, searchQuery.getParameters().isSortAscending(), searchQuery.getParameters().getSortField());
        org.hibernate.search.engine.search.query.SearchResult<Entry> queryResults = searchSession.search(Entry.class)
            .where(boolStep.toPredicate())
            .sort(searchSort)
            .fetch(searchQuery.getParameters().getStart(), searchQuery.getParameters().getRetrieveCount());


//        fullTextQuery.setProjection(ProjectionConstants.ID, "owner");

        // enable security filter if needed
//        checkEnableSecurityFilter(userId, fullTextQuery);

        // enable has attachment/sequence/sample (if needed)
//        checkEnableHasAttribute(fullTextQuery, searchQuery.getParameters());

        LinkedList<SearchResult> searchResults = new LinkedList<>();

        for (Entry entry : queryResults.hits()) {
            SearchResult searchResult = new SearchResult();
            PartData info = entry.toDataTransferObject();
            searchResult.setEntryInfo(info);

            searchResult.setMaxScore(1f);
            searchResults.add(searchResult);
        }

        SearchResults results = new SearchResults();
        long resultCount = queryResults.total().hitCount();
        results.setResultCount(resultCount);
        results.setResults(searchResults);

        Logger.info(userId + ": obtained " + resultCount + " results for empty query");
        return results;
    }

    /**
     * Intended to be called after running a blast search to filter the results. Blast search runs
     * on a fasta file which contains all the sequences. Hibernate search has a security filter for permissions
     * and is therefore used to filter out the blast results, as well as to filter based on the entry
     * attributes not handled by blast; such as "has sequence" and "bio-safety level"
     *
     * @param userId       identifier for account of user performing search
     * @param start        paging start
     * @param count        maximum number of results to return
     * @param blastResults raw results of the blast search
     * @return wrapper around list of filtered results
     */
    public SearchResults filterBlastResults(String userId, int start, int count, SearchQuery searchQuery,
                                            final HashMap<String, SearchResult> blastResults) {
        Session session = HibernateConfiguration.getCurrentSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);

        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        Query query = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();

        // todo : there is a limit of 1024 boolean clauses so return only return top blast results
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(query, BooleanClause.Occur.FILTER);

        // wrap Lucene query in a org.hibernate.Query
        Class<?>[] classes = SearchFieldFactory.classesForTypes(searchQuery.getEntryTypes());
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(builder.build(), classes);

        // enable security filter if an admin
        checkEnableSecurityFilter(userId, fullTextQuery);

        // enable has attachment/sequence/sample (if needed)
        checkEnableHasAttribute(fullTextQuery, searchQuery.getParameters());

        // bio-safety level
        if (searchQuery.getBioSafetyOption() != null) {
            TermContext levelContext = qb.keyword();
            Query biosafetyQuery = levelContext.onField("bioSafetyLevel").ignoreFieldBridge()
                .matching(searchQuery.getBioSafetyOption().getValue()).createQuery();
            builder.add(biosafetyQuery, BooleanClause.Occur.MUST);
        }

        // execute search
        fullTextQuery.setProjection("id");

        // list contains an object array with one Long object
        List<?> luceneResult = fullTextQuery.list();
        HashSet<String> resultSet = new HashSet<>();

        // page
        for (Object object : luceneResult) {
            Long result = (Long) ((Object[]) object)[0];
            resultSet.add(result.toString());
        }

        blastResults.keySet().removeIf(key -> !resultSet.contains(key));

        SearchResult[] searchResults = new SearchResult[count];
        int limit = Math.min((start + count), blastResults.size());
        LinkedList<SearchResult> list = new LinkedList<>(Arrays.asList(blastResults.values().toArray(searchResults))
            .subList(start, limit));

        SearchResults results = new SearchResults();
        results.setResultCount(blastResults.size());
        results.setResults(list);
        return results;
    }

    public SearchResults executeSearch(String userId, HashMap<String, QueryType> terms,
                                       SearchQuery searchQuery, HashMap<String, SearchResult> blastResults) {
        Session session = HibernateConfiguration.getCurrentSession();

        // get class types or search
        HashSet<String> fields = new HashSet<>(SearchFieldFactory.entryFields(searchQuery.getEntryTypes()));
        Class<?>[] classes = SearchFieldFactory.classesForTypes(searchQuery.getEntryTypes());

        // generate queries for terms filtering stop words
        for (Map.Entry<String, QueryType> entry : terms.entrySet()) {
            String term = cleanQuery(entry.getKey());
            if (term.trim().isEmpty() || StandardAnalyzer.STOP_WORDS_SET.contains(term.toLowerCase()))
                continue;

            BioSafetyOption safetyOption = searchQuery.getBioSafetyOption();
            generateQueriesForType(fullTextSession, fields, builder, term, entry.getValue(), safetyOption);
        }

        // check for blast search results filter
        createBlastFilterQuery(fullTextSession, blastResults, builder);

        // wrap Lucene query in a org.hibernate.Query
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(builder.build(), classes);

        // get max score
        fullTextQuery.setFirstResult(0);
        fullTextQuery.setMaxResults(1);
        fullTextQuery.setProjection(FullTextQuery.SCORE);
        List<?> result = fullTextQuery.list();
        float maxScore = -1f;
        if (result.size() == 1) {
            maxScore = (Float) ((Object[]) (result.get(0)))[0];
        }
        // end get max score

        // get sorting values
        Sort sort = getSort(searchQuery.getParameters().isSortAscending(), searchQuery.getParameters().getSortField());
        fullTextQuery.setSort(sort);

        // projection (specified properties must be stored in the index @Field(store=Store.YES))
        fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.ID, "owner");

        // enable security filter if needed
        checkEnableSecurityFilter(userId.toLowerCase(), fullTextQuery);

        // check sample
        checkEnableHasAttribute(fullTextQuery, searchQuery.getParameters());

        // set paging params
        fullTextQuery.setFirstResult(searchQuery.getParameters().getStart());
        fullTextQuery.setMaxResults(searchQuery.getParameters().getRetrieveCount());

        resultCount = fullTextQuery.getResultSize();

        // execute search
        result = fullTextQuery.list();
        Logger.info(userId + ": " + resultCount + " results for \"" + searchQuery.getQueryString() + "\"");

        LinkedList<SearchResult> searchResults = new LinkedList<>();
        for (Object[] objects : (Iterable<Object[]>) result) {
            float score = (Float) objects[0];
            Long entryId = (Long) objects[1];
            SearchResult searchResult;
            if (blastResults != null) {
                searchResult = blastResults.get(Long.toString(entryId));
                if (searchResult == null) // this should not really happen since we already filter
                    continue;
            } else {
                searchResult = new SearchResult();
                searchResult.setScore(score);
                PartData info = ModelToInfoFactory.createTableView(entryId, null);
                info.setOwner((String) objects[2]);
                searchResult.setEntryInfo(info);
            }

            searchResult.setMaxScore(maxScore);
            searchResults.add(searchResult);
        }

        SearchResults results = new SearchResults();
        results.setResultCount(resultCount);
        results.setResults(searchResults);
        return results;
    }

    private void generateQueriesForType(HashSet<String> fields, String term, QueryType type, BioSafetyOption option) {
        SearchSession searchSession = Search.session(HibernateConfiguration.getCurrentSession());

        if (!StringUtils.isEmpty(term)) {
            // generate term queries for each search term
            String[] queryFields = fields.toArray(new String[0]);
            if (type == QueryType.PHRASE) {
                // phrase types are for quotes so slop is omitted
                for (String field : fields) {
                    builder.add(qb.phrase().onField(field).sentence(term).createQuery(), BooleanClause.Occur.SHOULD);
                }
            } else {
                // term
                if (term.contains("*")) {
                    query = qb.keyword().wildcard().onFields(SearchFieldFactory.getCommonFields()).matching(term).createQuery();
                    builder.add(query, BooleanClause.Occur.SHOULD);
                } else {
                    query = qb.keyword().onFields(queryFields).ignoreFieldBridge().matching(term).createQuery();
                    builder.add(query, BooleanClause.Occur.MUST);
                }
            }

            // visibility (using must not because "must for visibility ok" adds it as the query and affects the match
            // the security filter takes care of other values not to be included such as "transferred" and "deleted"
            Query visibilityQuery = qb.keyword().onField("visibility")
                .matching(Visibility.DRAFT.getValue()).createQuery();
            builder.add(visibilityQuery, BooleanClause.Occur.MUST_NOT);

            Query visibilityQuery2 = qb.keyword().onField("visibility")
                .matching(Visibility.DELETED.getValue()).createQuery();
            builder.add(visibilityQuery2, BooleanClause.Occur.MUST_NOT);

            Query visibilityQuery3 = qb.keyword().onField("visibility")
                .matching(Visibility.PERMANENTLY_DELETED.getValue()).createQuery();
            builder.add(visibilityQuery3, BooleanClause.Occur.MUST_NOT);

            // bio-safety level
            if (option != null) {
                TermContext levelContext = qb.keyword();
                Query biosafetyQuery = levelContext.onField("bioSafetyLevel").ignoreFieldBridge()
                    .matching(option.getValue()).createQuery();
                builder.add(biosafetyQuery, BooleanClause.Occur.MUST);
            }
        }
    }

    private SearchSort getSort(SearchScope<Entry> scope, boolean asc, ColumnField sortField) {
        if (sortField == null)
            sortField = ColumnField.CREATED;

        ScoreSortOptionsStep<?> sortStep;

        switch (sortField) {
            case RELEVANCE:
            default:
                return scope.sort().score().order(asc ? SortOrder.ASC : SortOrder.DESC).toSort();

            case TYPE:
                return scope.sort().field("type").order(asc ? SortOrder.ASC : SortOrder.DESC).toSort();
        }
    }

    /**
     * Filter results based on blast results. Query should be a bool
     *
     * @param blastResults results from blast search. empty indicates valid "no results"
     */
    private void createBlastFilterQuery(Map<String, SearchResult> blastResults,
                                        SearchScope<Entry> scope,
                                        BooleanPredicateClausesStep<?> boolClause) {
        // null blast results indicates no blast query
        if (blastResults == null)
            return;

        // enable blast filter
        for (String id : blastResults.keySet()) {
            boolClause.must(scope.predicate().match().field("id").matching(id));
        }
    }

//    private void checkEnableSecurityFilter(String userId, FullTextQuery fullTextQuery) {
//        Set<String> groupUUIDs = new HashSet<>();
//        Set<String> folderIds = new HashSet<>();
//
//        if (StringUtils.isEmpty(userId)) {
//            groupUUIDs.add(GroupController.PUBLIC_GROUP_UUID);
//        } else {
//            AccountController accountController = new AccountController();
//            if (accountController.isAdministrator(userId)) {
//                return;
//            }
//            groupUUIDs = new GroupController().retrieveAccountGroupUUIDs(userId);
//            folderIds = new Folders(userId).getCanReadFolderIds();
//        }
//
//        fullTextQuery.enableFullTextFilter("security")
//            .setParameter("account", userId)
//            .setParameter("folderIds", folderIds)
//            .setParameter("groupUUids", groupUUIDs);
//    }

//    private void checkEnableHasAttribute(FullTextQuery fullTextQuery, SearchQuery.Parameters parameters) {
//        if (parameters == null)
//            return;
//
//        ArrayList<String> terms = new ArrayList<>(3);
//
//        if (parameters.getHasSample()) {
//            terms.add("hasSample");
//        }
//
//        if (parameters.getHasAttachment()) {
//            terms.add("hasAttachment");
//        }
//
//        if (parameters.getHasSequence()) {
//            terms.add("hasSequence");
//        }
//
//        if (terms.isEmpty())
//            return;
//
//        fullTextQuery.enableFullTextFilter("boolean").setParameter("field", terms);
//    }

    private static class SingletonHolder {
        private static final HibernateSearch INSTANCE = new HibernateSearch();
    }
}
