package org.jbei.ice.search;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.search.SearchQuery;
import org.jbei.ice.dto.search.SearchResults;
import org.jbei.ice.entry.Entries;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Entry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author Hector Plahar
 */
public class SearchIndexesTest {

    private SearchIndexes controller;

    @Before
    public void setUp() throws Exception {
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
        controller = new SearchIndexes();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }

    @Test
    public void testParseQueryString() {
        String query = "\"the quick\" brown fox jumped \"over\" \"the\" moon";
        HashMap<String, QueryType> result = controller.parseQueryString(query);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.get("the quick"), QueryType.PHRASE);
        Assert.assertEquals(result.get("brown"), QueryType.TERM);
        Assert.assertEquals(result.get("fox"), QueryType.TERM);
        Assert.assertEquals(result.get("jumped"), QueryType.TERM);
        Assert.assertEquals(result.get("over"), QueryType.PHRASE);
        Assert.assertEquals(result.get("the"), QueryType.PHRASE);
        Assert.assertEquals(result.get("moon"), QueryType.TERM);
    }

    @Test
    public void testRunSearch() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testRunSearch", false);
        Assert.assertNotNull(account);

        // create entry
        PartData partData = new PartData(EntryType.PLASMID);
        partData.setName("testPlasmid");

        partData = new Entries(account.getEmail()).create(partData);
        Entry entry = DAOFactory.getEntryDAO().get(partData.getId());

        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getId() > 0);
        HibernateConfiguration.commitTransaction();   // commit triggers indexing

        HibernateConfiguration.beginTransaction();
        SearchQuery query = new SearchQuery();
        query.setQueryString("testPlasmid");
        SearchResults results = controller.runSearch(account.getEmail(), query);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // case insensentive
        results = controller.runSearch(account.getEmail().toLowerCase(), query);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // search for promoters
        query.setQueryString("pTet");
        results = controller.runSearch(account.getEmail(), query);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // search email
        query.setQueryString(account.getEmail());
        results = controller.runSearch(account.getEmail(), query);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // fake search
        query.setQueryString("FAKE_SEARCH");
        results = controller.runSearch(account.getEmail(), query);
        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.getResultCount());
    }
}
