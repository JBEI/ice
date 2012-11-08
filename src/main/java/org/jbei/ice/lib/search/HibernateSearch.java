package org.jbei.ice.lib.search;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.server.EntryViewFactory;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SearchResultInfo;
import org.jbei.ice.shared.dto.SearchResults;

import org.apache.lucene.search.Sort;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Apache Lucene full text library functionality in Hibernate
 *
 * @author Hector Plahar
 */
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

    public void executeSearchOnField(String queryString, String field, int start, int limit) {
        Session session = HibernateHelper.newSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();

        org.apache.lucene.search.Query query = qb
                .keyword().fuzzy().withThreshold(0.8f)
                .onField(field)
                .matching(queryString)
                .createQuery();
        org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, Entry.class);
        fullTextQuery.setSort(Sort.RELEVANCE);
        fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);

        fullTextQuery.setFirstResult(start); //start from the "startth" element
        fullTextQuery.setMaxResults(limit); //return count elements

        int resultCount = fullTextQuery.getResultSize(); // this is where you are hiding
//        fquery.setProjection("id");

        // execute search
        List result = fullTextQuery.list();
        Logger.info("Found " + result.size() + " for " + fullTextQuery.getQueryString());
    }

    public SearchResults executeSearch(Account account, String queryString, int start, int count,
            PermissionsController permissionsController) {

        LinkedList<SearchResultInfo> searchResultInfos = new LinkedList<SearchResultInfo>();
        Session session = HibernateHelper.newSession();
        int resultCount = 0;
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        List result;

        // create native Lucene query using the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though

        // you can create several query builders (for each entity type involved in the root of the query)
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        boolean wildCard = queryString.endsWith("*");

//        TermContext termContext = qb.keyword();
//        if( wildCard)
//            termContext = termContext.wildcard();

        // use phrase (instead of keyword) for more than one word
        org.apache.lucene.search.Query query = qb
                .keyword()
                .fuzzy().withThreshold(0.8f)
                .onFields("owner", "creator", "names.name", "alias", "creator", "keywords", "shortDescription",
                          "references", "longDescription", "intellectualProperty")
                .matching(queryString)
                .createQuery();

        // wrap Lucene query in a org.hibernate.Query
        org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, Entry.class);
        fullTextQuery.setSort(Sort.RELEVANCE);

        // criteria example
//        fullTextQuery.setCriteriaQuery(session.createCriteria(Entry.class.getName()).add(
//                Restrictions.eq("ownerEmail", account.getEmail())));

        // projection (specified properties must be stored in the index)
        fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);

        // for paging
        fullTextQuery.setFirstResult(start); //start from the startth element
        fullTextQuery.setMaxResults(count); //return count elements

        // setting sort
//        org.hibernate.search.FullTextQuery fquery = fullTextSession.createFullTextQuery( query);
//        org.apache.lucene.search.Sort sort = new Sort(new SortField("title"));
//        fquery.setSort(sort);
//        List results = fquery.list();

        // instead of returning the full domain object you can return a subset of the properties
//                org.hibernate.search.FullTextQuery fquery = fullTextSession.createFullTextQuery( query);
        resultCount = fullTextQuery.getResultSize(); // this is where you are hiding
//        fquery.setProjection("id");

        // execute search
        result = fullTextQuery.list();
        Logger.info("Found " + result.size() + " for " + fullTextQuery.getQueryString());

        // sort

        Iterator<Object[]> iterator = result.iterator();
        while (iterator.hasNext()) {
            Object[] objects = iterator.next();
            float score = ((Float) objects[0]).floatValue();
            Entry entry = (Entry) objects[1];
            try {
                if (!permissionsController.hasReadPermission(account, entry))
                    continue;
            } catch (ControllerException e) {
                Logger.error(e);
                continue;
            }

            EntryInfo info = EntryViewFactory.createTipView(account, entry);
            SearchResultInfo searchResult = new SearchResultInfo();
            searchResult.setScore(score);
            searchResult.setEntryInfo(info);
            searchResultInfos.add(searchResult);
        }

        SearchResults results = new SearchResults();
        results.setResultCount(resultCount);
        results.setResults(searchResultInfos);
        return results;
    }
}
