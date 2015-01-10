package org.jbei.ice.lib.search;

import org.apache.lucene.search.BooleanClause;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author Hector Plahar
 */
public class SearchControllerTest {

    private SearchController controller;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new SearchController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testParseQueryString() throws Exception {
        String query = "\"the quick\" brown fox jumped \"over\" \"the\" moon";
        HashMap<String, BooleanClause.Occur> result = controller.parseQueryString(query);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.get("the quick"), BooleanClause.Occur.MUST);
        Assert.assertEquals(result.get("brown"), BooleanClause.Occur.SHOULD);
        Assert.assertEquals(result.get("fox"), BooleanClause.Occur.SHOULD);
        Assert.assertEquals(result.get("jumped"), BooleanClause.Occur.SHOULD);
        Assert.assertEquals(result.get("over"), BooleanClause.Occur.MUST);
        Assert.assertEquals(result.get("the"), BooleanClause.Occur.MUST);
        Assert.assertEquals(result.get("moon"), BooleanClause.Occur.SHOULD);
    }

    @Test
    public void testRunSearch() throws Exception {
//        Account account = AccountCreator.createTestAccount("testRunSearch", false);
//        Assert.assertNotNull(account);
//
//        // create entry
//        PlasmidData data = new PlasmidData();
//        data.setCircular(true);
//        data.setPromoters("pTet");
//        data.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
//        data.setOriginOfReplication("oRep");
//        data.setStatus("Complete");
//        data.setName("testPlasmid");
//        data.setFundingSource("DOE");
//        data.setPrincipalInvestigator("Nathan");
//        Entry entry = InfoToModelFactory.infoToEntry(data);
//        entry = new EntryCreator().createEntry(account, entry);
//        Assert.assertNotNull(entry);
//        Assert.assertTrue(entry.getId() > 0);
//        HibernateUtil.commitTransaction();   // commit triggers indexing
//
//        HibernateUtil.beginTransaction();
//        SearchQuery query = new SearchQuery();
//        query.setQueryString("testPlasmid");
//        SearchResults results = controller.runSearch(account.getEmail(), query, false);
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.getResultCount());
//
//        // search for promoters
//        query.setQueryString("pTet");
//        results = controller.runSearch(account.getEmail(), query, false);
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.getResultCount());
//
//        // search email
//        query.setQueryString(account.getEmail());
//        results = controller.runSearch(account.getEmail(), query, false);
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.getResultCount());
//
//        // fake search
//        query.setQueryString("FAKE_SEARCH");
//        results = controller.runSearch(account.getEmail(), query, false);
//        Assert.assertNotNull(results);
//        Assert.assertEquals(0, results.getResultCount());
    }
}
