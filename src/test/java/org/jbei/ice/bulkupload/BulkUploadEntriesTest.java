package org.jbei.ice.bulkupload;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.entry.Visibility;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.BulkUpload;
import org.jbei.ice.storage.model.Entry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Hector Plahar
 */
public class BulkUploadEntriesTest extends HibernateRepositoryTest {

    /**
     * Creates a new bulk upload record in the database
     *
     * @param entryType type of bulk upload
     * @return unique identifier for bulk upload created
     */
    private BulkUpload createBulkUpload(String userId, EntryType entryType, EntryType linkType) {
        BulkUploads uploads = new BulkUploads();
        BulkUploadInfo info = new BulkUploadInfo();
        info.setStatus(BulkUploadStatus.IN_PROGRESS);
        info.setType(entryType.getName());
        if (linkType != null)
            info.setLinkType(linkType.getName());
        long uploadId = uploads.create(userId, info).getId();
        return DAOFactory.getBulkUploadDAO().get(uploadId);
    }

    @Test
    public void testCreateEntry() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testCreateEntry", false);
        BulkUpload upload = createBulkUpload(account.getEmail(), EntryType.PLASMID, null);
        BulkUploadEntries entries = new BulkUploadEntries(account.getEmail(), upload.getId());

        // attempt to create strain part
        PartData partData = new PartData(EntryType.STRAIN);
        partData.setName("plasmid");

        // should not work because upload type is plasmid
        boolean caught = false;
        try {
            partData = entries.createEntry(partData);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        Assert.assertTrue(caught);

        // attempt to create plasmid entry type; should work now
        partData.setType(EntryType.PLASMID);
        partData = entries.createEntry(partData);
        Assert.assertNotNull(partData);

        // verify that entry was created and associated with upload
        upload = DAOFactory.getBulkUploadDAO().get(upload.getId());
        Assert.assertEquals(1, upload.getContents().size());

        Entry entry = upload.getContents().iterator().next();
        Assert.assertEquals(entry.getRecordType().toLowerCase(), "plasmid");

        //
        // repeat test but add strain with plasmid with
        upload = createBulkUpload(account.getEmail(), EntryType.STRAIN, EntryType.PLASMID);
        Assert.assertNotNull(upload);
        Assert.assertEquals(0, upload.getContents().size());

        entries = new BulkUploadEntries(account.getEmail(), upload.getId());
        PartData strainData = new PartData(EntryType.STRAIN);
        strainData.setName("test strain");
        PartData plasmidData = new PartData(EntryType.PLASMID);
        partData.setName("plasmid");
        strainData.getLinkedParts().add(plasmidData);
        strainData = entries.createEntry(strainData);
        Assert.assertNotNull(strainData);

        // check explicit ids
        long strainId = strainData.getId();
        long plasmidId = strainData.getLinkedParts().get(0).getId();

        Entry strain = DAOFactory.getEntryDAO().get(strainId);
        Assert.assertNotNull(strain);
        Assert.assertEquals(plasmidId, strain.getLinkedEntries().iterator().next().getId());

        // check upload
        upload = DAOFactory.getBulkUploadDAO().get(upload.getId());
        Assert.assertNotNull(upload);

        Assert.assertEquals(1, upload.getContents().size());
        Assert.assertEquals(strainId, upload.getContents().iterator().next().getId());
    }

    @Test
    public void testUpdateEntry() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testUpdateEntry", false);

        PartData strainData = new PartData(EntryType.STRAIN);
        PartData plasmidData = new PartData(EntryType.PLASMID);

        // create bulk upload
        BulkUpload bulkUpload = createBulkUpload(account.getEmail(), EntryType.STRAIN, EntryType.PLASMID);
        Assert.assertEquals(bulkUpload.getStatus(), BulkUploadStatus.IN_PROGRESS);

        BulkUploadEntries entries = new BulkUploadEntries(account.getEmail(), bulkUpload.getId());

        // create strain entry
        strainData = entries.createEntry(strainData);
        Assert.assertNotNull(strainData);
        Assert.assertTrue(strainData.getId() > 0);

        // link plasmid
        strainData.getLinkedParts().add(plasmidData);
        strainData = entries.updateEntry(strainData.getId(), strainData);
        Assert.assertNotNull(strainData);

        // retrieve
        BulkUploads uploads = new BulkUploads();
        BulkUploadInfo retrieved = uploads.get(account.getEmail(), bulkUpload.getId(), 0, 10);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(retrieved.getEntryList().size(), 1);
        PartData retrievedPart = retrieved.getEntryList().get(0);

