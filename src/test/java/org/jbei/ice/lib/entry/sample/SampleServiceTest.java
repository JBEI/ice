package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public void createStorage() throws Exception {
        Storage storage = service.createStorage("user1", "test1", SampleType.ADDGENE);
        Assert.assertNotNull(storage);
        Assert.assertEquals(storage.getIndex(), "test1");
        Assert.assertEquals(storage.getOwnerEmail(), "user1");
        Assert.assertEquals(storage.getStorageType().name(), SampleType.ADDGENE.name());
    }

    private String numberToPosition(int number) {
        Assert.assertTrue("Invalid plate number", number >= 0 && number <= 95);
        String row = String.format("%02d", (number % 12) + 1);
        int position = number / 12;

        switch (position) {
            case 0:
                return "A" + row;
            case 1:
                return "B" + row;
            case 2:
                return "C" + row;
            case 3:
                return "D" + row;
            case 4:
                return "E" + row;
            case 5:
                return "F" + row;
            case 6:
                return "G" + row;
            case 7:
                return "H" + row;
        }
        return null;
    }

    @Test
    public void testCreateSample() throws Exception {
        Account account = AccountCreator.createTestAccount("SampleServiceTest.testCreateSample", false);
        String userId = account.getEmail();

        // create 96 strains
        Map<Long, String> idPartNumberMap = new HashMap<>();
        for (int i = 0; i < 96; i += 1) {
            Strain strain = TestEntryCreator.createTestStrain(account);
            Assert.assertNotNull(strain);
            idPartNumberMap.put(strain.getId(), strain.getPartNumber());
        }
        Assert.assertEquals(96, idPartNumberMap.size());

        String mainPlate = "0000000001";

        // create main plate
        createPlateSamples(userId, mainPlate, idPartNumberMap);

        List<Storage> storageList = DAOFactory.getStorageDAO().retrieveStorageByIndex(mainPlate, SampleType.PLATE96);
        Assert.assertNotNull(storageList);
        Assert.assertEquals(1, storageList.size());

        // retrieve samples for entries
        int i = 0;
        for (String partNumber : idPartNumberMap.values()) {
            List<PartSample> samples = service.retrieveEntrySamples(userId, partNumber);
            Assert.assertNotNull(samples);
            Assert.assertTrue(samples.size() == 1);
            verifyMainPlate(samples.get(0), i, mainPlate);
            i += 1;
        }

        // create backup 1
        String backUp1Plate = "0000000002";
        createPlateSamples(userId, backUp1Plate, idPartNumberMap);
        storageList = DAOFactory.getStorageDAO().retrieveStorageByIndex(backUp1Plate, SampleType.PLATE96);
        Assert.assertNotNull(storageList);
        Assert.assertEquals(1, storageList.size());

        // retrieve samples for entries
        i = 0;
        for (String partNumber : idPartNumberMap.values()) {
            List<PartSample> samples = service.retrieveEntrySamples(userId, partNumber);
            Assert.assertNotNull(samples);
            Assert.assertTrue(samples.size() == 2);
            verifyMainPlate(samples.get(0), i, mainPlate, backUp1Plate);
            verifyMainPlate(samples.get(1), i, mainPlate, backUp1Plate);
            i += 1;
        }

        // create backup 2
        String backUp2Plate = "0000000003";
        createPlateSamples(userId, backUp2Plate, idPartNumberMap);
        storageList = DAOFactory.getStorageDAO().retrieveStorageByIndex(backUp1Plate, SampleType.PLATE96);
        Assert.assertNotNull(storageList);
        Assert.assertEquals(1, storageList.size());

        // retrieve samples for entries
        i = 0;
        for (String partNumber : idPartNumberMap.values()) {
            List<PartSample> samples = service.retrieveEntrySamples(userId, partNumber);
            Assert.assertNotNull(samples);
            Assert.assertTrue(samples.size() == 3);
            verifyMainPlate(samples.get(0), i, mainPlate, backUp1Plate, backUp2Plate);
            verifyMainPlate(samples.get(1), i, mainPlate, backUp1Plate, backUp2Plate);
            verifyMainPlate(samples.get(2), i, mainPlate, backUp1Plate, backUp2Plate);
            i += 1;
        }
    }

    protected void createPlateSamples(String userId, String plate, Map<Long, String> idPartNumberMap) {
        int i = 0;
        for (Long entryId : idPartNumberMap.keySet()) {

            // for each entry
            PartSample partSample = new PartSample();

            StorageLocation plateLocation = new StorageLocation();
            plateLocation.setType(SampleType.PLATE96);
            plateLocation.setDisplay(plate);

            StorageLocation well = new StorageLocation();
            well.setType(SampleType.WELL);
            String wellDisplay = numberToPosition(i);
            Assert.assertNotNull(wellDisplay);
            well.setDisplay(wellDisplay);  // e.g. "AO1"
            plateLocation.setChild(well);

            StorageLocation tube = new StorageLocation();
            tube.setType(SampleType.TUBE);
            String index = UUID.randomUUID().toString().split("-")[0];
            tube.setDisplay(index);
            well.setChild(tube);

            partSample.setLocation(plateLocation);
            String partNumber = idPartNumberMap.get(entryId);
            Assert.assertNotNull(partNumber);
            partSample.setLabel(partNumber);  // part number of part_number_backup 1/2

            partSample = service.createSample(userId, partNumber, partSample, null);

            Assert.assertNotNull(partSample);
            Assert.assertNotNull(partSample.getLabel());
            i += 1;
        }
    }

    protected void verifyMainPlate(PartSample sample, int index, String... mainPlate) {
        StorageLocation plate = sample.getLocation();
        Assert.assertNotNull(plate);
        Assert.assertEquals(plate.getType(), SampleType.PLATE96);

        boolean found = false;
        for (String aMainPlate : mainPlate) {
            if (plate.getDisplay().equals(aMainPlate)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);

        // well
        String wellLocation = numberToPosition(index);
        StorageLocation well = plate.getChild();
        Assert.assertEquals(well.getType(), SampleType.WELL);
        Assert.assertNotNull(well);
        Assert.assertEquals(well.getDisplay(), wellLocation);

        // plate
        StorageLocation tube = well.getChild();
        Assert.assertNotNull(tube);
        Assert.assertEquals(tube.getType(), SampleType.TUBE);
        Assert.assertFalse(tube.getDisplay().isEmpty());
    }

    @Test
    public void testRetrieveEntrySamples() throws Exception {
        Account account = AccountCreator.createTestAccount("SampleServiceTest.testRetrieveEntrySamples", false);
        String userId = account.getEmail();

        Strain strain = TestEntryCreator.createTestStrain(account);
        long entryId = strain.getId();

        List<PartSample> samplesList = service.retrieveEntrySamples(userId, Long.toString(entryId));

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

        partSample1.setLocation(shelf);
        partSample1 = service.createSample(userId, strain1.getRecordId(), partSample1, null);
        Assert.assertNotNull(partSample1);

        // fetch
        List<PartSample> samples1 = service.retrieveEntrySamples(userId, Long.toString(strain1.getId()));
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

        partSample2.setLocation(plate2);
        partSample2 = service.createSample(userId, Long.toString(strain2.getId()), partSample2, null);
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

        partSample3.setLocation(plate3);
        partSample3 = service.createSample(userId, Long.toString(strain2.getId()), partSample3, null);
        Assert.assertNotNull(partSample3);

        // fetch
        List<PartSample> samples3 = service.retrieveEntrySamples(userId, Long.toString(strain2.getId()));
        Assert.assertEquals(2, samples3.size());
        StorageLocation location3 = samples3.get(1).getLocation();

        // delete #2 and #3 consequently
        Assert.assertTrue(service.delete(userId, strain2.getId(), samples3.get(1).getId()));

        Assert.assertNull(DAOFactory.getStorageDAO().get(location3.getId()));

        List<PartSample> samples2 = service.retrieveEntrySamples(userId, Long.toString(strain2.getId()));
        Assert.assertEquals(1, samples2.size());
        StorageLocation location2 = samples2.get(0).getLocation();

        Assert.assertTrue(service.delete(userId, strain2.getId(), samples2.get(0).getId()));

        Assert.assertNull(DAOFactory.getStorageDAO().get(location2.getId()));

        List<PartSample> samplesEmpty = service.retrieveEntrySamples(userId, Long.toString(strain2.getId()));
        Assert.assertEquals(0, samplesEmpty.size());
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
        partSample = service.createSample(userId, Long.toString(strain.getId()), partSample, null);
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