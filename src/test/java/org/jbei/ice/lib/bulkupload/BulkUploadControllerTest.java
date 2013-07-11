package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.StatusType;
import org.jbei.ice.lib.shared.dto.AccountInfo;
import org.jbei.ice.lib.shared.dto.AccountType;
import org.jbei.ice.lib.shared.dto.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.Visibility;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.group.GroupInfo;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class BulkUploadControllerTest {

    private BulkUploadController controller;

    @BeforeClass
    public static void init() {
        HibernateHelper.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateHelper.beginTransaction();
        controller = new BulkUploadController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testRetrievePendingImports() throws Exception {
        Account account = createTestAccount("testRetrievePendingImports", true);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);
        autoUpdate.setRow(0);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "select");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // check that the bulk upload has been created
        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull("Null bulk upload", info);

        Assert.assertTrue("Submitting draft", controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));

        // entries associated with bulk upload must be pending
        info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 10);
        Assert.assertNotNull(info);
        Assert.assertTrue("Invalid entry count", info.getEntryList().size() == 1);
        Assert.assertTrue(info.getEntryList().get(0).getVisibility() == Visibility.PENDING);

        ArrayList<BulkUploadInfo> pending = controller.retrievePendingImports(account);
        Assert.assertNotNull("Null pending import", pending);
        boolean b = false;
        for (BulkUploadInfo uploadInfo : pending) {
            if (uploadInfo.getAccount().getEmail().equals(account.getEmail())) {
                b = true;
                break;
            }
        }
        Assert.assertTrue(b);
    }

    @Test
    public void testRetrieveById() throws Exception {
        Account account = createTestAccount("testRetrieveById", false);
        Assert.assertNull(controller.retrieveById(account, 100l, 0, 1));

        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "select");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // check that the bulk upload has been created
        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertEquals(0, info.getEntryList().size());
        info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 10);
        Assert.assertNotNull(info);
        Assert.assertEquals(1, info.getEntryList().size());
    }

    @Test
    public void testRetrieveByUser() throws Exception {
        Account account = createTestAccount("testRetrieveByUser", false);
        ArrayList<BulkUploadInfo> results = controller.retrieveByUser(account, account);
        Assert.assertEquals(0, results.size());

        // create strain with plasmid
        EntryAddType type = EntryAddType.STRAIN_WITH_PLASMID;
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.PLASMID_NAME, "strainPlasmid");
        autoUpdate.getKeyValue().put(EntryField.PLASMID_SUMMARY, "strainPlasmidSummary");
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, "Level 2");
        autoUpdate.getKeyValue().put(EntryField.PLASMID_STATUS, "In Progress");
        autoUpdate.getKeyValue().put(EntryField.PI, "Principal Investigator");
        autoUpdate.getKeyValue().put(EntryField.STRAIN_NAME, "strain");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "strain summary");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "strain selection markers");
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, type);
        Assert.assertNotNull(autoUpdate);
        ArrayList<BulkUploadInfo> userUpload = controller.retrieveByUser(account, account);
        Assert.assertNotNull(userUpload);
        Assert.assertEquals(1, userUpload.size());

        Account account2 = createTestAccount("testRetrieveByUser2", false);
        int count = 10;
        for (int i = 0; i < count; i += 1) {
            autoUpdate = new BulkUploadAutoUpdate(EntryType.PART);
            autoUpdate.getKeyValue().put(EntryField.NAME, "Name" + i);
            autoUpdate.getKeyValue().put(EntryField.PI, "PI" + i);
            autoUpdate.getKeyValue().put(EntryField.SUMMARY, "Summary" + i);
            if (i % 2 == 0)
                Assert.assertNotNull(controller.autoUpdateBulkUpload(account2, autoUpdate, EntryAddType.PART));
            else
                Assert.assertNotNull(controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PART));
        }
        userUpload = controller.retrieveByUser(account2, account2);
        Assert.assertEquals(count / 2, userUpload.size());
        userUpload = controller.retrieveByUser(account, account);
        Assert.assertEquals(count / 2 + 1, userUpload.size());
    }

    @Test
    public void testDeleteDraftById() throws Exception {
        Account account = createTestAccount("testDeleteDraftById", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "plasmid summary");
        autoUpdate.getKeyValue().put(EntryField.NAME, "plasmid name");
        autoUpdate.getKeyValue().put(EntryField.PI, "plasmid principal investigator");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "plasmid select markers");
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PLASMID);
        Assert.assertNotNull(autoUpdate);
        ControllerFactory.getAccountController().createSystemAccount();
        BulkUploadInfo info = controller.deleteDraftById(account, autoUpdate.getBulkUploadId());
        Assert.assertNotNull(info);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), info.getId());
