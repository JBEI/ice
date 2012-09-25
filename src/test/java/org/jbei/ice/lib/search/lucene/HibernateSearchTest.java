package org.jbei.ice.lib.search.lucene;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.search.HibernateSearch;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class HibernateSearchTest {

    @Before
    public void setUp() {
//        HibernateHelper.initializeMock();
    }

    @Test
    public void testExecuteSearch() throws Exception {
        // TODO : create entries
//        ApplicationController.initializeHibernateSearch();


        AccountController controller = new AccountController();
        String email = "haplahar@lbl.gov";
//        String password = controller.createNewAccount("", "TESTER", "", email, "LBL", "");
//        Assert.assertNotNull(password);
        Account account = controller.getByEmail(email);
        Assert.assertNotNull(account);
//
//        EntryController entryController = new EntryController();
//
//        for( int i = 0; i < 10; i += 1 ) {
//            Strain strain = new Strain();
//            strain.setOwnerEmail(email);
//            strain.setOwner("Hector Plahar");
//            strain.setCreator(strain.getOwner());
//            Name name = new Name();
//            name.setName("foo"+i);
//            strain.getNames().add(name);
//            EntryFundingSource fundingSource = new EntryFundingSource();
//            FundingSource source = new FundingSource();
//            source.setFundingSource("Funding");
//            source.setPrincipalInvestigator("Funding PI " + i);
//            fundingSource.setFundingSource(source);
//            strain.getEntryFundingSources().add(fundingSource);
//            entryController.createEntry(account, strain, false, null);
//
//        }
//
        ApplicationController.initializeHibernateSearch();
        HibernateSearch.getInstance().executeSearch(account, "Hector", new PermissionsController());
    }
}
