package org.jbei.ice.lib.search;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResults;
import org.jbei.ice.server.InfoToModelFactory;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class SearchControllerTest {

    private SearchController controller;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new SearchController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testRunSearch() throws Exception {
        Account account = AccountCreator.createTestAccount("testRunSearch", false);
        Assert.assertNotNull(account);

        // create entry
        PlasmidData data = new PlasmidData();
        data.setCircular(true);
        data.setPromoters("pTet");
        data.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        data.setOriginOfReplication("oRep");
        data.setStatus("Complete");
        data.setName("testPlasmid");
        data.setFundingSource("DOE");
        data.setPrincipalInvestigator("Nathan");
        Entry entry = InfoToModelFactory.infoToEntry(data);
        entry = ControllerFactory.getEntryController().createEntry(account, entry);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getId() > 0);
        HibernateHelper.commitTransaction();   // commit triggers indexing

        HibernateHelper.beginTransaction();
        SearchQuery query = new SearchQuery();
        query.setQueryString("testPlasmid");
        SearchResults results = controller.runSearch(account, query, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // search for promoters
        query.setQueryString("pTet");
        results = controller.runSearch(account, query, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // search email
        query.setQueryString(account.getEmail());
        results = controller.runSearch(account, query, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // fake search
        query.setQueryString("FAKE_SEARCH");
        results = controller.runSearch(account, query, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.getResultCount());
    }

    @Test
    public void test() throws Exception {
        Account a1 = AccountCreator.createTestAccount("test1", false);
        Account a2 = AccountCreator.createTestAccount("test2", false);

        // create entry
        PartData partInfo = new PartData();
        partInfo.setBioSafetyLevel(1);
        partInfo.setStatus("Complete");
        partInfo.setShortDescription("test");
        Entry entry = InfoToModelFactory.infoToEntry(partInfo, null);
        ControllerFactory.getEntryController().createEntry(a1, entry);
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getId() > 0);
        HibernateHelper.commitTransaction();   // commit triggers indexing

        HibernateHelper.beginTransaction();
        SearchQuery query = new SearchQuery();
        query.setQueryString("test");
        SearchResults results = controller.runSearch(a1, query, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getResultCount());

        // attempt with a2 (should not have permissions)
        results = controller.runSearch(a2, query, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.getResultCount());

        FolderDetails folder = ControllerFactory.getFolderController().createNewFolder(a1, "testFolder", "test", null);
        Assert.assertNotNull(folder);
        ArrayList<Entry> list = new ArrayList<>();
        list.add(entry);
        Assert.assertNotNull(ControllerFactory.getFolderController().addFolderContents(a1, folder.getId(), list));
        List<Folder> folders = ControllerFactory.getFolderController().getFoldersByEntry(entry);
        Assert.assertNotNull(folders);
        Assert.assertEquals(1, folders.size());
        Assert.assertEquals(folder.getId(), folders.get(0).getId());
    }
}
