package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderDAOTest {

    private FolderDAO dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    @Before
    public void setUp() {
        dao = new FolderDAO();
        HibernateHelper.beginTransaction();
    }

    @After
    public void tearDown() {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testGet() throws Exception {
    }

    @Test
    public void testDelete() throws Exception {
    }

    @Test
    public void testRemoveFolderEntries() throws Exception {
    }

    @Test
    public void testGetFolderSize() throws Exception {
    }

    @Test
    public void testGetFolderContents() throws Exception {
    }

    @Test
    public void testAddFolderContents() throws Exception {
    }

    @Test
    public void testSave() throws Exception {
    }

    @Test
    public void testGetFoldersByOwner() throws Exception {
    }

    @Test
    public void testGetFoldersByEntry() throws Exception {
    }
}