//        Assert.assertEquals(1, info.getCount());
        Assert.assertNull(controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0));
    }

    @Test
    public void testAutoUpdateBulkUpload() throws Exception {
        EntryAddType type = EntryAddType.STRAIN_WITH_PLASMID;
        Account account = createTestAccount("testAutoUpdateBulkUpload", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.LINKS, "google");

        // first auto update. expect it to create a new bulk upload and entry
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, type);
        Assert.assertNotNull(autoUpdate);
        long entryId = autoUpdate.getEntryId();
        long bulkId = autoUpdate.getBulkUploadId();
        Assert.assertTrue(entryId > 0);
        Assert.assertTrue(bulkId > 0);

        ControllerFactory.getAccountController().createSystemAccount();

        BulkUploadInfo bulkUploadInfo = controller.retrieveById(account, bulkId, 0, 1000);
        Assert.assertNotNull(bulkUploadInfo);

        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, entryId);
        Assert.assertNotNull(entry);
        Assert.assertNotNull(entry.getLinks());
        Assert.assertEquals(1, entry.getLinks().size());

        autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);

        // auto update: expect plasmid and bulk upload with no fields set
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, type);
        Assert.assertNotNull(autoUpdate);
        entryId = autoUpdate.getEntryId();
        bulkId = autoUpdate.getBulkUploadId();

        Assert.assertTrue(entryId > 0);
        Assert.assertTrue(bulkId > 0);

        entry = entryController.get(account, entryId);
        Assert.assertNotNull(entry);
    }

    @Test
    public void testSubmitBulkImportDraft() throws Exception {
        Account account = createTestAccount("testSubmitBulkImportDraft", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.STRAIN);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // check that the entry has been created
        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Assert.assertNotNull(entry);
        Assert.assertEquals("JBEI-0001", entry.getNamesAsString());

        // check that the bulk upload has been created
        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);

        // try to submit. should be rejected because the required fields are not present
        Assert.assertFalse(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));

        // enter information for others
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "select");
        autoUpdate.getKeyValue().put(EntryField.STATUS, "Complete");

        // validation should fail because of this
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, "BLS1");
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.STRAIN);
        Assert.assertFalse(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));

        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.STRAIN);

        Assert.assertTrue(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));

        // entries associated with bulk upload must be pending
        info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 10);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getEntryList().size() == 1);
        Assert.assertTrue(info.getEntryList().get(0).getVisibility() == Visibility.PENDING);

        // check the data associated with it
        EntryInfo entryInfo = info.getEntryList().get(0);
        Assert.assertEquals(entryInfo.getName(), "JBEI-0001");
        Assert.assertEquals(entryInfo.getShortDescription(), "this is a test");
        Assert.assertEquals(entryInfo.getPrincipalInvestigator(), "test");
    }

    @Test
    public void testRevertSubmitted() throws Exception {
        Account account = createTestAccount("testRevertSubmitted", false);
        Account admin = createTestAccount("testRevertSubmitted+Admin", true);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.ARABIDOPSIS);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.ARABIDOPSIS);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        Assert.assertNotNull(controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0));

        // try to revert. not submitted
        Assert.assertFalse(controller.revertSubmitted(admin, autoUpdate.getBulkUploadId()));
        Assert.assertTrue(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));
        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(controller.revertSubmitted(admin, autoUpdate.getBulkUploadId()));
    }

    @Test
    public void testApproveBulkImport() throws Exception {
        Account account = createTestAccount("testApproveBulkImport", true);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "select");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // set a preference for funding source
        PreferenceInfo preference = new PreferenceInfo(true, PreferenceKey.FUNDING_SOURCE.toString(), "JBEI");
        controller.updatePreference(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID, preference);

        // submit draft
        Assert.assertTrue(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));
        Assert.assertTrue(controller.approveBulkImport(account, autoUpdate.getBulkUploadId()));

        // bulk upload should be deleted
        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNull(info);

        // entry must still exist and have a visibility of OK
        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Assert.assertNotNull(entry);
        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());

        // check the set values of the entry (particularly the preferences)
        Assert.assertTrue(entry.getEntryFundingSources() != null && entry.getEntryFundingSources().size() == 1);
        EntryFundingSource fundingSource = (EntryFundingSource) entry.getEntryFundingSources().toArray()[0];
        Assert.assertEquals("test", fundingSource.getFundingSource().getPrincipalInvestigator());
        Assert.assertEquals("JBEI", fundingSource.getFundingSource().getFundingSource());
        Assert.assertEquals("JBEI-0001", entry.getNamesAsString());
    }

    @Test
    public void testRenameDraft() throws Exception {
        Account account = createTestAccount("testRenameDraft", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "selection");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.STRAIN_WITH_PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        long id = autoUpdate.getBulkUploadId();
        ControllerFactory.getAccountController().createSystemAccount();
        controller.renameDraft(account, id, "My draft");
        BulkUploadInfo info = controller.retrieveById(account, id, 0, 1000);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getName().equals("My draft"));
    }

    @Test
    public void testUpdatePreference() throws Exception {
        Account account = createTestAccount("testUpdatePreference", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.PART);
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PART);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);

        // update preference (adding all required via preferences only)
        PreferenceInfo preference = new PreferenceInfo(true, EntryField.PI.toString(), "Principal Investigator");
        controller.updatePreference(account, autoUpdate.getBulkUploadId(), EntryAddType.PART, preference);

        preference = new PreferenceInfo(true, EntryField.NAME.toString(), "JBEI-1000");
        controller.updatePreference(account, autoUpdate.getBulkUploadId(), EntryAddType.PART, preference);

        preference = new PreferenceInfo(true, EntryField.SUMMARY.toString(), "unit test summary");
        controller.updatePreference(account, autoUpdate.getBulkUploadId(), EntryAddType.PART, preference);

        Assert.assertTrue(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));
    }

    @Test
    public void testUpdatePermissions() throws Exception {
        Account account = createTestAccount("testUpdatePermissions", true);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "select");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // create test groups
        GroupController groupController = new GroupController();
        GroupInfo group1 = new GroupInfo();
        group1.setType(GroupType.PRIVATE);
        group1.setDescription("test group");
        group1.setLabel("test");
        group1 = groupController.createGroup(account, group1);
        Assert.assertNotNull(group1);
        Assert.assertTrue(group1.getId() > 0);

        GroupInfo group2 = new GroupInfo();
        group2.setType(GroupType.PRIVATE);
        group2.setDescription("test group2");
        group2.setLabel("test2");
        group2 = groupController.createGroup(account, group2);
        Assert.assertNotNull(group2);
        Assert.assertTrue(group2.getId() > 0);
        Assert.assertTrue(group2.getId() != group1.getId());

        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getPermissions().isEmpty());

        ArrayList<PermissionInfo> permissions = new ArrayList<>();

        // add permission for group 2
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.setArticle(PermissionInfo.Article.GROUP);
        permissionInfo.setType(PermissionInfo.Type.READ_ENTRY);
        permissionInfo.setArticleId(group2.getId());
        permissions.add(permissionInfo);
        long id = controller.updatePermissions(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID,
                                               permissions);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

        // verify that permissions have been added
        info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getPermissions().size() == 1);

        // check actual permission
        permissionInfo = info.getPermissions().get(0);
        Assert.assertEquals(permissionInfo.getArticleId(), group2.getId());

        // change permission to group 1
        permissionInfo = new PermissionInfo();
        permissionInfo.setArticleId(group1.getId());
        permissionInfo.setArticle(PermissionInfo.Article.GROUP);
        permissionInfo.setType(PermissionInfo.Type.READ_ENTRY);
        permissions.clear();
        permissions.add(permissionInfo);
        id = controller.updatePermissions(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID, permissions);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

        // verify that permissions have been changed
        info = controller.retrieveById(account, id, 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getPermissions().size() == 1);

        // check actual permission
        permissionInfo = info.getPermissions().get(0);
        Assert.assertEquals(permissionInfo.getArticleId(), group1.getId());

        // change permission to both
        permissions.clear();
        PermissionInfo permissionInfo1 = new PermissionInfo();
        permissionInfo1.setArticle(PermissionInfo.Article.GROUP);
        permissionInfo1.setArticleId(group1.getId());
        permissionInfo1.setType(PermissionInfo.Type.READ_ENTRY);
        permissions.add(permissionInfo1);

        PermissionInfo permissionInfo2 = new PermissionInfo();
        permissionInfo2.setArticleId(group2.getId());
        permissionInfo2.setArticle(PermissionInfo.Article.GROUP);
        permissionInfo2.setType(PermissionInfo.Type.READ_ENTRY);
        permissions.add(permissionInfo2);

        id = controller.updatePermissions(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID, permissions);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

        // verify that permissions have been changed
        info = controller.retrieveById(account, id, 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getPermissions().size() == 2);

        // approve the bulk upload
        Assert.assertTrue(controller.approveBulkImport(account, autoUpdate.getBulkUploadId()));

        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Assert.assertNotNull(entry);
        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());

        // check the entry permissions to ensure they are correct
        Set<Permission> entryPermissions = entry.getPermissions();
        for (PermissionInfo permission : permissions) {
            boolean found = false;
            for (Permission entryPermission : entryPermissions) {
                if (entryPermission.getGroup() != null
                        && entryPermission.getGroup().getId() == permission.getArticleId()
                        && entryPermission.isCanRead()) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Permissions for bulk upload were not propagated to the entry", found);
        }
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
