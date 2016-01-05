package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.BulkUpload;
import org.junit.*;

/**
 * @author Hector Plahar
 */
public class BulkEntryCreatorTest {

    private BulkEntryCreator creator;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateUtil.beginTransaction();
        creator = new BulkEntryCreator();
    }

    @Test
    public void testCreateBulkUpload() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateBulkUpload", false);
        long id = creator.createBulkUpload(account.getEmail(), EntryType.PLASMID);
        Assert.assertTrue(id > 0);
        BulkUpload bulkUpload = DAOFactory.getBulkUploadDAO().get(id);
        Assert.assertNotNull(bulkUpload);
        Assert.assertEquals(BulkUploadStatus.IN_PROGRESS, bulkUpload.getStatus());
        Assert.assertEquals(account.getEmail(), bulkUpload.getAccount().getEmail());
    }

    @Test
    public void testCreateEntry() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateEntry", false);
        long uploadId = creator.createBulkUpload(account.getEmail(), EntryType.PLASMID);
        PartData partData = new PartData(EntryType.PLASMID);
        partData.setShortDescription("test summary");
        partData.setName("plasmid");
        partData.setBioSafetyLevel(1);

        partData = creator.createEntry(account.getEmail(), uploadId, partData);
        Assert.assertNotNull(partData);
    }

    @Test
    public void testUpdateEntry() throws Exception {

        Account account = AccountCreator.createTestAccount("testUpdateEntry", false);

        PartData strainData = new PartData(EntryType.STRAIN);
        PartData plasmidData = new PartData(EntryType.PLASMID);
        strainData.setPrincipalInvestigator("test 1");
        plasmidData.setPrincipalInvestigator("test 2");

        // create bulk upload
        BulkUploadController controller = new BulkUploadController();
        BulkUploadInfo info = new BulkUploadInfo();
        info.setAccount(account.toDataTransferObject());
        info = controller.create(account.getEmail(), info);
        Assert.assertNotNull(info);
        Assert.assertEquals(info.getStatus(), BulkUploadStatus.IN_PROGRESS);

        // create strain entry
        strainData = creator.createEntry(account.getEmail(), info.getId(), strainData);
        Assert.assertNotNull(strainData);
        Assert.assertTrue(strainData.getId() > 0);

        // link plasmid
        strainData.getLinkedParts().add(plasmidData);
        strainData = creator.updateEntry(account.getEmail(), info.getId(), strainData.getId(), strainData);
        Assert.assertNotNull(strainData);

        // retrieve
        BulkUploadInfo retrieved = controller.getBulkImport(account.getEmail(), info.getId(), 0, 10);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(retrieved.getEntryList().size(), 1);
        PartData retrievedPart = retrieved.getEntryList().get(0);

        Assert.assertTrue(retrievedPart.getLinkedParts().size() == 1);
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
