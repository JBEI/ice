package org.jbei.ice.lib.search;

import java.util.List;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
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
     * Retrieve the singleton instance of this classe.
     *
     * @return HibernateSearch instance.
     */
    public static HibernateSearch getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void executeSearch(String queryString) {
        Session session = HibernateHelper.newSession();

        FullTextSession fullTextSession = Search.getFullTextSession(session);
        Transaction tx = fullTextSession.beginTransaction();

        // create native Lucene query using the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though

        // you can create several query builders (for each entity type involved in the root of the query)
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();

        org.apache.lucene.search.Query query = qb
//                .phrase()
//                .onField("owner")
//                .sentence(queryString)
//                .createQuery();
                .keyword()
                .fuzzy()
                .onFields("owner", "creator")
                .matching(queryString)
                .createQuery();

        // wrap Lucene query in a org.hibernate.Query
        org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, Entry.class);

        // for paging
//        fullTextQuery.setFirstResult(15); //start from the 15th element
//        fullTextQuery.setMaxResults(10); //return 10 elements

        // setting sort
//        org.hibernate.search.FullTextQuery fquery = fullTextSession.createFullTextQuery( query);
//        org.apache.lucene.search.Sort sort = new Sort(new SortField("title"));
//        fquery.setSort(sort);
//        List results = fquery.list();

        // instead of returning the full doman object you can return a subset of the properties
//                org.hibernate.search.FullTextQuery fquery = fullTextSession.createFullTextQuery( query);
//        fquery.getResultSize() ; // this is where you are hiding
//        fquery.setProjection("id");
//        List results = fquery.list();

        // execute search
        List result = fullTextQuery.list();
        System.out.println("Found " + result.size() + " for " + fullTextQuery.getQueryString());

        // sort
        tx.commit();
        session.close();
    }
}
