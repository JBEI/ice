package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.EntryFieldLabel;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.StatusType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Hector Plahar
 */
public class BulkUploadsTest extends HibernateRepositoryTest {

    private final BulkUploads uploads = new BulkUploads();

    @Test
    public void testCreate() throws Exception {
        Account account = AccountCreator.createTestAccount("testBulkUploadCreate", false);

        BulkUploadInfo info = new BulkUploadInfo();
        info.setName("testCreateName");
        info.setType(EntryType.PLASMID.getName());
        info.setAccount(account.toDataTransferObject());
        info = uploads.create(account.getEmail(), info);
        Assert.assertNotNull(info);
    }

    @Test
    public void testGetBulkImport() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetBulkImport", false);

        BulkUploads uploads = new BulkUploads();
        BulkUploadInfo info = new BulkUploadInfo();
        info.setStatus(BulkUploadStatus.IN_PROGRESS);
        info.setType(EntryType.PLASMID.getName());
        long uploadId = uploads.create(account.getEmail(), info).getId();

        BulkUploadEntries creator = new BulkUploadEntries(account.getEmail(), uploadId);

        // create bulk upload
        int count = 100;
        for (int i = 0; i < count; i += 1) {
            PartData partData = new PartData(EntryType.PLASMID);
            partData.setBioSafetyLevel(1);
            partData.setShortDescription("part description");
            partData.setName("part" + i);

            partData = creator.createEntry(partData);
            Assert.assertNotNull(partData);

            // add to bulk upload
        }

        info = uploads.get(account.getEmail(), uploadId, 0, 100);
        Assert.assertNotNull(info);
        Assert.assertEquals(info.getEntryList().size(), 100);

        // test retrieval in order
        for (int i = 0; i < 100; i += 10) {
            info = uploads.get(account.getEmail(), uploadId, i, 10);
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
        // create account 1. should not have any bulk uploads
        Account account = AccountCreator.createTestAccount("testRetrieveByUser", false);
        ArrayList<BulkUploadInfo> results = uploads.retrieveByUser("testRetrieveByUser", "testRetrieveByUser");
        Assert.assertEquals(0, results.size());

        // create strain with plasmid
        BulkUploadInfo info = createUpload(account.getEmail(), EntryType.STRAIN);
        Assert.assertNotNull(info);

        // update
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryFieldLabel.NAME, "strainPlasmid");
        autoUpdate.getKeyValue().put(EntryFieldLabel.SUMMARY, "strainPlasmidSummary");
        autoUpdate.getKeyValue().put(EntryFieldLabel.BIO_SAFETY_LEVEL, "Level 2");
        autoUpdate.getKeyValue().put(EntryFieldLabel.STATUS, "In Progress");
        autoUpdate.getKeyValue().put(EntryFieldLabel.PI, "Principal Investigator");
        autoUpdate.getKeyValue().put(EntryFieldLabel.SELECTION_MARKERS, "strain selection markers");
        autoUpdate.setBulkUploadId(info.getId());
        autoUpdate = uploads.autoUpdateBulkUpload(account.getEmail(), autoUpdate);
        Assert.assertNotNull(autoUpdate);

        // verify
        ArrayList<BulkUploadInfo> userUpload = uploads.retrieveByUser(account.getEmail(), account.getEmail());
        Assert.assertNotNull(userUpload);
        Assert.assertEquals(1, userUpload.size());

        // create account 2
        Account account2 = AccountCreator.createTestAccount("testRetrieveByUser2", false);

        // create second upload object
        info = createUpload(account2.getEmail(), EntryType.PART);
        Assert.assertNotNull(info);

        final int count = new Random().nextInt(10);
        for (int i = 0; i < count; i += 1) {
            autoUpdate = new BulkUploadAutoUpdate(EntryType.PART);
            autoUpdate.getKeyValue().put(EntryFieldLabel.NAME, "Name" + i);
            autoUpdate.getKeyValue().put(EntryFieldLabel.PI, "PI" + i);
            autoUpdate.getKeyValue().put(EntryFieldLabel.SUMMARY, "Summary" + i);
            autoUpdate.setBulkUploadId(info.getId());
            Assert.assertNotNull(uploads.autoUpdateBulkUpload(account2.getEmail(), autoUpdate));
        }

