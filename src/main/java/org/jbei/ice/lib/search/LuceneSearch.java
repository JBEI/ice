package org.jbei.ice.lib.search;

import java.util.List;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Name;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Full text search of the registry using Lucene search engine.
 * <p/>
 * It uses a single instance of the IndexSearcher for efficiency, and most importantly to be able to
 * rebuild the underlying index transparently. When rebuildIndex() is called, the index is rebuilt,
 * after which a new IndexSearcher is instantiated with the updated index.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
public class LuceneSearch {

    private LuceneSearch() {
    }

    private static class SingletonHolder {
        private static final LuceneSearch INSTANCE = new LuceneSearch();
    }

    /**
     * Retrieve the singleton instance of this classe.
     *
     * @return LuceneSearch instance.
     */
    public static LuceneSearch getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void executeSearch(String queryString) {
        Session session = HibernateHelper.newSession();

        FullTextSession fullTextSession = Search.getFullTextSession(session);
        Transaction tx = fullTextSession.beginTransaction();

        // create native Lucene query unsing the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though

        // you can create several query builders (for each entity type involved in the root of the query)
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Name.class).get();


        org.apache.lucene.search.Query query = qb
                .keyword()
                .fuzzy()
                .onFields("name")
                .matching(queryString)
                .createQuery();

        // wrap Lucene query in a org.hibernate.Query
        org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, Name.class);

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
