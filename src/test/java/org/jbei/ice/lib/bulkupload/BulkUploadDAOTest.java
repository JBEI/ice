package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class BulkUploadDAOTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateHelper.beginTransaction();
    }

    @Test
    public void testRetrieveById() throws Exception {
    }

    @Test
    public void testRetrieveByAccount() throws Exception {
    }

    @Test
    public void testUpdateWithNewEntry() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }
}