        userUpload = uploads.retrieveByUser(account2.getEmail(), account2.getEmail());
        Assert.assertEquals(1, userUpload.size());

        // attempt to retrieve each others entries (should not work due to lack of permissions)
        Assert.assertEquals(0, uploads.retrieveByUser(account.getEmail(), account2.getEmail()).size());
        Assert.assertEquals(0, uploads.retrieveByUser(account2.getEmail(), account.getEmail()).size());
    }

    private BulkUploadInfo createUpload(String email, EntryType entryType) {
        // create upload object
        BulkUploadInfo info = new BulkUploadInfo();
        info.setType(entryType.getName());
        info.setName("my upload");
        info.setStatus(BulkUploadStatus.IN_PROGRESS);
        info = uploads.create(email, info);
        Assert.assertNotNull(info);
        return info;
    }

    @Test
    public void testDeleteDraftById() throws Exception {
        Account account = AccountCreator.createTestAccount("testDeleteDraftById", false);
        BulkUploadInfo info = createUpload(account.getEmail(), EntryType.STRAIN);
        Assert.assertNotNull(uploads.get(account.getEmail(), info.getId(), 0, 0));

        // delete bulk upload
        BulkUploadDeleteTask task = new BulkUploadDeleteTask(account.getEmail(), info.getId());
        task.execute();
        Assert.assertNull(uploads.get(account.getEmail(), info.getId(), 0, 0));
    }

    @Test
    public void testAutoUpdateBulkUpload() throws Exception {
        Account account = AccountCreator.createTestAccount("testAutoUpdateBulkUpload", false);
        BulkUploadInfo info = createUpload(account.getEmail(), EntryType.STRAIN);

        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.STRAIN);
        autoUpdate.getKeyValue().put(EntryFieldLabel.LINKS, "google");
        autoUpdate.setBulkUploadId(info.getId());

        // first auto update. expect it to create a new entry
        autoUpdate = uploads.autoUpdateBulkUpload(account.getEmail(), autoUpdate);
        Assert.assertNotNull(autoUpdate);

        long entryId = autoUpdate.getEntryId();
        long bulkId = autoUpdate.getBulkUploadId();

        // check bulk upload
        BulkUploadInfo bulkUploadInfo = uploads.get(account.getEmail(), bulkId, 0, 1000);
        Assert.assertNotNull(bulkUploadInfo);

        // check entry
        EntryDAO dao = DAOFactory.getEntryDAO();
        Entry entry = dao.get(entryId);
        Assert.assertNotNull(entry);
        Assert.assertNotNull(entry.getLinks());
        Assert.assertEquals(1, entry.getLinks().size());
        Assert.assertEquals("google", entry.getLinks().iterator().next().getLink());
    }

    @Test
    public void testRevertSubmitted() throws Exception {
        // create accounts
        Account account = AccountCreator.createTestAccount("testRevertSubmitted", false);
        Account admin = AccountCreator.createTestAccount("testRevertSubmitted+Admin", true);
        BulkUploadInfo info = createUpload(account.getEmail(), EntryType.SEED);

        // update object
        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryType.SEED);
        autoUpdate.getKeyValue().put(EntryFieldLabel.NAME, "JBEI-0001");
        autoUpdate.getKeyValue().put(EntryFieldLabel.SUMMARY, "this is a test");
        autoUpdate.getKeyValue().put(EntryFieldLabel.PI, "test");
        autoUpdate.getKeyValue().put(EntryFieldLabel.STATUS, StatusType.COMPLETE.toString());
        autoUpdate.getKeyValue().put(EntryFieldLabel.BIO_SAFETY_LEVEL, BioSafetyOption.LEVEL_TWO.getValue());
        autoUpdate.getKeyValue().put(EntryFieldLabel.SELECTION_MARKERS, "test");
        autoUpdate.setBulkUploadId(info.getId());

        autoUpdate = uploads.autoUpdateBulkUpload(account.getEmail(), autoUpdate);
        Assert.assertNotNull(autoUpdate);
        Assert.assertTrue(autoUpdate.getEntryId() > 0);
        Assert.assertTrue(autoUpdate.getBulkUploadId() > 0);
        Assert.assertNotNull(autoUpdate.getLastUpdate());

        Assert.assertNotNull(uploads.get(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0));

        // try to revert. not submitted
        Assert.assertFalse(uploads.revertSubmitted(admin, autoUpdate.getBulkUploadId()));

        // actual submission (update status)
        BulkUploadEntries bulkUploadEntries = new BulkUploadEntries(account.getEmail(), autoUpdate.getBulkUploadId());
        bulkUploadEntries.updateStatus(BulkUploadStatus.PENDING_APPROVAL);

        BulkUploadInfo retrieved = uploads.get(account.getEmail(), autoUpdate.getBulkUploadId(), 0, 0);
        Assert.assertNotNull(retrieved);
        Assert.assertTrue(uploads.revertSubmitted(admin, autoUpdate.getBulkUploadId()));
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
        testInfo.setLinkType(EntryType.PLASMID.getName());
        testInfo = uploads.create(userId, testInfo);
        Assert.assertNotNull(testInfo);

        // create entry for upload
        BulkUploadEntries creator = new BulkUploadEntries(userId, testInfo.getId());
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

        PartData returnStrainData = creator.createEntry(strainData);
        Assert.assertNotNull(returnStrainData);

        plasmidData.setStatus("In Progress");
        plasmidData = creator.updateEntry(returnStrainData.getLinkedParts().get(0).getId(), plasmidData);
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
        BulkUploadInfo uploadInfo = uploads.create(account.getEmail(), info);
        Assert.assertNotNull(uploadInfo);

        Account accountFriend = AccountCreator.createTestAccount("testAddPermission2", false);
        long id = uploadInfo.getId();
        AccessPermission permission = new AccessPermission();
        permission.setArticle(AccessPermission.Article.ACCOUNT);
        permission.setArticleId(accountFriend.getId());
        permission.setType(AccessPermission.Type.READ_UPLOAD);
        permission.setTypeId(id);

        uploads.addPermission(account.getEmail(), id, permission);

        List<AccessPermission> permissions = uploads.getUploadPermissions(account.getEmail(), id);
        Assert.assertNotNull(permissions);
        Assert.assertEquals(1, permissions.size());

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
        BulkUploadInfo uploadInfo = uploads.create(account.getEmail(), info);
        Assert.assertNotNull(uploadInfo);

        Account accountFriend = AccountCreator.createTestAccount("testDeletePermission2", false);
        long id = uploadInfo.getId();
        AccessPermission permission = new AccessPermission();
        permission.setArticle(AccessPermission.Article.ACCOUNT);
        permission.setArticleId(accountFriend.getId());
        permission.setType(AccessPermission.Type.READ_UPLOAD);
        permission.setTypeId(id);

        permission = uploads.addPermission(account.getEmail(), id, permission);

        List<AccessPermission> permissions = uploads.getUploadPermissions(account.getEmail(), id);
        Assert.assertNotNull(permissions);
        Assert.assertEquals(1, permissions.size());

        //delete
        AccessPermission returnedPermission = permissions.get(0);
        Assert.assertTrue(uploads.deletePermission(account.getEmail(), id, returnedPermission.getId()));

        permissions = uploads.getUploadPermissions(account.getEmail(), id);
        Assert.assertTrue(permissions.isEmpty());

        // check that the permission record has been deleted
        Assert.assertNull(DAOFactory.getPermissionDAO().get(permission.getId()));
    }
}
