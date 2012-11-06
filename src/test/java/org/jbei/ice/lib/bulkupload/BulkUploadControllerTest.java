package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;
import org.jbei.ice.shared.dto.Visibility;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

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
        Account adminAccount = accountController.createAdminAccount();
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
        boolean success = controller.submitBulkImport(account, EntryAddType.ARABIDOPSIS, entryList, "");
        Assert.assertTrue("Could not submit bulk import", success);

        // 1 pending
        infos = controller.retrievePendingImports(adminAccount);
        Assert.assertNotNull(infos);
        Assert.assertEquals(1, infos.size());
        Assert.assertEquals(count, infos.get(0).getCount());
    }

    @Test
    public void comprehensiveTest() throws Exception {

        final String email = "test_Comprehensive@test.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount();
        Assert.assertNotNull(adminAccount);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList,
                                                                       "");
        Assert.assertNotNull(createdDraft);

        // update to see behavior (nothing has changed)
        BulkUploadInfo updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertEquals("Updated draft has a different id from existing", createdDraft.getId(),
                            updatedDraft.getId());

        // actually update with entry (part)
        EntryInfo info = new PartInfo();
        info.setAlias("alias");
        entryList.add(info);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertTrue(updatedDraft.getCount() == 1);

        // retrieve updated
        updatedDraft = controller.retrieveById(account, createdDraft.getId());
        EntryInfo added = updatedDraft.getEntryList().get(0);
        long addedId = added.getId();
        Assert.assertNotNull(added);

        // update existing change value
        entryList = updatedDraft.getEntryList();
        added.setAlias("new alias");
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
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
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertNotNull(updatedDraft);

        ArrayList<Long> allEntries = entryController.getEntryIdsByOwner(account, account.getEmail(), Visibility.DRAFT);
        Assert.assertNotNull(allEntries);
        Assert.assertEquals(2, allEntries.size());

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
                verify.getEntryList()), "");
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
        Assert.assertTrue(controller.approveBulkImport(adminAccount, verify.getId(), verify.getEntryList(), ""));

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
        Account adminAccount = accountController.createAdminAccount();
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
                                                                  entryList, "");
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
        Account adminAccount = accountController.createAdminAccount();
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
                                                                  entryList, "");
        Assert.assertNotNull(created);
        ArrayList<BulkUploadInfo> user = controller.retrieveByUser(account, account);
        Assert.assertNotNull(user);
        Assert.assertEquals(1, user.size());
        Assert.assertEquals(count, user.get(0).getCount());
    }

    @Test
    public void testDeleteDraftById() throws Exception {

        final String email = "tester@test_DeleteDraftById.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.ARABIDOPSIS, "Test",
                                                                       entryList, "");
        Assert.assertNotNull(createdDraft);

        // save entry with draft
        ArabidopsisSeedInfo info = new ArabidopsisSeedInfo();
        info.setGeneration(ArabidopsisSeedInfo.Generation.M1);
        info.setName("Name");
        info.setAlias("Alias");
        info.setEcotype("Ecotype");
        info.setHomozygosity("homozygot");
        info.setParents("parent");
        info.setPlantType(ArabidopsisSeedInfo.PlantType.OTHER);
        entryList.add(info);
        BulkUploadInfo updatedBulk = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertNotNull(updatedBulk);
        Assert.assertEquals(1, updatedBulk.getCount());

        // retrieve entry associated with created draft
        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, updatedBulk.getEntryList().get(0).getId());
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getClass() == ArabidopsisSeed.class);

        // delete draft
        Assert.assertNotNull(controller.deleteDraftById(account, updatedBulk.getId()));

        // ensure contents are deleted
        try {
            controller.retrieveById(account, updatedBulk.getId());
        } catch (ControllerException c) {
            // expected
        }

        // ensure no entries left "hanging"
