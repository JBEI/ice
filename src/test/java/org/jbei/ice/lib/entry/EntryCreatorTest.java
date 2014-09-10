package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.StrainData;
import org.jbei.ice.lib.entry.model.Strain;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class EntryCreatorTest {

    private EntryCreator creator = new EntryCreator();

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
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

        strain.setBioSafetyLevel(1);

        long id = creator.createPart(userId, strain);
        Strain entry = (Strain) DAOFactory.getEntryDAO().get(id);
        Assert.assertNotNull(entry);

        Assert.assertEquals(strainData.getGenotypePhenotype(), entry.getGenotypePhenotype());
        Assert.assertEquals(strainData.getHost(), entry.getHost());
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
