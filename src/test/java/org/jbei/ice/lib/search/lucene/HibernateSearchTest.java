package org.jbei.ice.lib.search.lucene;

import java.util.List;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Sort;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class HibernateSearchTest {

    @Before
    public void setUp() {
//        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
    }

    @After
    public void tearDown() {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testExecuteSearch() throws Exception {
    }

    @Test
    public void testExecuteMultiTerm() throws Exception {
    }

    @Test
    public void test() throws Exception {
        String queryString = "test";
        String field = "creatorEmail";
        int start = 0;
        int limit = 20;

        Session session = HibernateHelper.newSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        boolean wildCard = queryString.endsWith("*") || queryString.endsWith("?");
        BooleanQuery boolQuery = new BooleanQuery();


        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Entry.class).get();
        org.apache.lucene.search.Query query;
        if (wildCard) {
            query = qb.keyword().wildcard().onField(field).matching(queryString).createQuery();
        } else {
            query = qb
                    .keyword().fuzzy().withThreshold(0.8f)        // todo add threshold as params to fields
                    .onField(field)
                    .ignoreFieldBridge()    // search for objects of type string only
                    .matching(queryString)
                    .createQuery();
        }
        boolQuery.add(query, BooleanClause.Occur.MUST);

        org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(boolQuery, Entry.class);
        fullTextQuery.setSort(Sort.RELEVANCE);
        fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
        fullTextQuery.enableFullTextFilter("security").setParameter("account",
                                                                    (new GroupController().createOrRetrievePublicGroup()
                                                                                          .getUuid()));

        fullTextQuery.setFirstResult(start); //start from the "startth" element
        fullTextQuery.setMaxResults(limit); //return count elements

        int resultCount = fullTextQuery.getResultSize(); // this is where you are hiding
//        fquery.setProjection("id");

        // execute search
        List result = fullTextQuery.list();
        Logger.info("Found " + resultCount + " for " + fullTextQuery.getQueryString());
    }
}