        Assert.assertEquals(1, retrievedPart.getLinkedParts().size());

        // update
        strainData = entries.updateEntry(strainData.getId(), strainData);

        Entry strain = DAOFactory.getEntryDAO().get(strainData.getId());

        retrieved = uploads.get(account.getEmail(), bulkUpload.getId(), 0, 10);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(retrieved.getEntryList().size(), 1);
    }

    @Test
    public void testUpdateStatus() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testUpdateStatus", false);
        BulkUpload bulkUpload = createBulkUpload(account.getEmail(), EntryType.PART, null);
        Assert.assertNotNull(bulkUpload);

        BulkUploadEntries entries = new BulkUploadEntries(account.getEmail(), bulkUpload.getId());

        // add random number (< 10) entries
        PartData partData = new PartData(EntryType.PART);
        final int count = new Random().nextInt(10);
        List<Long> entryIds = new ArrayList<>(count);

        for (int i = 0; i < count; i += 1) {
            partData.setOwnerEmail(account.getEmail());
            partData.setOwner(account.getFullName());
            partData = entries.createEntry(partData);
            Assert.assertNotNull(partData);
            entryIds.add(partData.getId());
        }

        // verify entries
        bulkUpload = DAOFactory.getBulkUploadDAO().get(bulkUpload.getId());
        Assert.assertNotNull(bulkUpload);
        Assert.assertEquals(count, bulkUpload.getContents().size());

        for (Entry entry : bulkUpload.getContents()) {
            Assert.assertEquals(Visibility.DRAFT.getValue(), entry.getVisibility().intValue());
            Assert.assertEquals(EntryType.PART.getName(), entry.getRecordType());
        }

        // update
        ProcessedBulkUpload process = entries.updateStatus(BulkUploadStatus.PENDING_APPROVAL);
        Assert.assertNotNull(process); // should fail due to validation error
        Assert.assertFalse(process.isSuccess());

        // name, pi, status, creator
        for (long id : entryIds) {
            PartData update = new PartData(EntryType.PART);
            update.setId(id);
            update.setName("name" + id);
            entries.updateEntry(id, update);
        }

        process = entries.updateStatus(BulkUploadStatus.PENDING_APPROVAL);
        Assert.assertTrue(process.isSuccess());
        bulkUpload = DAOFactory.getBulkUploadDAO().get(bulkUpload.getId());
        Assert.assertNotNull(bulkUpload);
        Assert.assertEquals(BulkUploadStatus.PENDING_APPROVAL, bulkUpload.getStatus());

        for (Entry entry : bulkUpload.getContents()) {
            Assert.assertEquals(Visibility.DRAFT.getValue(), entry.getVisibility().intValue());
        }

        // update to approved (only admins can update)
        process = entries.updateStatus(BulkUploadStatus.APPROVED);
        Assert.assertFalse(process.isSuccess());

        // create admin account
        AccountModel admin = AccountCreator.createTestAccount("ADMIN_testUpdateStatus", true);
        entries = new BulkUploadEntries(admin.getEmail(), bulkUpload.getId());
        process = entries.updateStatus(BulkUploadStatus.APPROVED);
        Assert.assertTrue(process.isSuccess());

        // when done, bulk upload is deleted
        bulkUpload = DAOFactory.getBulkUploadDAO().get(bulkUpload.getId());
        Assert.assertNull(bulkUpload);
    }

    @Test
    public void testCreateOrUpdateEntries() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testCreateOrUpdateEntries", false);
        BulkUpload upload = createBulkUpload(account.getEmail(), EntryType.SEED, null);
        Assert.assertNotNull(upload);

        // create a entry and associate with this upload
        PartData partData = new PartData(EntryType.SEED);
        partData.setName("name0");

        BulkUploadEntries entries = new BulkUploadEntries(account.getEmail(), upload.getId());
        partData = entries.createEntry(partData);
        Assert.assertNotNull(partData);

        final int count = new Random().nextInt(10);
        List<PartData> data = new ArrayList<>(count + 1);
        data.add(partData);

        for (int i = 0; i < count; i += 1) {
            PartData seed = new PartData(EntryType.SEED);
            seed.setName("name" + (i + 1));
            data.add(seed);
        }

        // create "count" entries and update 1
        BulkUploadInfo info = entries.createOrUpdateEntries(data);
        Assert.assertNotNull(info);
        Assert.assertEquals(count + 1, info.getEntryList().size());
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }
}
