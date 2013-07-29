package org.jbei.ice.lib.permissions;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.EntryCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import org.junit.After;
import org.junit.Assert;
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
        Account account = AccountCreator.createTestAccount("testAddPermission", false);
        Account account2 = AccountCreator.createTestAccount("testAddPermission2", false);
        Account account3 = AccountCreator.createTestAccount("testAddPermission3", false);
        Strain strain = EntryCreator.createTestStrain(account);
        AccessPermission access = new AccessPermission(AccessPermission.Article.ACCOUNT, account2.getId(),
                                                       AccessPermission.Type.READ_ENTRY, strain.getId(),
                                                       account.getFullName());

        // requesting access for self. expected to fail
        boolean failed = false;
        try {
            controller.addPermission(account2, access);
        } catch (Exception e) {
            failed = true;
        }
        Assert.assertTrue(failed);

        // strain owner requesting read permission
        Assert.assertNotNull(controller.addPermission(account, access));

        // account with read permission requesting for another; expected to fail
        failed = false;
        try {
            controller.addPermission(account2, new AccessPermission(AccessPermission.Article.ACCOUNT,
                                                                    account3.getId(),
                                                                    AccessPermission.Type.READ_ENTRY,
                                                                    strain.getId(),
                                                                    account.getFullName()));
        } catch (Exception e) {
            failed = true;
        }
        Assert.assertTrue(failed);

        // account with read permission requesting write for self (should fail)
        failed = false;
        try {
            controller.addPermission(account2, new AccessPermission(AccessPermission.Article.ACCOUNT,
                                                                    account2.getId(),
                                                                    AccessPermission.Type.READ_ENTRY,
                                                                    strain.getId(),
                                                                    account.getFullName()));
        } catch (Exception e) {
            failed = true;
        }
        Assert.assertTrue(failed);

        // owner gives write permission to account3, who then gives write permission to account 2
        Assert.assertNotNull(controller.addPermission(account, new AccessPermission(AccessPermission.Article.ACCOUNT,
                                                                                    account3.getId(),
                                                                                    AccessPermission.Type.WRITE_ENTRY,
                                                                                    strain.getId(),
                                                                                    account.getFullName())));
        Assert.assertNotNull(controller.addPermission(account3, new AccessPermission(AccessPermission.Article.ACCOUNT,
                                                                                     account2.getId(),
                                                                                     AccessPermission.Type.WRITE_ENTRY,
                                                                                     strain.getId(),
                                                                                     account.getFullName())));
    }

    @Test
    public void testRemovePermission() throws Exception {
        Account account = AccountCreator.createTestAccount("testRemovePermission", false);
        Account account2 = AccountCreator.createTestAccount("testRemovePermission2", false);

        Strain strain = EntryCreator.createTestStrain(account);

        // read permission for account2
        AccessPermission access = new AccessPermission(AccessPermission.Article.ACCOUNT, account2.getId(),
                                                       AccessPermission.Type.READ_ENTRY, strain.getId(),
                                                       account.getFullName());
        Assert.assertNotNull(controller.addPermission(account, access));
        Assert.assertTrue(controller.accountHasReadPermission(account2, strain));

        // remove just added permission
        controller.removePermission(account, access);
        Assert.assertFalse(controller.accountHasReadPermission(account2, strain));
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
