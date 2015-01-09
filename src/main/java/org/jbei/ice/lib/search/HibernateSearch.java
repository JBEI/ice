package org.jbei.ice.lib.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.*;
import org.apache.lucene.util.OpenBitSet;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermContext;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.search.filter.SearchFieldFactory;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.servlet.ModelToInfoFactory;

import java.io.IOException;
import java.util.*;

/**
 * Apache Lucene full text library functionality in Hibernate
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class HibernateSearch {

    private HibernateSearch() {
    }

    private static class SingletonHolder {
        private static final HibernateSearch INSTANCE = new HibernateSearch();
    }

    /**
     * Retrieve the singleton instance of this class.
     *
     * @return HibernateSearch instance.
     */
    public static HibernateSearch getInstance() {
        return SingletonHolder.INSTANCE;
    }

    protected BooleanQuery generateQueriesForType(FullTextSession fullTextSession, HashSet<String> fields,
                                                  BooleanQuery booleanQuery, String term, BooleanClause.Occur occur, BioSafetyOption option,
                                                  HashMap<String, Float> userBoost) {
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        if (!StringUtils.isEmpty(term)) {
            // generate term queries for each search term
            for (String field : fields) {
                Query query;

                if (occur == BooleanClause.Occur.MUST)
                    query = qb.phrase().onField(field).sentence(term).createQuery();
                else if (term.contains("*")) {
                    if (!field.equals("name"))
                        continue;
                    query = qb.keyword().wildcard().onField(field).matching(term).createQuery();
                } else
                    query = qb.keyword().fuzzy().withThreshold(0.8f).onField(field).ignoreFieldBridge().matching(
                            term).createQuery();

                Float boost = userBoost.get(field);
                if (boost != null)
                    query.setBoost(boost);

                booleanQuery.add(query, BooleanClause.Occur.SHOULD);
            }

            // visibility (using must not because "must for visibility ok" adds it as the query and affects the match
            // the security filter takes care of other values not to be included such as "transferred" and "deleted"
            Query visibilityQuery = qb.keyword().onField("visibility")
                    .matching(Visibility.DRAFT.getValue()).createQuery();
            booleanQuery.add(visibilityQuery, BooleanClause.Occur.MUST_NOT);

            Query visibilityQuery2 = qb.keyword().onField("visibility")
                    .matching(Visibility.DELETED.getValue()).createQuery();
            booleanQuery.add(visibilityQuery2, BooleanClause.Occur.MUST_NOT);

            // bio-safety level
            if (option != null) {
                TermContext levelContext = qb.keyword();
                org.apache.lucene.search.Query biosafetyQuery =
                        levelContext.onField("bioSafetyLevel").ignoreFieldBridge()
                                .matching(option.getValue()).createQuery();
                booleanQuery.add(biosafetyQuery, BooleanClause.Occur.MUST);
            }
        }
        return booleanQuery;
    }

    public SearchResults executeSearchNoTerms(Account account, SearchQuery searchQuery) {
        ArrayList<EntryType> entryTypes = searchQuery.getEntryTypes();
        if (entryTypes == null || entryTypes.isEmpty()) {
            entryTypes = new ArrayList<>();
            entryTypes.addAll(Arrays.asList(EntryType.values()));
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        int resultCount;
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        BooleanQuery booleanQuery = new BooleanQuery();

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
        booleanQuery.add(recordTypeQuery, BooleanClause.Occur.MUST);

        // visibility
        Query visibilityQuery = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();
        booleanQuery.add(visibilityQuery, BooleanClause.Occur.MUST);

        // biosafety
        BioSafetyOption option = searchQuery.getBioSafetyOption();
        if (option != null) {
            TermContext bslContext = qb.keyword();
            org.apache.lucene.search.Query biosafetyQuery =
                    bslContext.onField("bioSafetyLevel").ignoreFieldBridge().matching(option.getValue()).createQuery();
            booleanQuery.add(biosafetyQuery, BooleanClause.Occur.MUST);
        }
        // wrap Lucene query in a org.hibernate.Query
        org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery,
                Entry.class);

        // get sorting values
        Sort sort = getSort(searchQuery.getParameters().isSortAscending(), searchQuery.getParameters().getSortField());
        fullTextQuery.setSort(sort);

        // enable security filter if needed
        checkEnableSecurityFilter(account, fullTextQuery);

        // enable has attachment/sequence/sample (if needed)
        checkEnableHasAttribute(fullTextQuery, searchQuery.getParameters());

        // check if there is also a blast search
        final HashMap<String, SearchResult> blastResults;
        if (searchQuery.getBlastQuery() != null && searchQuery.getBlastQuery().getSequence() != null) {
            try {
                blastResults = BlastPlus.runBlast(account, searchQuery.getBlastQuery());

                if (blastResults.size() > 0) {

                    // enable blast filter
                    Query query = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();
                    FilteredQuery filteredQuery = new FilteredQuery(query, new Filter() {
                        @Override
                        public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                            OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
                            TermDocs docs;
                            for (String id : blastResults.keySet()) {
                                docs = reader.termDocs(new Term("id", id));
                                while (docs.next()) {
                                    bitSet.set(docs.doc());
                                }
                            }
                            return bitSet;
                        }
                    });

                    booleanQuery.add(filteredQuery, BooleanClause.Occur.MUST);
                }

            } catch (BlastException e) {
                Logger.error(e);
                return null;
            }
        } else
            blastResults = null;

        // set paging params
        fullTextQuery.setFirstResult(searchQuery.getParameters().getStart());
        fullTextQuery.setMaxResults(searchQuery.getParameters().getRetrieveCount());

        resultCount = fullTextQuery.getResultSize();
        List result = fullTextQuery.list();

        LinkedList<SearchResult> searchResults = new LinkedList<>();
        String email = null;
        if (account != null)
            email = account.getEmail();

        for (Object object : result) {
            Entry entry = (Entry) object;
            SearchResult searchResult;
            if (blastResults != null) {
                searchResult = blastResults.get(Long.toString(entry.getId()));
                if (searchResult == null) // this should not really happen since we already filter
                    continue;
            } else {
                searchResult = new SearchResult();
                searchResult.setScore(1f);
                PartData info = ModelToInfoFactory.createTableViewData(email, entry, true);
                searchResult.setEntryInfo(info);
            }

            searchResult.setMaxScore(1f);
            searchResults.add(searchResult);
        }

        SearchResults results = new SearchResults();
        results.setResultCount(resultCount);
        results.setResults(searchResults);

        Logger.info(email + ": obtained " + resultCount + " results for empty query");
        return results;
    }

    /**
     * Intended to be called after running a blast search to filter the results. Blast search runs
     * on a fasta file which contains all the sequences. Hibernate search has a security filter for permissions
     * and is therefore used to filter out the blast results, as well as to filter based on the entry
     * attributes not handled by blast; such as "has sequence" and "bio-safety level"
     *
     * @param account      Account of user performing search
     * @param blastResults unfiltered blast result
     * @param searchQuery  Search Query
     * @return wrapper around list of filtered results
     */
    public SearchResults runSearchFilter(Account account, final HashMap<String, SearchResult> blastResults,
                                         SearchQuery searchQuery) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);

        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        Query query = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();
        FilteredQuery filteredQuery = new FilteredQuery(query, new Filter() {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
                TermDocs docs;
                for (String id : blastResults.keySet()) {
                    docs = reader.termDocs(new Term("id", id));
                    while (docs.next()) {
                        bitSet.set(docs.doc());
                    }
                }
                return bitSet;
            }
        });

        // wrap Lucene query in a org.hibernate.Query
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(filteredQuery, Entry.class);

        Set<String> groupUUIDs;
        if (account != null) {
            groupUUIDs = new GroupController().retrieveAccountGroupUUIDs(account);
        } else {
            groupUUIDs = new HashSet<>();
        }

        String email = account == null ? "" : account.getEmail();
        fullTextQuery.enableFullTextFilter("security")
                .setParameter("account", email)
                .setParameter("groupUUids", groupUUIDs);

        // paging parameters
        int start = searchQuery.getParameters().getStart();
        int count = searchQuery.getParameters().getRetrieveCount();

        // execute search
        fullTextQuery.setProjection("id");

        // list contains an object array with one Long object
        List luceneResult = fullTextQuery.list();
        HashSet<String> resultSet = new HashSet<>();

        // page
        for (Object object : luceneResult) {
            Long result = (Long) ((Object[]) object)[0];
            resultSet.add(result.toString());
        }

        HashSet<String> toRemove = new HashSet<>();
        for (String key : blastResults.keySet()) {
            if (!resultSet.contains(key))
                toRemove.add(key);
        }

        // remove
        for (String remove : toRemove) {
            blastResults.remove(remove);
        }

        SearchResult searchResults[] = new SearchResult[count];
        int limit = (start + count) > blastResults.size() ? blastResults.size() : (start + count);
        LinkedList<SearchResult> list = new LinkedList<>(Arrays.asList(blastResults.values().toArray(searchResults)).subList(start, limit));

        SearchResults results = new SearchResults();
        results.setResultCount(blastResults.size());

        // sort only if the sort is a blast sort that cannot be handled by the query filter
