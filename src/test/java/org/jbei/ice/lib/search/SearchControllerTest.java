package org.jbei.ice.lib.search;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.search.SearchQuery;
import org.jbei.ice.shared.dto.search.SearchResults;

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
        Account account = createTestAccount("testRunSearch", false);
        Assert.assertNotNull(account);

        // create entry
        PlasmidInfo info = new PlasmidInfo();
        info.setCircular(true);
        info.setPromoters("pTet");
        info.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        info.setOriginOfReplication("oRep");
        info.setStatus("Complete");
        info.setName("testPlasmid");
        info.setFundingSource("DOE");
        info.setPrincipalInvestigator("Nathan");
        Entry entry = InfoToModelFactory.infoToEntry(info);
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

    protected Account createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountController accountController = new AccountController();
        Account account = accountController.getByEmail(email);
        if (account != null)
            throw new Exception("duplicate account");

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setFirstName("");
        accountInfo.setLastName("TEST");
        accountInfo.setEmail(email);
        String pass = accountController.createNewAccount(accountInfo, false);

        Assert.assertNotNull(pass);
        account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        if (admin) {
            account.setType(AccountType.ADMIN);
            accountController.save(account);
        }
        return account;
    }
}