//        entry = entryController.get(account, entry.getId());
//        Assert.assertNull(entry);

        // test delete with valid group id
        GroupController groupController = new GroupController();
        Group publicGroup = groupController.create("delete_DRAFT", "TEST", null);
        Assert.assertNotNull(publicGroup);

        entryList.clear();
        entryList.add(info);
        createdDraft = controller.createBulkImportDraft(account, EntryAddType.ARABIDOPSIS, "Test",
                                                        entryList, publicGroup.getUuid());

        Assert.assertNotNull(createdDraft);

        entry = entryController.get(account, createdDraft.getEntryList().get(0).getId());
        Assert.assertNotNull(entry);

        final String assistantTester = "assistantTester@TEST";
        accountController.createNewAccount("", "TESTER", "", assistantTester, "LBL", "");
        Account assistant = accountController.getByEmail(assistantTester);
        Assert.assertNotNull(assistant);

        // add write permission
        PermissionsController permissionsController = new PermissionsController();
        Assert.assertFalse(permissionsController.hasReadPermission(assistant, entry));
        permissionsController.addReadGroup(account, entry, publicGroup);

        assistant.getGroups().add(publicGroup);
        accountController.save(assistant);

        Set<Long> set = entryController.getAllVisibleEntryIDs(assistant);
        ArrayList<Entry> setEntries = entryController.getEntriesByIdSet(account, new ArrayList<Long>(set));
        Assert.assertEquals(set.size(), setEntries.size());


        for (Entry entry1 : setEntries) {
            Assert.assertFalse(entry1.getVisibility() == Visibility.DRAFT.getValue());
        }

        // delete draft
        Assert.assertNotNull(controller.deleteDraftById(account, createdDraft.getId()));

        // ensure contents are deleted
        try {
            controller.retrieveById(account, createdDraft.getId());
        } catch (ControllerException c) {
            // expected
        }

        // ensure no entries left "hanging"
