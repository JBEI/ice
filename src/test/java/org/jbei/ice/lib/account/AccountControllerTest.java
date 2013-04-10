/**
 *
 */
package org.jbei.ice.lib.account;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link AccountController}
 *
 * @author Hector Plahar
 */
public class AccountControllerTest {

    private AccountController controller;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        controller = new AccountController();
        HibernateHelper.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testGet() throws Exception {
        Account account = controller.get(0);
        Assert.assertNull(account);

        // create new account
        account = new Account();
        account.setFirstName("First");
        account.setLastName("Last");
        account.setDescription("Desc");
        account.setInitials("FL");
        account.setIsSubscribed(1);
        account.setPassword("plom");
        account.setIp("");
        account.setInstitution("");
        account.setEmail("testGet@TEST");
        Assert.assertNotNull(controller.save(account));

        // test get
        account = controller.get(account.getId());
        Assert.assertNotNull(account);
        Assert.assertEquals("First", account.getFirstName());
        Assert.assertEquals("Last", account.getLastName());
        Assert.assertEquals("Desc", account.getDescription());
        Assert.assertEquals("testGet@TEST", account.getEmail());
        Assert.assertEquals("FL", account.getInitials());
    }

    @Test
    public void testResetPassword() {
    }

    @Test
    public void testUpdatePassword() {
    }

    @Test
    public void testCreateNewAccount() {
    }

    @Test
    public void testGetByEmail() {
    }

    @Test
    public void testSave() {
    }

    @Test
    public void testIsModerator() {
    }

    @Test
    public void testIsValidPassword() {
    }

    @Test
    public void testGetAccountBySessionKey() {
    }

    @Test
    public void testGetAccountPreferences() {
    }

    @Test
    public void testAuthenticateStringStringString() {
    }

    @Test
    public void testAuthenticateStringString() {
    }

    @Test
    public void testIsAuthenticated() {
    }

    @Test
    public void testDeauthenticate() {
    }

    @Test
    public void testGetSystemAccount() {
    }

    @Test
    public void testResetUserPassword() {
    }

    @Test
    public void testGetMatchingAccounts() {
    }

    @Test
    public void testGetAccountByAuthToken() {
    }
}
