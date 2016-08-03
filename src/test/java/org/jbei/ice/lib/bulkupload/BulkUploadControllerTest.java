package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.StatusType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class BulkUploadControllerTest {

    private BulkUploadController controller;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new BulkUploadController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testCreate() throws Exception {
        Account account = AccountCreator.createTestAccount("testBulkUploadCreate", false);

        BulkUploadInfo info = new BulkUploadInfo();
        info.setName("testCreateName");
        info.setType(EntryType.PLASMID.getName());
        info.setAccount(account.toDataTransferObject());
        info = controller.create(account.getEmail(), info);
        Assert.assertNotNull(info);
    }

    @Test
    public void testGetBulkImport() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetBulkImport", false);
        BulkEntryCreator creator = new BulkEntryCreator();

        // create bulk upload
        long id = creator.createBulkUpload(account.getEmail(), EntryType.PART);
        Assert.assertTrue(id > 0);

        int count = 100;

        for (int i = 0; i < count; i += 1) {
            PartData partData = new PartData(EntryType.PLASMID);
            partData.setBioSafetyLevel(1);
            partData.setShortDescription("part description");
            partData.setName("part" + i);

            partData = creator.createEntry(account.getEmail(), id, partData);
            Assert.assertNotNull(partData);

            // add to bulk upload
        }

        BulkUploadInfo info = controller.getBulkImport(account.getEmail(), id, 0, 100);
        Assert.assertNotNull(info);
        Assert.assertEquals(info.getEntryList().size(), 100);

        // test retrieval in order
        for (int i = 0; i < 100; i += 10) {
            info = controller.getBulkImport(account.getEmail(), id, i, 10);
            Assert.assertNotNull(info);
            Assert.assertEquals(info.getEntryList().size(), 10);

            ArrayList<PartData> list = info.getEntryList();
            int j = i;
            for (PartData data : list) {
                Assert.assertEquals(data.getName(), "part" + j);
                j += 1;
            }
        }
    }

    @Test
    public void testRetrieveByUser() throws Exception {
        Account account = AccountCreator.createTestAccount("testRetrieveByUser", false);
        ArrayList<BulkUploadInfo> results = controller.retrieveByUser("testRetrieveByUser", "testRetrieveByUser");
        Assert.assertEquals(0, results.size());

        // create strain with plasmid
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.NAME, "strainPlasmid");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "strainPlasmidSummary");
        autoUpdate.getKeyValue().put(EntryField.BIO_SAFETY_LEVEL, "Level 2");
        autoUpdate.getKeyValue().put(EntryField.STATUS, "In Progress");
        autoUpdate.getKeyValue().put(EntryField.PI, "Principal Investigator");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "strain summary");
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "strain selection markers");
        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, EntryType.STRAIN);
        Assert.assertNotNull(autoUpdate);
        ArrayList<BulkUploadInfo> userUpload = controller.retrieveByUser(account.getEmail(), account.getEmail());
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
                Assert.assertNotNull(controller.autoUpdateBulkUpload(account2.getEmail(), autoUpdate,
                        EntryType.PART));
            else
                Assert.assertNotNull(controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate,
                        EntryType.PART));
        }
        userUpload = controller.retrieveByUser(account2.getEmail(), account2.getEmail());
        Assert.assertEquals(count / 2, userUpload.size());
        userUpload = controller.retrieveByUser(account.getEmail(), account.getEmail());
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
        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, EntryType.PLASMID);
        Assert.assertNotNull(autoUpdate);

        // delete bulk upload
        BulkUploadDeleteTask task = new BulkUploadDeleteTask(account.getEmail(), autoUpdate.getBulkUploadId());
        task.execute();
        Assert.assertNull(controller.getBulkImport(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0));
    }

    @Test
    public void testAutoUpdateBulkUpload() throws Exception {
        EntryType type = EntryType.STRAIN;
        Account account = AccountCreator.createTestAccount("testAutoUpdateBulkUpload", false);
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.LINKS, "google");

        // first auto update. expect it to create a new bulk upload and entry
        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, type);
        Assert.assertNotNull(autoUpdate);
        long entryId = autoUpdate.getEntryId();
        long bulkId = autoUpdate.getBulkUploadId();
        Assert.assertTrue(entryId > 0);
        Assert.assertTrue(bulkId > 0);

        BulkUploadInfo bulkUploadInfo = controller.getBulkImport(account.getEmail(), bulkId, 0, 1000);
        Assert.assertNotNull(bulkUploadInfo);

        EntryDAO dao = DAOFactory.getEntryDAO();
        Entry entry = dao.get(entryId);
        Assert.assertNotNull(entry);
        Assert.assertNotNull(entry.getLinks());
        Assert.assertEquals(1, entry.getLinks().size());

        autoUpdate = new BulkUploadAutoUpdate(EntryType.PLASMID);

        // auto update: expect plasmid and bulk upload with no fields set
        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, type);
        Assert.assertNotNull(autoUpdate);
        entryId = autoUpdate.getEntryId();
        bulkId = autoUpdate.getBulkUploadId();

        Assert.assertTrue(entryId > 0);
        Assert.assertTrue(bulkId > 0);

        entry = dao.get(entryId);
        Assert.assertNotNull(entry);
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
        autoUpdate.getKeyValue().put(EntryField.BIO_SAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());
        autoUpdate.getKeyValue().put(EntryField.SELECTION_MARKERS, "test");

        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, EntryType.ARABIDOPSIS);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        Assert.assertNotNull(controller.getBulkImport(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0));

        // try to revert. not submitted
        Assert.assertFalse(controller.revertSubmitted(admin, autoUpdate.getBulkUploadId()));

        // actual submission (update status)
        BulkEntryCreator bulkEntryCreator = new BulkEntryCreator();
        bulkEntryCreator.updateStatus(account.getEmail(), autoUpdate.getBulkUploadId(), BulkUploadStatus.PENDING_APPROVAL);
        BulkUploadInfo info = controller.getBulkImport(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(info);
        Assert.assertTrue(controller.revertSubmitted(admin, autoUpdate.getBulkUploadId()));
    }

    @Test
    public void testApproveBulkImport() throws Exception {
        Account account = AccountCreator.createTestAccount("testApproveBulkImport", true);

        //
        // test strain with plasmid
        //
        // create bulk upload draft
        String userId = account.getEmail();
        BulkUploadInfo testInfo = new BulkUploadInfo();
        testInfo.setName("testing");
        testInfo.setType(EntryType.STRAIN.getName());
        testInfo = controller.create(userId, testInfo);
        Assert.assertNotNull(testInfo);

        // create entry for upload
        BulkEntryCreator creator = new BulkEntryCreator();
        PartData strainData = new PartData(EntryType.STRAIN);
        strainData.setName("testStrain");
        ArrayList<String> selectionMarkers = new ArrayList<>();
        selectionMarkers.add("Spectinomycin");
        strainData.setSelectionMarkers(selectionMarkers);
        strainData.setBioSafetyLevel(1);
        strainData.setStatus("Complete");
        strainData.setShortDescription("testing bulk upload");
        strainData.setCreator(account.getFullName());
        strainData.setCreatorEmail(account.getEmail());
        strainData.setPrincipalInvestigator("PI");

        PartData plasmidData = new PartData(EntryType.PLASMID);
        plasmidData.setName("testPlasmid");
        selectionMarkers.clear();
        selectionMarkers.add("Spectinomycin");
        plasmidData.setSelectionMarkers(selectionMarkers);
        plasmidData.setBioSafetyLevel(1);
//        plasmidData.setStatus("In Progress");
        plasmidData.setShortDescription("testing bulk upload with strain with plasmid");
        plasmidData.setCreator(account.getFullName());
        plasmidData.setCreatorEmail(account.getEmail());
        plasmidData.setPrincipalInvestigator("PI");

        strainData.getLinkedParts().add(plasmidData);

        PartData returnStrainData = creator.createEntry(userId, testInfo.getId(), strainData);
        Assert.assertNotNull(returnStrainData);

        plasmidData.setStatus("In Progress");
        plasmidData = creator.updateEntry(userId, testInfo.getId(), returnStrainData.getLinkedParts().get(0).getId(), plasmidData);
        Assert.assertNotNull(plasmidData);
//        testInfo = controller.submitBulkImportDraft(userId, testInfo.getId());
//        Assert.assertNotNull(testInfo);
//        Assert.assertEquals(testInfo.getStatus(), BulkUploadStatus.PENDING_APPROVAL);
    }

    @Test
    public void testAddPermission() throws Exception {
        Account account = AccountCreator.createTestAccount("testAddPermission", false);
        BulkUploadInfo info = new BulkUploadInfo();
        info.setAccount(account.toDataTransferObject());
        info.setType(EntryType.PART.toString());
        BulkUploadInfo uploadInfo = controller.create(account.getEmail(), info);
        Assert.assertNotNull(uploadInfo);

        Account accountFriend = AccountCreator.createTestAccount("testAddPermission2", false);
        long id = uploadInfo.getId();
        AccessPermission permission = new AccessPermission();
        permission.setArticle(AccessPermission.Article.ACCOUNT);
        permission.setArticleId(accountFriend.getId());
        permission.setType(AccessPermission.Type.READ_UPLOAD);
        permission.setTypeId(id);

        controller.addPermission(account.getEmail(), id, permission);

        List<AccessPermission> permissions = controller.getUploadPermissions(account.getEmail(), id);
        Assert.assertNotNull(permissions);
        Assert.assertTrue(permissions.size() == 1);

        AccessPermission returnedPermission = permissions.get(0);
        Assert.assertEquals(returnedPermission.getArticle(), AccessPermission.Article.ACCOUNT);
        Assert.assertEquals(returnedPermission.getArticleId(), accountFriend.getId());
        Assert.assertEquals(returnedPermission.getType(), AccessPermission.Type.READ_UPLOAD);
        Assert.assertEquals(returnedPermission.getTypeId(), id);
    }

    @Test
    public void testDeletePermission() throws Exception {
        Account account = AccountCreator.createTestAccount("testDeletePermission", false);
        BulkUploadInfo info = new BulkUploadInfo();
        info.setAccount(account.toDataTransferObject());
        info.setType(EntryType.PART.toString());
        BulkUploadInfo uploadInfo = controller.create(account.getEmail(), info);
        Assert.assertNotNull(uploadInfo);

        Account accountFriend = AccountCreator.createTestAccount("testDeletePermission2", false);
        long id = uploadInfo.getId();
        AccessPermission permission = new AccessPermission();
        permission.setArticle(AccessPermission.Article.ACCOUNT);
        permission.setArticleId(accountFriend.getId());
        permission.setType(AccessPermission.Type.READ_UPLOAD);
        permission.setTypeId(id);

        permission = controller.addPermission(account.getEmail(), id, permission);

        List<AccessPermission> permissions = controller.getUploadPermissions(account.getEmail(), id);
        Assert.assertNotNull(permissions);
        Assert.assertTrue(permissions.size() == 1);

        //delete
        AccessPermission returnedPermission = permissions.get(0);
        Assert.assertTrue(controller.deletePermission(account.getEmail(), id, returnedPermission.getId()));

        permissions = controller.getUploadPermissions(account.getEmail(), id);
        Assert.assertTrue(permissions.isEmpty());

        // check that the permission record has been deleted
        Assert.assertNull(DAOFactory.getPermissionDAO().get(permission.getId()));
    }
}
