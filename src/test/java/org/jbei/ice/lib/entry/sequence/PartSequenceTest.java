package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class PartSequenceTest {

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
    public void testGet() throws Exception {
        Account account = AccountCreator.createTestAccount("PartSequenceTest.testGet", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        PartSequence partSequence = new PartSequence(strain.getRecordId());
        FeaturedDNASequence sequence = partSequence.get(account.getEmail());
        Assert.assertNull(sequence);
    }

    @Test
    public void testGetFeaturedSequence() throws Exception {

    }

    @Test
    public void testSequenceToDNASequence() throws Exception {

    }
}