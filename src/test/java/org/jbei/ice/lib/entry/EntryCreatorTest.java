package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.ArabidopsisSeed;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        long id = creator.createPart(userId, strain).getId();
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
        seedData.setHarvestDate("01/02/2014");
        seed.setBioSafetyLevel(2);
        seed.setArabidopsisSeedData(seedData);

        long seedId = creator.createPart(userId, seed).getId();
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
