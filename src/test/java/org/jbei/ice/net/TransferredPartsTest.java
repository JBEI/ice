package org.jbei.ice.net;

import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class TransferredPartsTest {

    private TransferredParts parts;

    @Before
    public void setUp() throws Exception {
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
        parts = new TransferredParts();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }

    @Test
    public void testReceiveTransferredEntry() throws Exception {

    }
}
