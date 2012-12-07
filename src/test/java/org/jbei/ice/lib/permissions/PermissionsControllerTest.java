package org.jbei.ice.lib.permissions;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Strain;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class PermissionsControllerTest {

    private PermissionsController controller;

    @Before
    public void setUp() throws Exception {
//        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new PermissionsController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
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
        Assert.assertFalse(controller.hasReadPermission(otherAccount, strain));
        // add read account permission
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
    public void test() throws Exception {
//        Account account = new AccountController().getByEmail("haplahar@lbl.gov");
//        Entry entry = new EntryController().get(account, 27340);
//        ArrayList<PermissionInfo> permissionInfos = controller.retrieveSetEntryPermissions(account, entry);
//        System.out.println(permissionInfos.size());
////        controller.addReadUser(account, entry, account);
//        int count = controller.clearPermissions(account, entry);
//        System.out.println(count);
//        permissionInfos = controller.retrieveSetEntryPermissions(account, entry);
//        System.out.println(permissionInfos.size());
        ApplicationController.initializeHibernateSearch();
    }
}