//        entry = entryController.get(account, entry.getId());
//        Assert.assertNull(entry);
    }

    @Test
    public void testCreateBulkImportDraft() throws Exception {

        final String email = "tester@test_CreateBulkImportDraft.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount();
        Assert.assertNotNull(adminAccount);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        PartInfo partInfo = new PartInfo();
        partInfo.setPrincipalInvestigator("test_PI");
        entryList.add(partInfo);

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList,
                                                                       "");
        Assert.assertNotNull(createdDraft);
        Assert.assertEquals(entryList.size(), createdDraft.getCount());
        Assert.assertEquals(entryList.size(), createdDraft.getEntryList().size());
        Assert.assertEquals(partInfo.getPrincipalInvestigator(), createdDraft.getEntryList().get(0)
                                                                             .getPrincipalInvestigator());
        Assert.assertEquals(account.getEmail(), createdDraft.getEntryList().get(0).getOwnerEmail());
        Assert.assertEquals(account.getFullName(), createdDraft.getEntryList().get(0).getOwner());

        createdDraft = controller.retrieveById(account, createdDraft.getId());
        Assert.assertNotNull(createdDraft);
        Assert.assertEquals(entryList.size(), createdDraft.getCount());
        Assert.assertEquals(entryList.size(), createdDraft.getEntryList().size());
        Assert.assertEquals(partInfo.getPrincipalInvestigator(), createdDraft.getEntryList().get(0)
                                                                             .getPrincipalInvestigator());
        Assert.assertEquals(account.getEmail(), createdDraft.getEntryList().get(0).getOwnerEmail());
        Assert.assertEquals(account.getFullName(), createdDraft.getEntryList().get(0).getOwner());
    }

    @Test
    public void testUpdateBulkImportDraft() throws Exception {

        final String email = "tester@test_UpdateBulkImportDraft.org";

        // create
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount();
        Assert.assertNotNull(adminAccount);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList,
                                                                       "");

        // update to see behavior (nothing has changed)
        BulkUploadInfo updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertEquals("Updated draft has a different id from existing", createdDraft.getId(),
                            updatedDraft.getId());

        // actually update with entry (part)
        EntryInfo info = new PartInfo();
        info.setAlias("alias");
        entryList.add(info);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertTrue(updatedDraft.getCount() == 1);
        Assert.assertEquals(info.getAlias(), updatedDraft.getEntryList().get(0).getAlias());

        // retrieve updated
        updatedDraft = controller.retrieveById(account, createdDraft.getId());
        EntryInfo added = updatedDraft.getEntryList().get(0);
        long addedId = added.getId();
        Assert.assertNotNull(added);

        // update existing change value
        entryList = updatedDraft.getEntryList();
        added.setAlias("new alias");
        added.setFundingSource("no funds");
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, "");
        Assert.assertEquals(1, updatedDraft.getCount());
        Assert.assertEquals(addedId, updatedDraft.getEntryList().get(0).getId());

        // check the id of the updated
        updatedDraft = controller.retrieveById(account, updatedDraft.getId());
        Assert.assertTrue(updatedDraft.getCount() == 1);
        Assert.assertEquals(addedId, updatedDraft.getEntryList().get(0).getId());

        // retrieve the entry
        EntryController entryController = new EntryController();
        Entry newEntry = entryController.get(account, addedId);
        Assert.assertNotNull(newEntry);
        Assert.assertEquals(Visibility.DRAFT.getValue(), newEntry.getVisibility().intValue());
        Assert.assertEquals(newEntry.getAlias(), "new alias"); // check updated name

        // check group
        PermissionsController permissionsController = new PermissionsController();
        ArrayList<PermissionInfo> permissionInfos = permissionsController.retrieveSetEntryPermissions(account,
                                                                                                      newEntry);
        Assert.assertTrue(permissionInfos.isEmpty());

        // update existing and add one more and also set the group to public
        GroupController groupController = new GroupController();
        Group group = groupController.create("delete_UPDATE", "TEST", null);
        added.setName("Part Test");
        EntryInfo newInfo = new PartInfo();
        newInfo.setLongDescription("This is a long description");
        entryList = updatedDraft.getEntryList();
        entryList.add(newInfo);
        updatedDraft = controller.updateBulkImportDraft(account, createdDraft.getId(), entryList, group.getUuid());
        Assert.assertNotNull(updatedDraft);
        Assert.assertEquals(group.getUuid(), updatedDraft.getGroupInfo().getUuid());

        // both entries should have same permissions
        newEntry = entryController.get(account, addedId);
        long id = updatedDraft.getEntryList().get(0).getId();
        Entry second = entryController.get(account, id);
        permissionInfos = permissionsController.retrieveSetEntryPermissions(account, newEntry);
        Assert.assertEquals(1, permissionInfos.size());
        Assert.assertEquals(PermissionInfo.PermissionType.READ_GROUP, permissionInfos.get(0).getType());
        permissionInfos = permissionsController.retrieveSetEntryPermissions(account, second);
        Assert.assertEquals(1, permissionInfos.size());
        Assert.assertEquals(PermissionInfo.PermissionType.READ_GROUP, permissionInfos.get(0).getType());
    }

    @Test
    public void testSubmitBulkImportDraft() throws Exception {
        // test submission of a saved draft

        // create accounts
        final String email = "tester@test_SubmitBulkImportDraft.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount();
        Assert.assertNotNull(adminAccount);

        // create bulk import (with strain with plasmid)
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        StrainInfo strainInfo = new StrainInfo();
        strainInfo.setGenotypePhenotype("test");
        strainInfo.setAlias("alias");
        strainInfo.setHost("A host");
        PlasmidInfo plasmidInfo = new PlasmidInfo();
        strainInfo.setInfo(plasmidInfo);
        plasmidInfo.setCircular(true);
        plasmidInfo.setBackbone("jawbone");
        plasmidInfo.setPromoters("+1AwesomePOWA");
        plasmidInfo.setPartId("ptsN");
        plasmidInfo.setName("partINPlas");

        entryList.add(strainInfo);

        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account,
                                                                       EntryAddType.STRAIN_WITH_PLASMID,
                                                                       "Test",
                                                                       entryList, "");
        Assert.assertNotNull(createdDraft);
        strainInfo = (StrainInfo) createdDraft.getEntryList().get(0);
        plasmidInfo = (PlasmidInfo) strainInfo.getInfo();
        Assert.assertNotNull(strainInfo);
        Assert.assertNotNull(plasmidInfo);
        Assert.assertEquals(1, createdDraft.getCount());
        Assert.assertEquals(createdDraft.getCount(), createdDraft.getEntryList().size());
        Assert.assertEquals(Visibility.DRAFT, strainInfo.getVisibility());
        Assert.assertEquals(Visibility.DRAFT, plasmidInfo.getVisibility());

        createdDraft = controller.retrieveById(account, createdDraft.getId());

        Assert.assertNotNull(createdDraft);
        strainInfo = (StrainInfo) createdDraft.getEntryList().get(0);
        plasmidInfo = (PlasmidInfo) strainInfo.getInfo();
        Assert.assertNotNull(strainInfo);
        Assert.assertNotNull(plasmidInfo);
        Assert.assertEquals(1, createdDraft.getCount());
        Assert.assertEquals(createdDraft.getCount(), createdDraft.getEntryList().size());

        Assert.assertEquals(Visibility.DRAFT, strainInfo.getVisibility());
        Assert.assertEquals(Visibility.DRAFT, plasmidInfo.getVisibility());

        // submit bulk import
        Assert.assertTrue("failed to submit bulk import draft",
                          controller.submitBulkImportDraft(account, createdDraft.getId(), createdDraft.getEntryList(),
                                                           ""));

        // check entry visibility is pending
        EntryController entryController = new EntryController();
        Entry strain = entryController.get(account, strainInfo.getId());
        Entry plasmid = entryController.get(account, plasmidInfo.getId());

        Assert.assertNotNull(strain);
        Assert.assertNotNull(plasmid);

        Assert.assertEquals(Visibility.PENDING.getValue(), strain.getVisibility().intValue());
        Assert.assertEquals(Visibility.PENDING.getValue(), plasmid.getVisibility().intValue());

        // check draft
        createdDraft = controller.retrieveById(adminAccount, createdDraft.getId());
        Assert.assertEquals(account.getEmail(), createdDraft.getName());
    }

    @Test
    public void testSubmitBulkImport() throws Exception {
        // create accounts
        final String email = "tester@test_SubmitBulkImport.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        // create entries
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        EntryInfo info = new PartInfo();
        info.setAlias("alias");
        entryList.add(info);

        // submit bulk import
        Assert.assertTrue("failed to submit bulk import draft",
                          controller.submitBulkImport(account, EntryAddType.PART, entryList, ""));

        // check entry visibility is pending
        EntryController entryController = new EntryController();
        ArrayList<Long> results = entryController.getEntryIdsByOwner(account, account.getEmail(), Visibility.PENDING);
        Assert.assertNotNull(results);
        Assert.assertEquals(entryList.size(), results.size());

        Entry entry = entryController.get(account, results.get(0));
        Assert.assertNotNull(entry);
        Assert.assertEquals("alias", entry.getAlias());

        // user should not have any bulk imports
        ArrayList<BulkUploadInfo> infos = controller.retrieveByUser(account, account);
        Assert.assertNotNull(infos);
        Assert.assertEquals(0, infos.size());
    }

    @Test
    public void testApproveBulkImport() throws Exception {

        // create accounts
        final String email = "tester@test_ApproveBulkImport.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount();
        Assert.assertNotNull(adminAccount);

        // create bulk import
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        EntryInfo info = new PartInfo();
        info.setAlias("alias");
        entryList.add(info);
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(account, EntryAddType.PART, "Test", entryList,
                                                                       "");
        Assert.assertNotNull(createdDraft);
        Assert.assertEquals(1, createdDraft.getCount());
        Assert.assertEquals(createdDraft.getCount(), createdDraft.getEntryList().size());

        // check entry visibility is draft
        EntryInfo partInfo = createdDraft.getEntryList().get(0);
        Assert.assertEquals(partInfo.getType(), EntryType.PART);
        Assert.assertEquals(Visibility.DRAFT, partInfo.getVisibility());

        // submit bulk import
        Assert.assertTrue("failed to submit bulk import draft",
                          controller.submitBulkImportDraft(account, createdDraft.getId(), createdDraft.getEntryList(),
                                                           ""));

        // check entry visibility is pending
        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, partInfo.getId());
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getId() == partInfo.getId());
        Assert.assertEquals(Visibility.PENDING.getValue(), entry.getVisibility().intValue());

        // retrieve bulk import draft
        createdDraft = controller.retrieveById(adminAccount, createdDraft.getId());
        Assert.assertNotNull(createdDraft);
        partInfo = createdDraft.getEntryList().get(0);
        Assert.assertEquals(partInfo.getType(), EntryType.PART);
        Assert.assertEquals("alias", partInfo.getAlias());

        // update
        partInfo.setAlias("alias+updated");

        // try to approve bulk import with regular account
        try {
            controller.approveBulkImport(account, createdDraft.getId(), createdDraft.getEntryList(), "");
        } catch (PermissionException pe) {
            // expected
        }

        boolean approved = controller.approveBulkImport(adminAccount,
                                                        createdDraft.getId(),
                                                        createdDraft.getEntryList(), "");
        Assert.assertTrue("Failed to approved bulk upload", approved);

        // verify that record does not exist anymore
        try {
            controller.retrieveById(account, createdDraft.getId());
        } catch (ControllerException ce) {
            // expected
        }

        // check entry visibility is ok
        entry = entryController.get(account, entry.getId());
        Assert.assertNotNull(entry);
        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());
        Assert.assertEquals(account.getEmail(), entry.getOwnerEmail());
        Assert.assertEquals("alias+updated", entry.getAlias());
    }

    @Test
    public void testCreateBulkImportDraftWithStrainWithPlasmid() throws Exception {
        final String email = "tester@test_CreateBulkImportDraftWithStrainWithPlasmid.org";

        // create accounts
        AccountController accountController = new AccountController();
        String password = accountController.createNewAccount("", "TESTER", "", email, "LBL", "");
        Assert.assertNotNull(password);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account adminAccount = accountController.createAdminAccount();
        Assert.assertNotNull(adminAccount);

        // starting with clean slate now
        ArrayList<EntryInfo> entryList = new ArrayList<EntryInfo>();
        EntryInfo strainInfo = new StrainInfo();
        strainInfo.setPrincipalInvestigator("pi_Test");
        EntryInfo plasmidInfo = new PlasmidInfo();
        plasmidInfo.setPrincipalInvestigator("pi_Test");
        strainInfo.setInfo(plasmidInfo);
        entryList.add(strainInfo);

        // create draft with no entries
        BulkUploadInfo createdDraft = controller.createBulkImportDraft(
                account, EntryAddType.STRAIN_WITH_PLASMID, "Test", entryList, "");
        Assert.assertNotNull(createdDraft);
    }
}
