package org.jbei.ice.lib.entry;

import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class EntriesAsCSVTest {

    @Before
    public void setUp() throws Exception {
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }

    @Test
    public void testSetEntries() throws Exception {

    }
}