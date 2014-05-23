package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.entry.PartData;

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

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testCreatePart() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreatePart", false);
        PartData data = new PartData();
        data.setName("A");
        data.setShortDescription("A short description");
        data.setPrincipalInvestigator("A principal Investigator");
        long singleEntryId = creator.createPart(account.getEmail(), data);
        Assert.assertTrue(singleEntryId >= 1);

        // test create with linked using a combination of created and new
        data = new PartData();
        data.setName("B");
        data.setShortDescription("B short description");
        data.setPrincipalInvestigator("B principal Investigator");

        // first link is existing
        PartData linked1 = new PartData();
        linked1.setId(singleEntryId);
        data.getLinkedParts().add(linked1);

        PartData linked2 = new PartData();
        linked2.setName("C");
        linked2.setShortDescription("C short description");
        linked2.setPrincipalInvestigator("C principal Investigator");
        data.getLinkedParts().add(linked2);

        long entryId = creator.createPart(account.getEmail(), data);
        Assert.assertTrue(entryId > 0);
    }
}
