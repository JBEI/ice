package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.shared.ColumnField;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderDAOTest {

    private FolderDAO dao;

    @Before
    public void setUp() {
        dao = new FolderDAO();
    }

    @Test
    public void testGet() throws Exception {
        HibernateHelper.beginTransaction();
        dao.retrieveFolderContents(1, ColumnField.CREATED, false, 0, 12);
        HibernateHelper.rollbackTransaction();
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
