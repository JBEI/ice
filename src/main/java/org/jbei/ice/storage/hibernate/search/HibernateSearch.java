package org.jbei.ice.storage.hibernate.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.*;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermContext;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.search.FieldFilter;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.folder.Folders;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.QueryType;
import org.jbei.ice.lib.search.filter.SearchFieldFactory;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.ColumnField;
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

    public SearchResults executeSearchNoTerms(String userId, HashMap<String, SearchResult> blastResults, SearchQuery searchQuery) {
        ArrayList<EntryType> entryTypes = searchQuery.getEntryTypes();
        if (entryTypes == null || entryTypes.isEmpty()) {
            entryTypes = new ArrayList<>(Arrays.asList(EntryType.values()));
        }

        int resultCount;
        FullTextSession fullTextSession = HibernateConfiguration.getFullTextSession();
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();

        ArrayList<Query> except = new ArrayList<>();
        for (EntryType type : EntryType.values()) {
            if (entryTypes.contains(type))
                continue;

            except.add(qb.keyword().onField("recordType").matching(type.getName()).createQuery());
        }

        // add terms for record types
        Query[] queries = new Query[]{};
        Query recordTypeQuery = qb.all().except(except.toArray(queries)).createQuery();
        builder.add(recordTypeQuery, BooleanClause.Occur.FILTER);

        // visibility
        Query visibilityQuery = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();
        builder.add(visibilityQuery, BooleanClause.Occur.FILTER);

        // bio safety level
        BioSafetyOption option = searchQuery.getBioSafetyOption();
        if (option != null) {
            TermContext bslContext = qb.keyword();
            Query biosafetyQuery =
                    bslContext.onField("bioSafetyLevel").ignoreFieldBridge().matching(option.getIntValue()).createQuery();
            builder.add(biosafetyQuery, BooleanClause.Occur.FILTER);
        }

        // check filter filters
        if (searchQuery.getFieldFilters() != null && !searchQuery.getFieldFilters().isEmpty()) {
            for (FieldFilter fieldFilter : searchQuery.getFieldFilters()) {
                String searchField = SearchFieldFactory.searchFieldForEntryField(fieldFilter.getField());
                if (StringUtils.isEmpty(searchField))
                    continue;

                Query filterQuery = qb.keyword().onField(searchField).matching(fieldFilter.getFilter()).createQuery();
                builder.add(filterQuery, BooleanClause.Occur.MUST);
            }
        }

        // check if there is a blast results
        createBlastFilterQuery(fullTextSession, blastResults, builder);

        // wrap Lucene query in a org.hibernate.Query
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(builder.build(), Entry.class);

        // get sorting values
        Sort sort = getSort(searchQuery.getParameters().isSortAscending(), searchQuery.getParameters().getSortField());
        fullTextQuery.setSort(sort);
        fullTextQuery.setProjection(ProjectionConstants.ID, "owner");

        // enable security filter if needed
        checkEnableSecurityFilter(userId, fullTextQuery);

        // enable has attachment/sequence/sample (if needed)
        checkEnableHasAttribute(fullTextQuery, searchQuery.getParameters());

        // set paging params
        fullTextQuery.setFirstResult(searchQuery.getParameters().getStart());
        fullTextQuery.setMaxResults(searchQuery.getParameters().getRetrieveCount());

        resultCount = fullTextQuery.getResultSize();
        List<?> result = fullTextQuery.list();

        LinkedList<SearchResult> searchResults = new LinkedList<>();

        // since we are using projection, result is an arraylist of objects
        for (Object object : result) {
            Object[] objects = (Object[]) object;
            long entryId = (Long) objects[0];
            SearchResult searchResult;
            if (blastResults != null) {
                searchResult = blastResults.get(Long.toString(entryId));
                if (searchResult == null) // this should not really happen since we already filter
                    continue;
            } else {
                searchResult = new SearchResult();
                searchResult.setScore(1f);
                PartData info = ModelToInfoFactory.createTableView(entryId, null);
                info.setOwner((String) objects[1]);
                searchResult.setEntryInfo(info);
            }

            searchResult.setMaxScore(1f);
            searchResults.add(searchResult);
        }

        SearchResults results = new SearchResults();
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
        FullTextSession fullTextSession = HibernateConfiguration.getFullTextSession();

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

    @SuppressWarnings("unchecked")
    public SearchResults executeSearch(String userId, HashMap<String, QueryType> terms,
                                       SearchQuery searchQuery,
                                       HashMap<String, SearchResult> blastResults) {
        int resultCount;
        FullTextSession fullTextSession = HibernateConfiguration.getFullTextSession();
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        // get classes for search
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

    private void generateQueriesForType(FullTextSession fullTextSession, HashSet<String> fields,
                                        BooleanQuery.Builder builder, String term, QueryType type,
                                        BioSafetyOption option) {
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        if (!StringUtils.isEmpty(term)) {
            // generate term queries for each search term
            Query query;
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

    private Sort getSort(boolean asc, ColumnField sortField) {
        if (sortField == null)
            sortField = ColumnField.CREATED;

        switch (sortField) {
            case RELEVANCE:
            default:
                return new Sort(new SortField(SortField.FIELD_SCORE.getField(), SortField.FIELD_SCORE.getType(), asc));

            case TYPE:
                return new Sort(new SortField("recordType", SortField.Type.STRING, asc));

            case PART_ID:
                return new Sort(new SortField("partNumber_forSort", SortField.Type.STRING, asc));

            case CREATED:
                return new Sort(new SortField("creationTime", SortField.Type.INT, asc));
        }
    }

    // empty blast results indicates valid results
    private void createBlastFilterQuery(FullTextSession fullTextSession,
                                        final HashMap<String, SearchResult> blastResults,
                                        BooleanQuery.Builder builder) {
        // null blast results indicates no blast query
        if (blastResults == null)
            return;

        // enable blast filter
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        Query query = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();
        builder.add(query, BooleanClause.Occur.FILTER);
        for (String id : blastResults.keySet()) {
            Query filterQuery = qb.keyword().onField("id").matching(id).createQuery();
            builder.add(filterQuery, BooleanClause.Occur.FILTER);
        }
    }

    /**
     * Enables the security filter if the account does not have administrative privileges
     *
     * @param userId        identifier for account which is checked for administrative privs
     * @param fullTextQuery search fulltextquery for which filter is enabled
     */
    private void checkEnableSecurityFilter(String userId, FullTextQuery fullTextQuery) {
        Set<String> groupUUIDs = new HashSet<>();
        Set<String> folderIds = new HashSet<>();

        if (StringUtils.isEmpty(userId)) {
            groupUUIDs.add(GroupController.PUBLIC_GROUP_UUID);
        } else {
            AccountController accountController = new AccountController();
            if (accountController.isAdministrator(userId)) {
                return;
            }
            groupUUIDs = new GroupController().retrieveAccountGroupUUIDs(userId);
            folderIds = new Folders(userId).getCanReadFolderIds();
        }

        fullTextQuery.enableFullTextFilter("security")
                .setParameter("account", userId)
                .setParameter("folderIds", folderIds)
                .setParameter("groupUUids", groupUUIDs);
    }

    private void checkEnableHasAttribute(FullTextQuery fullTextQuery, SearchQuery.Parameters parameters) {
        if (parameters == null)
            return;

        ArrayList<String> terms = new ArrayList<>(3);

        if (parameters.getHasSample()) {
            terms.add("hasSample");
        }

        if (parameters.getHasAttachment()) {
            terms.add("hasAttachment");
        }

        if (parameters.getHasSequence()) {
            terms.add("hasSequence");
        }

        if (terms.isEmpty())
            return;

        fullTextQuery.enableFullTextFilter("boolean").setParameter("field", terms);
    }

    private static class SingletonHolder {
        private static final HibernateSearch INSTANCE = new HibernateSearch();
    }
}
