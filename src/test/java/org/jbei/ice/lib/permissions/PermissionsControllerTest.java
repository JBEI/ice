package org.jbei.ice.lib.permissions;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.server.dao.hibernate.HibernateHelper;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class PermissionsControllerTest {

    private PermissionsController controller;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
        controller = new PermissionsController();
    }

    @Test
    public void testAddPermission() throws Exception {

        String email = "testAddPermission@TESTER.org";
        String email2 = "testAddPermissionOther@TESTER.org";

        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        accountController.createNewAccount("", "TEST", "T", email2, null, "");
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account otherAccount = accountController.getByEmail(email2);
        Assert.assertNotNull(otherAccount);

        // create entry
        Strain strain = new Strain();
        EntryController entryController = new EntryController();
        Assert.assertNotNull(entryController.createEntry(account, strain, false, null));

        // add read account permission
        controller.addPermission(account, PermissionInfo.PermissionType.READ_ACCOUNT, strain, otherAccount.getId());

        // verify
        Assert.assertFalse(controller.hasWritePermission(otherAccount, strain));
        Assert.assertTrue(controller.hasWritePermission(account, strain));     // is owner so has write permission
        Assert.assertTrue(controller.hasReadPermission(otherAccount, strain));

        // add write permission
        controller.addPermission(account, PermissionInfo.PermissionType.WRITE_ACCOUNT, strain, otherAccount.getId());

        // verify
        Assert.assertTrue(controller.hasWritePermission(otherAccount, strain));

        // remove write permission
        controller.removePermission(account, PermissionInfo.PermissionType.READ_ACCOUNT, strain, otherAccount.getId());

        // should still have read permission
        Assert.assertTrue(controller.hasReadPermission(otherAccount, strain));
    }

    @Test
    public void testRemovePermission() throws Exception {

    }

    @Test
    public void testSetReadGroup() throws Exception {

    }

    @Test
    public void testSetWriteGroup() throws Exception {

    }

    @Test
    public void testSetReadUser() throws Exception {

    }

    @Test
    public void testAddReadUser() throws Exception {

    }

    @Test
    public void testRemoveReadUser() throws Exception {

    }

    @Test
    public void testAddReadGroup() throws Exception {

    }

    @Test
    public void testRemoveReadGroup() throws Exception {

    }

    @Test
    public void testAddWriteUser() throws Exception {

    }

    @Test
    public void testRemoveWriteUser() throws Exception {

    }

    @Test
    public void testRemoveWriteGroup() throws Exception {

    }

    @Test
    public void testAddWriteGroup() throws Exception {

    }

    @Test
    public void testSetWriteUser() throws Exception {

    }

    @Test
    public void testHasReadPermission() throws Exception {

    }

    @Test
    public void testHasWritePermission() throws Exception {

    }

    @Test
    public void testGetReadUser() throws Exception {

    }

    @Test
    public void testGetWriteUser() throws Exception {

    }

    @Test
    public void testGetReadGroup() throws Exception {

    }

    @Test
    public void testGetWriteGroup() throws Exception {
        String email = "testGetWriteGroup@TESTER.org";
        String email2 = "testGetWriteGroupOther@TESTER.org";

        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        accountController.createNewAccount("", "TEST", "T", email2, null, "");
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account otherAccount = accountController.getByEmail(email2);
        Assert.assertNotNull(otherAccount);

        // create entry
        Strain strain = new Strain();
        EntryController entryController = new EntryController();
        Assert.assertNotNull(entryController.createEntry(account, strain, false, null));

        controller.getWriteGroup(account, strain);

    }
}
