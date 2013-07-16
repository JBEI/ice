package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderControllerTest {

    private FolderController controller;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new FolderController();
    }

    @Test
    public void testCreateNewFolder() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateNewFolder", false);
        FolderDetails folder = controller.createNewFolder(account, "test", "testing folder creation", null);
        Assert.assertNotNull(folder);
        Folder f = controller.getFolderById(folder.getId());
        Assert.assertNotNull(f);
        Assert.assertEquals("test", f.getName());
        Assert.assertEquals("testing folder creation", f.getDescription());
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }
}
