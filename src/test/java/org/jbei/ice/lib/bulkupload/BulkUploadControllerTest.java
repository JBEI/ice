package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.server.dao.hibernate.HibernateHelper;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.Visibility;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for BulkUploadController
 *
 * @author Hector Plahar
 */
public class BulkUploadControllerTest {

    private BulkUploadController controller;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
        controller = new BulkUploadController();
        AccountController accountController = new AccountController();
        if (accountController.getByEmail("system") == null)
            accountController.createNewAccount("System", "Account", "", "system", "", "");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRetrievePendingImports() throws Exception {
        AccountController accountController = new AccountController();
        Account adminAccount = accountController.createAdminAccount("tester+pending@test.org", "popop");
        Assert.assertNotNull(adminAccount);
        accountController.createNewAccount("", "TESTER", "", "regular+pending@test.org", "LBL", "");

        // should not be any pending
        ArrayList<BulkUploadInfo> infos = controller.retrievePendingImports(adminAccount);
        Assert.assertNotNull(infos);
        Assert.assertTrue(infos.isEmpty());

        Account account = accountController.getByEmail("regular+pending@test.org");

        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        final int count = 100;
        for (int i = 0; i < count; i += 1) {
            ArabidopsisSeedInfo info = new ArabidopsisSeedInfo();
            info.setGeneration(ArabidopsisSeedInfo.Generation.M1);
            info.setName("Name" + i);
            info.setAlias("Alias" + i);
            info.setEcotype("Ecotype" + i);
            info.setHomozygosity("homozygot" + i);
            info.setParents("parent" + i);
            info.setPlantType(ArabidopsisSeedInfo.PlantType.OTHER);
            entryList.add(info);
        }
        boolean success = controller.submitBulkImport(account, EntryAddType.ARABIDOPSIS, entryList);
        Assert.assertTrue("Could not submit bulk import", success);

        // 1 pending
        infos = controller.retrievePendingImports(adminAccount);
        Assert.assertNotNull(infos);
        Assert.assertEquals(1, infos.size());
        Assert.assertEquals(count, infos.get(0).getCount());
    }

    @Test
    public void comprehensiveTest() throws Exception {

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", "tester@test.org", "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail("tester@test.org");
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount("tester+admin@test.org", "popop");
        Assert.assertNotNull(adminAccount);
        // create system account
//        accountController.createNewAccount("System", "Account", "", "system", "", "");

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList);
        Assert.assertNotNull(createdDraft);

        // update to see behavior (nothing has changed)
        BulkUploadInfo updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertEquals("Updated draft has a different id from existing", createdDraft.getId(),
                            updatedDraft.getId());

        // actually update with entry (part)
        EntryInfo info = new PartInfo();
        info.setAlias("alias");
        entryList.add(info);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertTrue(updatedDraft.getCount() == 1);

        // retrieve updated
        updatedDraft = controller.retrieveById(account, createdDraft.getId());
        EntryInfo added = updatedDraft.getEntryList().get(0);
        long addedId = added.getId();
        Assert.assertNotNull(added);

        // update existing change value
        entryList = updatedDraft.getEntryList();
        added.setAlias("new alias");
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertTrue(updatedDraft.getCount() == 1);
        Assert.assertEquals(addedId, updatedDraft.getEntryList().get(0).getId());

        // check the id of the updated
        updatedDraft = controller.retrieveById(account, updatedDraft.getId());
        Assert.assertTrue(updatedDraft.getCount() == 1);
        Assert.assertEquals(addedId, updatedDraft.getEntryList().get(0).getId());

        EntryController entryController = new EntryController();
        Entry newEntry = entryController.get(account, addedId);
        Assert.assertNotNull(newEntry);
        Assert.assertEquals(Visibility.DRAFT.getValue(), newEntry.getVisibility().intValue());
        Assert.assertEquals(newEntry.getAlias(), "new alias"); // check updated name

        // update existing and add one more
        added.setName("Part Test");
        EntryInfo newInfo = new PartInfo();
        newInfo.setLongDescription("This is a long description");
        entryList = updatedDraft.getEntryList();
        entryList.add(newInfo);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertNotNull(updatedDraft);

        ArrayList<Entry> allEntries = entryController.getAllEntries();
        Assert.assertNotNull(allEntries);
        int count = 0;
        for (Entry entry : allEntries) {
            if (entry.getOwnerEmail().equals(account.getEmail()))
                count += 1;
        }
        Assert.assertEquals(2, count);

