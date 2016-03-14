package org.jbei.ice.lib.entry;

import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class EntryHistoryTest {

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

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testDeleteAll() throws Exception {

    }
}