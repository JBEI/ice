package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.StatusType;
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
    public void testRetrieveByUser() throws Exception {
        Account account = AccountCreator.createTestAccount("testRetrieveByUser", false);
        ArrayList<BulkUploadInfo> results = controller.retrieveByUser("testRetrieveByUser", "testRetrieveByUser");
        Assert.assertEquals(0, results.size());

        // create strain with plasmid
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryField.NAME, "strainPlasmid");
        autoUpdate.getKeyValue().put(EntryField.SUMMARY, "strainPlasmidSummary");
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, "Level 2");
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
        BulkUploadInfo info = controller.deleteDraftById(account.getEmail(), autoUpdate.getBulkUploadId());
        Assert.assertNotNull(info);
        Assert.assertEquals(autoUpdate.getBulkUploadId(), info.getId());
//        Assert.assertEquals(1, info.getCount());
        Assert.assertNull(controller.retrieveById(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0));
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

        BulkUploadInfo bulkUploadInfo = controller.retrieveById(account.getEmail(), bulkId, 0, 1000);
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
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, EntryType.ARABIDOPSIS);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        Assert.assertNotNull(controller.retrieveById(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0));

        // try to revert. not submitted
        Assert.assertFalse(controller.revertSubmitted(admin, autoUpdate.getBulkUploadId()));
        Assert.assertNotNull(controller.submitBulkImportDraft(account.getEmail(), autoUpdate.getBulkUploadId()));
        BulkUploadInfo info = controller.retrieveById(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0);
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
        autoUpdate.getKeyValue().put(EntryField.FUNDING_SOURCE, "JBEI");
        autoUpdate.getKeyValue().put(EntryField.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());

        autoUpdate = controller.autoUpdateBulkUpload(account.getEmail(), autoUpdate, EntryType.PLASMID);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertTrue(autoUpdate.getLastUpdate() != null);

        // submit draft
        Assert.assertNotNull(controller.submitBulkImportDraft(account.getEmail(), autoUpdate.getBulkUploadId()));
        Assert.assertTrue(controller.approveBulkImport(account.getEmail(), autoUpdate.getBulkUploadId()));

        // bulk upload should be deleted
        BulkUploadInfo info = controller.retrieveById(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNull(info);

        // entry must still exist and have a visibility of OK
        Entry entry = DAOFactory.getEntryDAO().get(autoUpdate.getEntryId());
        Assert.assertNotNull(entry);
        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());

        // check the set values of the entry (particularly the preferences)
        Assert.assertEquals("test", entry.getPrincipalInvestigator());
        Assert.assertEquals("JBEI", entry.getFundingSource());
        Assert.assertEquals("JBEI-0001", entry.getName());
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
