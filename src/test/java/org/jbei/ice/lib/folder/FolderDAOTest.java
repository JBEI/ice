package org.jbei.ice.lib.folder;

import junit.framework.Assert;
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
        Folder folder = dao.get(1);
        Assert.assertNotNull(folder);
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