        // verify
        BulkUploadInfo verify = controller.retrieveById(account, createdDraft.getId());
        Assert.assertEquals(2, verify.getCount()); // expect two entries now
        long partId = verify.getEntryList().get(0).getId();
        long strainId = verify.getEntryList().get(1).getId();


        Entry entry = entryController.get(account, partId);
        Entry partEntry = entryController.get(account, strainId);

        Assert.assertEquals(Visibility.DRAFT.getValue(), entry.getVisibility().intValue());
        Assert.assertEquals(Visibility.DRAFT.getValue(), partEntry.getVisibility().intValue());

        // submit for approval
        boolean submitted = controller.submitBulkImportDraft(account, verify.getId(), new ArrayList<EntryInfo>(
                verify.getEntryList()));
        Assert.assertTrue(submitted);

        entry = entryController.get(account, partId);
        partEntry = entryController.get(account, strainId);

        Assert.assertEquals(Visibility.PENDING.getValue(), entry.getVisibility().intValue());
        Assert.assertEquals(Visibility.PENDING.getValue(), partEntry.getVisibility().intValue());

        // ensure record still exists
        boolean caught = false;
        try {
            controller.retrieveById(account, verify.getId());
        } catch (PermissionException e) {
            caught = true;
        }

        Assert.assertTrue(caught);
        verify = controller.retrieveById(adminAccount, verify.getId());
        Assert.assertNotNull(verify);

        // user should not own it anymore
        ArrayList<BulkUploadInfo> user = controller.retrieveByUser(account, account);
        Assert.assertTrue(user.isEmpty());

        // however the entry owners should be the same
        entry = entryController.get(account, partId);
        partEntry = entryController.get(account, strainId);
        Assert.assertNotNull(entry);
        Assert.assertNotNull(partEntry);
        Assert.assertEquals(entry.getOwnerEmail(), account.getEmail());
        Assert.assertEquals(partEntry.getOwnerEmail(), account.getEmail());

        // should be owned by system
        user = controller.retrieveByUser(adminAccount, accountController.getSystemAccount());
        int systemSize = user.size();
        Assert.assertTrue(systemSize >= 1);

        // now approve
        Assert.assertTrue(controller.approveBulkImport(adminAccount, verify.getId(), verify.getEntryList()));

