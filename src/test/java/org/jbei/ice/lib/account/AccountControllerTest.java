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
 * @author Hector Plahar
 */
public class AccountControllerTest {

    private AccountController controller;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        controller = new AccountController();
        HibernateHelper.beginTransaction();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        HibernateHelper.rollbackTransaction();
    }

    /**
     * Test method for {@link org.jbei.ice.lib.account.AccountController#get(long)}.
     */
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

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#resetPassword(java.lang.String, boolean, java.lang.String)}
     */
    @Test
    public void testResetPassword() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#updatePassword(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testUpdatePassword() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#createNewAccount(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCreateNewAccount() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#getByEmail(java.lang.String)}.
     */
    @Test
    public void testGetByEmail() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#save(org.jbei.ice.lib.account.model.Account)}.
     */
    @Test
    public void testSave() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#isAdministrator(org.jbei.ice.lib.account.model.Account)}
     * .
     */
    @Test
    public void testIsModerator() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#isValidPassword(org.jbei.ice.lib.account.model.Account,
     * java.lang.String)}
     * .
     */
    @Test
    public void testIsValidPassword() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#getAccountBySessionKey(java.lang.String)}.
     */
    @Test
    public void testGetAccountBySessionKey() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#getAccountPreferences(org.jbei.ice.lib.account.model.Account)}
     * .
     */
    @Test
    public void testGetAccountPreferences() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#authenticate(java.lang.String, java.lang.String,
     * java.lang.String)}
     * .
     */
    @Test
    public void testAuthenticateStringStringString() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#authenticate(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testAuthenticateStringString() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#isAuthenticated(java.lang.String)}.
     */
    @Test
    public void testIsAuthenticated() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#deauthenticate(java.lang.String)}.
     */
    @Test
    public void testDeauthenticate() {
    }

    /**
     * Test method for {@link org.jbei.ice.lib.account.AccountController#getSystemAccount()}.
     */
    @Test
    public void testGetSystemAccount() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#resetUserPassword(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testResetUserPassword() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#getMatchingAccounts(java.lang.String, int)}
     * .
     */
    @Test
    public void testGetMatchingAccounts() {
    }

    /**
     * Test method for
     * {@link org.jbei.ice.lib.account.AccountController#getAccountByAuthToken(java.lang.String)}.
     */
    @Test
    public void testGetAccountByAuthToken() {
    }
}
