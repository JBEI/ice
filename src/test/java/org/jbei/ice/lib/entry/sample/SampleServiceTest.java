package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Storage;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class SampleServiceTest {

    private SampleService service;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        service = new SampleService();
    }

    @Test
    public void testCreateSample() throws Exception {
        Account account = AccountCreator.createTestAccount("SampleServiceTest.testCreateSample", false);
        String userId = account.getEmail();

        Strain strain = TestEntryCreator.createTestStrain(account);
        PartSample partSample = new PartSample();
        partSample.setLabel("test");

        StorageLocation testLocation = new StorageLocation();
        testLocation.setDisplay("test");
        testLocation.setType(SampleType.GENERIC);
        partSample.setLocation(testLocation);

        partSample = service.createSample(userId, strain.getId(), partSample, null);

        Assert.assertNotNull(partSample);
        Assert.assertNotNull(partSample.getLabel());

        // create samples on plate
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail(userId);

        for (int i = 1; i <= 9; i += 1) {
            String location = "A0" + i;
            String index = "I" + i;

            Strain strainWithSample = TestEntryCreator.createTestStrain(account);

            PartSample plateSample = new PartSample();
            plateSample.setLabel("Working Copy");
            plateSample.setDepositor(accountTransfer);

            // plate
            StorageLocation storageLocation = new StorageLocation();
            storageLocation.setType(SampleType.PLATE96);
            storageLocation.setDisplay("0000000004");

            // well
            StorageLocation well = new StorageLocation();
            well.setType(SampleType.WELL);
            well.setDisplay(location);
            storageLocation.setChild(well);

            // tube
            StorageLocation tube = new StorageLocation();
            tube.setType(SampleType.TUBE);
            tube.setDisplay(index);
            well.setChild(tube);

            plateSample.setLocation(storageLocation);
            plateSample = service.createSample(userId, strainWithSample.getId(), plateSample, null);
            Assert.assertNotNull(plateSample);
        }

        List<Storage> result = DAOFactory.getStorageDAO().retrieveStorageByIndex("0000000004", SampleType.PLATE96);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testRetrieveEntrySamples() throws Exception {
        Account account = AccountCreator.createTestAccount("SampleServiceTest.testRetrieveEntrySamples", false);
        String userId = account.getEmail();

        Strain strain = TestEntryCreator.createTestStrain(account);
        long entryId = strain.getId();

        List<PartSample> samplesList = service.retrieveEntrySamples(userId, entryId);

        Assert.assertNotNull(samplesList);

        for (PartSample partSample : samplesList) {
            Assert.assertNotNull(partSample.getId());
            Assert.assertNotNull(partSample.getCreationTime());
            Assert.assertNotNull(partSample.getLabel());
            Assert.assertNotNull(partSample.getLocation());
            Assert.assertNotNull(partSample.getDepositor());
            Assert.assertNotNull(partSample.getId());
            Assert.assertNotNull(partSample.isCanEdit());

            StorageLocation location = partSample.getLocation();
            while (location.getChild() != null) {
                location = location.getChild();
                Assert.assertNotNull(location.getType());
                Assert.assertNotNull(location.getId());
                Assert.assertNotNull(location.getDisplay());
                Assert.assertNotNull(location.getName());
            }

            if (partSample.getComments() != null) {
                for (UserComment comment : partSample.getComments()) {
                    Assert.assertNotNull(comment.getId());
                    Assert.assertNotNull(comment.getMessage());
                }
            }
        }
    }

    @Test
    public void testDelete() throws Exception {
        Account account = AccountCreator.createTestAccount("testDelete", false);
        String userId = account.getEmail();

        // ----- CASE #1 -----
        Strain strain1 = TestEntryCreator.createTestStrain(account);
        PartSample partSample1 = new PartSample();
        partSample1.setLabel("testForShelfScheme1");

        // well
        StorageLocation well = new StorageLocation();
        well.setDisplay("well");
        well.setType(SampleType.WELL);

        // box
        StorageLocation box = new StorageLocation();
        box.setDisplay("box");
        box.setType(SampleType.BOX_INDEXED);
        box.setChild(well);

        // shelf
        StorageLocation shelf = new StorageLocation();
        shelf.setDisplay("shelf");
        shelf.setType(SampleType.SHELF);
        shelf.setChild(box);

        partSample1.setLocation(well);
        partSample1 = service.createSample(userId, strain1.getId(), partSample1, null);
        Assert.assertNotNull(partSample1);

        // fetch
        List<PartSample> samples1 = service.retrieveEntrySamples(userId, strain1.getId());
        Assert.assertEquals(1, samples1.size());
        StorageLocation location1 = samples1.get(0).getLocation();

        // delete
        Assert.assertTrue(service.delete(userId, strain1.getId(), samples1.get(0).getId()));

        Assert.assertNull(DAOFactory.getStorageDAO().get(location1.getId()));

        // ----- CASE 2 -----

        Strain strain2 = TestEntryCreator.createTestStrain(account);
        PartSample partSample2 = new PartSample();
        partSample2.setLabel("testForPlateScheme2");

        // tube2
        StorageLocation tube2 = new StorageLocation();
        tube2.setDisplay("tube2");
        tube2.setType(SampleType.TUBE);

        // well
        StorageLocation well2 = new StorageLocation();
        well2.setDisplay("well2");
        well2.setType(SampleType.WELL);
        well2.setChild(tube2);

        // plate2
        StorageLocation plate2 = new StorageLocation();
        plate2.setDisplay("plate2");
        plate2.setType(SampleType.PLATE96);
        plate2.setChild(well2);

        partSample2.setLocation(tube2);
        partSample2 = service.createSample(userId, strain2.getId(), partSample2, null);
        Assert.assertNotNull(partSample2);

        // ----- CASE 3 -----
        PartSample partSample3 = new PartSample();
        partSample3.setLabel("testForPlateScheme3");

        // tube2
        StorageLocation tube3 = new StorageLocation();
        tube3.setDisplay("tube3");
        tube3.setType(SampleType.TUBE);

        // well
        StorageLocation well3 = new StorageLocation();
        well3.setDisplay("well3");
        well3.setType(SampleType.WELL);
        well3.setChild(tube3);

        // plate2
        StorageLocation plate3 = new StorageLocation();
        plate3.setDisplay("plate3");
        plate3.setType(SampleType.PLATE96);
        plate3.setChild(well3);

        partSample3.setLocation(tube3);
        partSample3 = service.createSample(userId, strain2.getId(), partSample3, null);
        Assert.assertNotNull(partSample3);

        // fetch
        List<PartSample> samples3 = service.retrieveEntrySamples(userId, strain2.getId());
        Assert.assertEquals(2, samples3.size());
        StorageLocation location3 = samples3.get(1).getLocation();

        // delete #2 and #3 consequently
        Assert.assertTrue(service.delete(userId, strain2.getId(), samples3.get(1).getId()));

        Assert.assertNull(DAOFactory.getStorageDAO().get(location3.getId()));

        List<PartSample> samples2 = service.retrieveEntrySamples(userId, strain2.getId());
        Assert.assertEquals(1, samples2.size());
        StorageLocation location2 = samples2.get(0).getLocation();

        Assert.assertTrue(service.delete(userId, strain2.getId(), samples2.get(0).getId()));

        Assert.assertNull(DAOFactory.getStorageDAO().get(location2.getId()));

        List<PartSample> samplesEmpty = service.retrieveEntrySamples(userId, strain2.getId());
        Assert.assertEquals(0, samplesEmpty.size());
    }

    @Test
    public void testGetStorageLocations() throws Exception {
        Account account = AccountCreator.createTestAccount("SampleServiceTest.testGetStorageLocations", false);
        String userId = account.getEmail();

        Strain strain = TestEntryCreator.createTestStrain(account);
        String entryType = strain.getRecordType();

        List<StorageLocation> storageLocations = service.getStorageLocations(userId, entryType);
        Assert.assertNotNull(storageLocations);
    }

    @Test
    public void testGetSamplesByBarcode() throws Exception {
        Account account = AccountCreator.createTestAccount("SampleServiceTest.testGetSamplesByBarcode", false);
        String userId = account.getEmail();

        Strain strain = TestEntryCreator.createTestStrain(account);
        PartSample partSample = new PartSample();
        partSample.setLabel("test");

        StorageLocation location = new StorageLocation();
        location.setDisplay("tube");
        location.setType(SampleType.TUBE);

        partSample.setLocation(location);
        partSample = service.createSample(userId, strain.getId(), partSample, null);
        Assert.assertNotNull(partSample);

        List<PartSample> partSamples = service.getSamplesByBarcode(userId, location.getDisplay());
        Assert.assertNotNull(partSamples);
        Assert.assertEquals(1, partSamples.size());
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}