        entry = entryController.get(account, partId);
        partEntry = entryController.get(account, strainId);

        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());
        Assert.assertEquals(Visibility.OK.getValue(), partEntry.getVisibility().intValue());

        // the owners should be the same
        Assert.assertEquals(entry.getOwnerEmail(), account.getEmail());
        Assert.assertEquals(partEntry.getOwnerEmail(), account.getEmail());

        user = controller.retrieveByUser(adminAccount, accountController.getSystemAccount());
        if (systemSize == 1)
            Assert.assertTrue(user.isEmpty());
    }

    @Test
    public void testRetrieveById() throws Exception {
        AccountController accountController = new AccountController();
        Account adminAccount = accountController.createAdminAccount("tester+retrieveById@test.org", "popop");
        Assert.assertNotNull(adminAccount);
        accountController.createNewAccount("", "TESTER", "", "regular+retrieveById@test.org", "LBL", "");

        Account account = accountController.getByEmail("tester+retrieveById@test.org");
        try {
            controller.retrieveById(account, 1);
        } catch (ControllerException ce) {
        }

        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        final int count = 10;
        for (int i = 0; i < count; i += 1) {
            ArabidopsisSeedInfo info = new ArabidopsisSeedInfo();
            info.setGeneration(ArabidopsisSeedInfo.Generation.M1);
            info.setName("Name" + i);
            info.setAlias("Alias" + i);
            info.setEcotype("Ecotype" + i);
            info.setHomozygosity("homozygot" + i);
            info.setParents("parent" + i);
            info.setPlantType(ArabidopsisSeedInfo.PlantType.OTHER);
            entryList.add(info);
        }

        BulkUploadInfo created = controller.createBulkImportDraft(account, EntryAddType.ARABIDOPSIS, "My Test",
                                                                  entryList);
        Assert.assertNotNull(created);

        BulkUploadInfo retrieved = controller.retrieveById(account, created.getId());
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(created.getName(), retrieved.getName());
        Assert.assertEquals(created.getCount(), retrieved.getCount());
        Assert.assertEquals(created.getId(), retrieved.getId());
        Assert.assertEquals(created.getCreated(), retrieved.getCreated());
        Assert.assertEquals(created.getLastUpdate(), retrieved.getLastUpdate());
    }

    @Test
    public void testRetrieveByUser() throws Exception {
        AccountController accountController = new AccountController();
        Account adminAccount = accountController.createAdminAccount("tester+retrieveByUser@test.org", "popop");
        Assert.assertNotNull(adminAccount);
        accountController.createNewAccount("", "TESTER", "", "regular+retrieveByUser@test.org", "LBL", "");

        Account account = accountController.getByEmail("tester+retrieveByUser@test.org");
        try {
            controller.retrieveById(account, 1);
        } catch (ControllerException ce) {
        }

        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        final int count = 10;
        for (int i = 0; i < count; i += 1) {
            PlasmidInfo info = new PlasmidInfo();
            info.setName("Name" + i);
            info.setAlias("Alias" + i);
            info.setCircular(i % 2 == 0);
            entryList.add(info);
        }

        BulkUploadInfo created = controller.createBulkImportDraft(account,
                                                                  EntryAddType.PLASMID,
                                                                  "My Plasmid Test",
                                                                  entryList);
        Assert.assertNotNull(created);
        ArrayList<BulkUploadInfo> user = controller.retrieveByUser(account, account);
        Assert.assertNotNull(user);
        Assert.assertEquals(1, user.size());
        Assert.assertEquals(count, user.get(0).getCount());
    }

    @Test
    public void testDeleteDraftById() throws Exception {

    }

    @Test
    public void testCreateBulkImportDraft() throws Exception {
        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", "tester@test_CreateBulkImportDraft.org",
                                                             "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail("tester@test.org");
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount("tester+admin@test_CreateBulkImportDraft.org",
                                                                    "popop");
        Assert.assertNotNull(adminAccount);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList);
        Assert.assertNotNull(createdDraft);
    }

    @Test
    public void testUpdateBulkImportDraft() throws Exception {
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", "tester@test_UpdateBulkImportDraft.org",
                                                             "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail("tester@test_UpdateBulkImportDraft.org");
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount("tester+admin_UpdateBulkImportDraft@test.org",
                                                                    "popop");
        Assert.assertNotNull(adminAccount);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList);

        // update to see behavior (nothing has changed)
        BulkUploadInfo updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertEquals("Updated draft has a different id from existing", createdDraft.getId(),
                            updatedDraft.getId());

        // actually update with entry (part)
        EntryInfo info = new PartInfo();
        info.setAlias("alias");
        entryList.add(info);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertTrue(updatedDraft.getCount() == 1);

        // retrieve updated
        updatedDraft = controller.retrieveById(account, createdDraft.getId());
        EntryInfo added = updatedDraft.getEntryList().get(0);
        long addedId = added.getId();
        Assert.assertNotNull(added);

        // update existing change value
        entryList = updatedDraft.getEntryList();
        added.setAlias("new alias");
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertTrue(updatedDraft.getCount() == 1);
        Assert.assertEquals(addedId, updatedDraft.getEntryList().get(0).getId());

        // check the id of the updated
        updatedDraft = controller.retrieveById(account, updatedDraft.getId());
        Assert.assertTrue(updatedDraft.getCount() == 1);
        Assert.assertEquals(addedId, updatedDraft.getEntryList().get(0).getId());

        EntryController entryController = new EntryController();
        Entry newEntry = entryController.get(account, addedId);
        Assert.assertNotNull(newEntry);
        Assert.assertEquals(Visibility.DRAFT.getValue(), newEntry.getVisibility().intValue());
        Assert.assertEquals(newEntry.getAlias(), "new alias"); // check updated name

        // update existing and add one more
        added.setName("Part Test");
        EntryInfo newInfo = new PartInfo();
        newInfo.setLongDescription("This is a long description");
        entryList = updatedDraft.getEntryList();
        entryList.add(newInfo);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList);
        Assert.assertNotNull(updatedDraft);
    }

    @Test
    public void testSubmitBulkImportForVerification() throws Exception {

    }

    @Test
    public void testApproveBulkImport() throws Exception {

    }
}
