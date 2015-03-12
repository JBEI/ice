package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.shared.ColumnField;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class EntryCreatorTest {

    private EntryCreator creator;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        creator = new EntryCreator();
    }

    @Test
    public void testReceivedTransferredEntry() throws Exception {
        Account account = AccountCreator.createTestAccount("testReceivedTransferredEntry", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        PartData data = strain.toDataTransferObject();
        data.setRecordId(account.getEmail()); // faking record id since this is stored on "this instance"
        PartData createdData = creator.receiveTransferredEntry(data);
        Assert.assertNotNull(createdData);
        // transferring with same record id. should return null
        Assert.assertNull(creator.receiveTransferredEntry(data));

        // retrieve list of transferred
        List<Entry> entries = DAOFactory.getEntryDAO().getByVisibility(null, Visibility.TRANSFERRED, ColumnField.CREATED, false, 0, 1000);
        Assert.assertNotNull(entries);
        Assert.assertTrue(entries.size() == 1);
    }

    @Test
    public void testCreatePart() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreatePart", false);
        Assert.assertNotNull(account);
        String userId = account.getEmail();

        // create strain
        PartData strain = new PartData(EntryType.STRAIN);
        StrainData strainData = new StrainData();
        strainData.setGenotypePhenotype("genPhen");
        strainData.setHost("host");
        strain.setStrainData(strainData);
        strain.setOwner("Tester");
        strain.setOwnerEmail("tester");
        strain.setCreator(strain.getOwner());
        strain.setCreatorEmail(strain.getOwnerEmail());

        strain.setBioSafetyLevel(1);

        long id = creator.createPart(userId, strain);
        Strain entry = (Strain) DAOFactory.getEntryDAO().get(id);
        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getOwnerEmail(), strain.getOwnerEmail());

        Assert.assertEquals(strainData.getGenotypePhenotype(), entry.getGenotypePhenotype());
        Assert.assertEquals(strainData.getHost(), entry.getHost());

        // create arabidopsis seed
        PartData seed = new PartData(EntryType.ARABIDOPSIS);
        ArabidopsisSeedData seedData = new ArabidopsisSeedData();
        seedData.setGeneration(Generation.F3);
        seedData.setPlantType(PlantType.OTHER);
        seed.setBioSafetyLevel(2);
        seed.setArabidopsisSeedData(seedData);

        long seedId = creator.createPart(userId, seed);
        ArabidopsisSeed entrySeed = (ArabidopsisSeed) DAOFactory.getEntryDAO().get(seedId);
        Assert.assertNotNull(entrySeed);
        Assert.assertEquals(seedData.getGeneration(), entrySeed.getGeneration());
        Assert.assertEquals(seedData.getPlantType(), entrySeed.getPlantType());
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
