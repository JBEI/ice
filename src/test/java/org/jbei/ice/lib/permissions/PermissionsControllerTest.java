package org.jbei.ice.lib.permissions;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.shared.dto.user.User;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class PermissionsControllerTest {

    private PermissionsController controller;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
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
        User info = new User();
        info.setFirstName("Ter");
        info.setLastName("TEST");
        info.setEmail(email);
        String pass = accountController.createNewAccount(info, false);
        Assert.assertNotNull(pass);

        User info2 = new User();
        info2.setFirstName("T");
        info2.setLastName("TEST");
        info2.setEmail(email2);
        accountController.createNewAccount(info2, false);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        Account otherAccount = accountController.getByEmail(email2);
        Assert.assertNotNull(otherAccount);
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
    }
}
