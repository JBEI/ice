package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class BulkUploadDAOTest {

    private BulkUploadDAO dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateUtil.beginTransaction();
        dao = new BulkUploadDAO();
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
        HibernateUtil.commitTransaction();
    }
}
