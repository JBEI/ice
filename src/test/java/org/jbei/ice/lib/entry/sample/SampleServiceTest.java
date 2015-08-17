package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.entry.model.Strain;
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
    public void testDelete() throws Exception {
        Account account = AccountCreator.createTestAccount("testDelete", false);
        String userId = account.getEmail();

        Strain strain = TestEntryCreator.createTestStrain(account);
        PartSample partSample = new PartSample();
        partSample.setLabel("test");

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

        partSample.setLocation(shelf);
        partSample = service.createSample(userId, strain.getId(), partSample, null);
        Assert.assertNotNull(partSample);

        // fetch
        List<PartSample> samples = service.retrieveEntrySamples(userId, strain.getId());
        Assert.assertEquals(1, samples.size());
        StorageLocation location = samples.get(0).getLocation();

        // delete
        Assert.assertTrue(service.delete(userId, strain.getId(), samples.get(0).getId()));

        Assert.assertNull(DAOFactory.getStorageDAO().get(location.getId()));
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}