package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dao.hibernate.BulkUploadDAO;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.entry.model.Entry;

import org.junit.After;
import org.junit.Assert;
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

    @Test
    public void testGetUploadEntry() throws Exception {
        Entry entry = dao.getUploadEntry(335l, 36892l);
        Assert.assertNull(entry);
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
