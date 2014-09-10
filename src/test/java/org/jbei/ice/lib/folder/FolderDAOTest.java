package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dao.hibernate.FolderDAO;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderDAOTest {

    private FolderDAO dao;

    @Before
    public void setUp() {
        HibernateUtil.initializeMock();
        dao = new FolderDAO();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() {
        HibernateUtil.rollbackTransaction();
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

    private Folder createFolderObject() {
        Folder folder = new Folder();
        folder.setDescription("test");
        folder.setName("testFolderName");
        folder.setPropagatePermissions(false);
        return folder;
    }
}
