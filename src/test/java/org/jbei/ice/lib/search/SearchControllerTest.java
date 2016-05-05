package org.jbei.ice.lib.search;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PlasmidData;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
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
        Account account = AccountCreator.createTestAccount("testRunSearch", false);
        Assert.assertNotNull(account);

        // create entry
        PlasmidData plasmidData = new PlasmidData();
        plasmidData.setCircular(true);
        plasmidData.setPromoters("pTet");
        plasmidData.setOriginOfReplication("oRep");

        PartData partData = new PartData(EntryType.PLASMID);
        partData.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        partData.setStatus("Complete");
        partData.setName("testPlasmid");
        partData.setFundingSource("DOE");
        partData.setPrincipalInvestigator("Nathan");
        partData.setPlasmidData(plasmidData);
        Entry entry = InfoToModelFactory.infoToEntry(partData);
        entry = new EntryCreator().createEntry(account, entry, null);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getId() > 0);
        HibernateUtil.commitTransaction();   // commit triggers indexing

        HibernateUtil.beginTransaction();
        SearchQuery query = new SearchQuery();
        query.setQueryString("testPlasmid");
        SearchResults results = controller.runSearch(account.getEmail(), query);
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
