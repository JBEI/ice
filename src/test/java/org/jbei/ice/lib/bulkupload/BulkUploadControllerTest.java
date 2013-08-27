package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.AccountCreator;
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
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
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
        Account account = AccountCreator.createTestAccount("testRetrievePendingImports", true);
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
        Account account = AccountCreator.createTestAccount("testRetrieveById", false);
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
        Account account = AccountCreator.createTestAccount("testRetrieveByUser", false);
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

        Account account2 = AccountCreator.createTestAccount("testRetrieveByUser2", false);
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
        Account account = AccountCreator.createTestAccount("testDeleteDraftById", false);
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
        Account account = AccountCreator.createTestAccount("testAutoUpdateBulkUpload", false);
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
        Account account = AccountCreator.createTestAccount("testSubmitBulkImportDraft", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0001");
        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.STRAIN);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // check that the entry has been created and has visibility of draft
        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Assert.assertNotNull(entry);
        Assert.assertEquals("JBEI-0001", entry.getName());
        Assert.assertTrue(entry.getVisibility().equals(Integer.valueOf(Visibility.DRAFT.getValue())));


        // check that the bulk upload has been created
        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);

        // try to submit. should be rejected because the required fields are not present
        Assert.assertFalse(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));

        // enter information for others
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryField.PI, "test");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "select");

        // use preference for the status
        PreferenceInfo preference = new PreferenceInfo();
        preference.setAdd(true);
        preference.setKey(EntryField.STATUS.toString());
        preference.setValue("Complete");

        long id = controller.updatePreference(account, autoUpdate.getBulkUploadId(), EntryAddType.STRAIN, preference);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

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
        Assert.assertTrue(info.getEntryList().get(0).getVisibility().equals(Visibility.PENDING));

        // check the data associated with it
        PartData entryInfo = info.getEntryList().get(0);
        Assert.assertEquals(entryInfo.getName(), "JBEI-0001");
        Assert.assertEquals(entryInfo.getShortDescription(), "this is a test");
        Assert.assertEquals(entryInfo.getPrincipalInvestigator(), "test");
        Assert.assertEquals(entryInfo.getSelectionMarkers(), "select");
        Assert.assertEquals(entryInfo.getBioSafetyLevel(), new Integer(BioSafetyOption.LEVEL_TWO.getValue()));
        Assert.assertEquals(entryInfo.getStatus(), "Complete");
    }

    @Test
    public void testRevertSubmitted() throws Exception {
        Account account = AccountCreator.createTestAccount("testRevertSubmitted", false);
        Account admin = AccountCreator.createTestAccount("testRevertSubmitted+Admin", true);
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
        Account account = AccountCreator.createTestAccount("testApproveBulkImport", true);
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
        Assert.assertEquals("JBEI-0001", entry.getName());

        // test strain with plasmid
        autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.NAME, "JBEI-0002");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "strain for plasmid");
        autoUpdate.getKeyValue().put(EntryField.PI, "nathan hillson");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.IN_PROGRESS.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_ONE.getValue());
        autoUpdate.getKeyValue().put(EntryField.STRAIN_SELECTION_MARKERS, "select");
        autoUpdate.getKeyValue().put(EntryField.PLASMID_NAME, "pst100");
        autoUpdate.getKeyValue().put(EntryField.PLASMID_SUMMARY, "plasmid for strain");
        autoUpdate.getKeyValue().put(EntryField.PLASMID_SELECTION_MARKERS, "plasmid select");

        autoUpdate = controller.autoUpdateBulkUpload(account, autoUpdate, EntryAddType.STRAIN_WITH_PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        long id = autoUpdate.getEntryId();
        Entry strain = entryController.get(account, id);
        Set<Entry> linked = strain.getLinkedEntries();
        Assert.assertEquals(1, linked.size());

        Entry plasmid = (Entry) linked.toArray()[0];
        Assert.assertTrue(strain.getVisibility() == plasmid.getVisibility() &&
                                  plasmid.getVisibility() == Visibility.DRAFT.getValue());

        Assert.assertTrue(controller.submitBulkImportDraft(account, autoUpdate.getBulkUploadId()));
        strain = entryController.get(account, id);
        linked = strain.getLinkedEntries();
        plasmid = (Entry) linked.toArray()[0];
        Assert.assertTrue(strain.getVisibility() == plasmid.getVisibility() &&
                                  plasmid.getVisibility() == Visibility.PENDING.getValue());

        Assert.assertTrue(controller.approveBulkImport(account, autoUpdate.getBulkUploadId()));

        strain = entryController.get(account, id);
        linked = strain.getLinkedEntries();
        plasmid = (Entry) linked.toArray()[0];
        Assert.assertTrue(strain.getVisibility() == plasmid.getVisibility() &&
                                  plasmid.getVisibility() == Visibility.OK.getValue());
    }

    @Test
    public void testRenameDraft() throws Exception {
        Account account = AccountCreator.createTestAccount("testRenameDraft", false);
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
        Account account = AccountCreator.createTestAccount("testUpdatePreference", false);
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
        Account account = AccountCreator.createTestAccount("testUpdatePermissions", true);
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
        UserGroup userGroup1 = new UserGroup();
        userGroup1.setType(GroupType.PRIVATE);
        userGroup1.setDescription("test group");
        userGroup1.setLabel("test");
        userGroup1 = groupController.createGroup(account, userGroup1);
        Assert.assertNotNull(userGroup1);
        Assert.assertTrue(userGroup1.getId() > 0);

        UserGroup userGroup2 = new UserGroup();
        userGroup2.setType(GroupType.PRIVATE);
        userGroup2.setDescription("test group2");
        userGroup2.setLabel("test2");
        userGroup2 = groupController.createGroup(account, userGroup2);
        Assert.assertNotNull(userGroup2);
        Assert.assertTrue(userGroup2.getId() > 0);
        Assert.assertTrue(userGroup2.getId() != userGroup1.getId());

        BulkUploadInfo info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getAccessPermissions().isEmpty());

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

        // add permission for group 2
        AccessPermission accessPermission = new AccessPermission();
        accessPermission.setArticle(AccessPermission.Article.GROUP);
        accessPermission.setType(AccessPermission.Type.READ_ENTRY);
        accessPermission.setArticleId(userGroup2.getId());
        accessPermissions.add(accessPermission);
        long id = controller.updatePermissions(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID,
                                               accessPermissions);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

        // verify that permissions have been added
        info = controller.retrieveById(account, autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getAccessPermissions().size() == 1);

        // check actual permission
        accessPermission = info.getAccessPermissions().get(0);
        Assert.assertEquals(accessPermission.getArticleId(), userGroup2.getId());

        // change permission to group 1
        accessPermission = new AccessPermission();
        accessPermission.setArticleId(userGroup1.getId());
        accessPermission.setArticle(AccessPermission.Article.GROUP);
        accessPermission.setType(AccessPermission.Type.READ_ENTRY);
        accessPermissions.clear();
        accessPermissions.add(accessPermission);
        id = controller.updatePermissions(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID,
                                          accessPermissions);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

        // verify that permissions have been changed
        info = controller.retrieveById(account, id, 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getAccessPermissions().size() == 1);

        // check actual permission
        accessPermission = info.getAccessPermissions().get(0);
        Assert.assertEquals(accessPermission.getArticleId(), userGroup1.getId());

        // change permission to both
        accessPermissions.clear();
        AccessPermission accessPermission1 = new AccessPermission();
        accessPermission1.setArticle(AccessPermission.Article.GROUP);
        accessPermission1.setArticleId(userGroup1.getId());
        accessPermission1.setType(AccessPermission.Type.READ_ENTRY);
        accessPermissions.add(accessPermission1);

        AccessPermission accessPermission2 = new AccessPermission();
        accessPermission2.setArticleId(userGroup2.getId());
        accessPermission2.setArticle(AccessPermission.Article.GROUP);
        accessPermission2.setType(AccessPermission.Type.READ_ENTRY);
        accessPermissions.add(accessPermission2);

        id = controller.updatePermissions(account, autoUpdate.getBulkUploadId(), EntryAddType.PLASMID,
                                          accessPermissions);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), id);

        // verify that permissions have been changed
        info = controller.retrieveById(account, id, 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getAccessPermissions().size() == 2);

        // approve the bulk upload
        Assert.assertTrue(controller.approveBulkImport(account, autoUpdate.getBulkUploadId()));

        EntryController entryController = new EntryController();
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Assert.assertNotNull(entry);
        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());

        // check the entry permissions to ensure they are correct
        Set<Permission> entryPermissions = entry.getPermissions();
        for (AccessPermission ap : accessPermissions) {
            boolean found = false;
            for (Permission entryPermission : entryPermissions) {
                if (entryPermission.getGroup() != null
                        && entryPermission.getGroup().getId() == ap.getArticleId()
                        && entryPermission.isCanRead()) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Permissions for bulk upload were not propagated to the entry", found);
        }
    }
}
