package org.jbei.ice.test.managers;

import java.io.IOException;

import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Attachment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAttachmentManager {
    protected Attachment attachment1;
    protected Attachment attachment2;

    @Before
    public void setUp() throws ManagerException {
        /*String description = "This is a file description";
        String fileName = "test_file_1.txt";
        String dataString = "This is the contents of file1\n";
        Entry entry = null;

        entry = EntryManager.get(1);
        Base64String data = new Base64String();
        data.putBytes(dataString.getBytes());
        //
        attachment1 = new Attachment(description, fileName, entry, data);

        //
        description = "This is the description for the second file";
        fileName = "test file 2.bin";
        dataString = "this is contents of file2";
        data.putBytes(dataString.getBytes());
        attachment2 = new Attachment(description, fileName, entry, data);*/

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateGetDelete() throws IOException, ManagerException {
        /*Attachment createdAttachment1 = AttachmentManager.create(attachment1);
        Attachment createdAttachment2 = AttachmentManager.create(attachment2);

        Attachment gotAttachment1 = AttachmentManager.get(createdAttachment1.getId());
        Attachment gotAttachment2 = AttachmentManager.get(createdAttachment2.getId());

        assertArrayEquals(createdAttachment1.getData().getBytes(), gotAttachment1.getData()
                .getBytes());
        assertArrayEquals(createdAttachment2.getData().getBytes(), gotAttachment2.getData()
                .getBytes());

        AttachmentManager.getByFileId(createdAttachment1.getFileId());
        AttachmentManager.getByFileId(createdAttachment2.getFileId());

        AttachmentManager.delete(createdAttachment1);
        AttachmentManager.delete(createdAttachment2);*/
    }

    @Test
    public void testGetByEntryId() throws Exception {
        /*Attachment createdAttachment1 = AttachmentManager.create(attachment1);
        Attachment createdAttachment2 = AttachmentManager.create(attachment2);

        try {
            ArrayList<Attachment> attachments = AttachmentManager
                    .getByEntry(attachment1.getEntry());

            for (Attachment i : attachments) {
                System.out.println(i.getFileId());

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            AttachmentManager.delete(createdAttachment1);
            AttachmentManager.delete(createdAttachment2);
        }*/
    }
}