//        SearchResults.sort(searchQuery.getParameters().getSortField(), filtered);
        results.setResults(list);
        return results;
    }

    public SearchResults executeSearch(Account account, HashMap<String, BooleanClause.Occur> terms,
                                       SearchQuery searchQuery, HashMap<String, Float> userBoost) {
        // types for which we are searching
        ArrayList<EntryType> entryTypes = searchQuery.getEntryTypes();
        if (entryTypes == null || entryTypes.isEmpty()) {
            entryTypes = new ArrayList<>();
            entryTypes.addAll(Arrays.asList(EntryType.values()));
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        int resultCount;
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        BooleanQuery booleanQuery = new BooleanQuery();

        // get classes for search
        Class<?>[] classes = new Class<?>[EntryType.values().length];
        HashSet<String> fields = new HashSet<>();
        fields.addAll(SearchFieldFactory.getCommonFields());

        for (int i = 0; i < entryTypes.size(); i += 1) {
            EntryType type = entryTypes.get(i);
            classes[i] = SearchFieldFactory.entryClass(type);
            fields.addAll(SearchFieldFactory.entryFields(type));
        }

        // generate queries for terms
        for (Map.Entry<String, BooleanClause.Occur> entry : terms.entrySet()) {
            String term = cleanQuery(entry.getKey());
            if (term.trim().isEmpty() || StandardAnalyzer.STOP_WORDS_SET.contains(term))
                continue;

            BioSafetyOption safetyOption = searchQuery.getBioSafetyOption();
            booleanQuery = generateQueriesForType(fullTextSession, fields, booleanQuery, term, entry.getValue(),
                    safetyOption, userBoost);
        }

        if (booleanQuery.getClauses().length == 0)
            return executeSearchNoTerms(account, searchQuery);

        // check if there is also a blast search
        final HashMap<String, SearchResult> blastResults;
        if (searchQuery.getBlastQuery() != null && searchQuery.getBlastQuery().getSequence() != null) {
            try {
                blastResults = BlastPlus.runBlast(account, searchQuery.getBlastQuery());

                if (blastResults.size() > 0) {

                    // enable blast filter
                    QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
                    Query query = qb.keyword().onField("visibility").matching(Visibility.OK.getValue()).createQuery();
                    FilteredQuery filteredQuery = new FilteredQuery(query, new Filter() {
                        @Override
                        public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                            OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
                            TermDocs docs;
                            for (String id : blastResults.keySet()) {
                                docs = reader.termDocs(new Term("id", id));
                                while (docs.next()) {
                                    bitSet.set(docs.doc());
                                }
                            }
                            return bitSet;
                        }
                    });

                    booleanQuery.add(filteredQuery, BooleanClause.Occur.MUST);
                }

            } catch (BlastException e) {
                Logger.error(e);
                return null;
            }
        } else
            blastResults = null;

        // wrap Lucene query in a org.hibernate.Query
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, classes);

        // get max score
        fullTextQuery.setFirstResult(0); //start from the "start"th element
        fullTextQuery.setMaxResults(1);  //return count elements
        fullTextQuery.setProjection(FullTextQuery.SCORE);
        List result = fullTextQuery.list();
        float maxScore = -1f;
        if (result.size() == 1) {
            maxScore = (Float) ((Object[]) (result.get(0)))[0];
        }
        // end get max score

        // get sorting values
        Sort sort = getSort(searchQuery.getParameters().isSortAscending(), searchQuery.getParameters().getSortField());
        fullTextQuery.setSort(sort);

        // projection (specified properties must be stored in the index @Field(store=Store.YES))
        fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);

        // enable security filter if needed
        checkEnableSecurityFilter(account, fullTextQuery);

        // check sample
        checkEnableHasAttribute(fullTextQuery, searchQuery.getParameters());

        // set paging params
        fullTextQuery.setFirstResult(searchQuery.getParameters().getStart());
        fullTextQuery.setMaxResults(searchQuery.getParameters().getRetrieveCount());

        resultCount = fullTextQuery.getResultSize();

        // execute search
        result = fullTextQuery.list();
        Logger.info(resultCount + " results for \"" + searchQuery.getQueryString() + "\"");

        LinkedList<SearchResult> searchResults = new LinkedList<>();
        Iterator<Object[]> iterator = result.iterator();
        while (iterator.hasNext()) {
            Object[] objects = iterator.next();
            float score = (Float) objects[0];
            Entry entry = (Entry) objects[1];
            SearchResult searchResult;
            if (blastResults != null) {
                searchResult = blastResults.get(Long.toString(entry.getId()));
                if (searchResult == null) // this should not really happen since we already filter
                    continue;
            } else {
                searchResult = new SearchResult();
                searchResult.setScore(score);
                String userId = account != null ? account.getEmail() : null;
                PartData info = ModelToInfoFactory.createTableViewData(userId, entry, true);
                if (info == null)
                    continue;
                // for bulk edit
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

    protected Sort getSort(boolean asc, ColumnField sortField) {
        if (sortField == null)
            sortField = ColumnField.CREATED;

        switch (sortField) {
            case RELEVANCE:
            default:
                return new Sort(new SortField(SortField.FIELD_SCORE.getField(), SortField.FIELD_SCORE.getType(), asc));

            case TYPE:
                return new Sort(new SortField("recordType", SortField.STRING, asc));

            case CREATED:
                return new Sort(new SortField("creationTime", SortField.STRING, asc));
        }
    }

    /**
     * Enables the security filter if the account does not have administrative privileges
     *
     * @param account       account which is checked for administrative privs
     * @param fullTextQuery search fulltextquery for which filter is enabled
     */
    protected void checkEnableSecurityFilter(Account account, org.hibernate.search.FullTextQuery fullTextQuery) {
        if (account != null && account.getType() == AccountType.ADMIN) {
            return;
        }

        Set<String> groupUUIDs = new GroupController().retrieveAccountGroupUUIDs(account);
        String email = account == null ? "" : account.getEmail();
        fullTextQuery.enableFullTextFilter("security")
                .setParameter("account", email)
                .setParameter("groupUUids", groupUUIDs);
    }

    protected void checkEnableHasAttribute(org.hibernate.search.FullTextQuery fullTextQuery,
                                           SearchQuery.Parameters parameters) {
        if (parameters == null)
            return;

        ArrayList<String> terms = new ArrayList<>();

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

        fullTextQuery.enableFullTextFilter("boolean")
                     .setParameter("field", terms);
    }

    protected HashMap<String, SearchResult> checkEnableBlast(Account account, FullTextQuery fullTextQuery,
            SearchQuery query) throws BlastException {
        if (!query.hasBlastQuery())
            return null;

        HashMap<String, SearchResult> rids = BlastPlus.runBlast(account, query.getBlastQuery());
        fullTextQuery.enableFullTextFilter("blastFilter").setParameter("recordIds", new HashSet<>(rids.keySet()));
        return rids;
    }

    protected static String cleanQuery(String query) {
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
//        if (cleanedQuery.startsWith("*") || cleanedQuery.startsWith("?")) {
//            cleanedQuery = cleanedQuery.substring(1);
//        }
        return cleanedQuery;
    }
}
