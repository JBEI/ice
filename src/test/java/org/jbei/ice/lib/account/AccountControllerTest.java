/**
 *
 */
package org.jbei.ice.lib.account;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link AccountController}
 *
 * @author Hector Plahar
 */
public class AccountControllerTest {

    private AccountController controller;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new AccountController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.rollbackTransaction();
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
    public void testResetPassword() throws Exception {
        Account account = AccountCreator.createTestAccount("testResetPassword", false);
        String oldPassword = account.getPassword();
        controller.resetPassword(account.getEmail(), false, null);
        Assert.assertFalse(oldPassword.equalsIgnoreCase(account.getPassword()));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        Account account = AccountCreator.createTestAccount("testUpdatePassword", false);
        controller.updatePassword(account.getEmail(), "p455W0rd");
        Assert.assertTrue(controller.isValidPassword(account, "p455W0rd"));
        Assert.assertFalse(controller.isValidPassword(account, "p455W0rd1"));
    }

    @Test
    public void testCreateNewAccount() throws Exception {
        AccountTransfer info = new AccountTransfer();
        info.setEmail("testCreateNewAccount");
        info.setFirstName("Test");
        info.setLastName("Test");
        String password = controller.createNewAccount(info, false);
        Assert.assertNotNull(password);
    }

    @Test
    public void testGetByEmail() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetByEmail", false);
        account = controller.getByEmail(account.getEmail());
        Assert.assertNotNull(account);
    }

    @Test
    public void testSave() throws Exception {
        Account account = new Account();
        account.setFirstName("First");
        account.setLastName("Last");
        account.setDescription("Desc");
        account.setInitials("FL");
        account.setPassword("plom");
        account.setIp("");
        account.setInstitution("");
        account.setEmail("testGet@TEST");
        Assert.assertNotNull(controller.save(account));
    }

    @Test
    public void testIsAdministrator() throws Exception {
        Account account = AccountCreator.createTestAccount("testIsAdministratorNonAdmin", false);
        Assert.assertFalse(controller.isAdministrator(account));

        account = AccountCreator.createTestAccount("testIsAdministratorAdmin", true);
        Assert.assertTrue(controller.isAdministrator(account));
    }

    @Test
    public void testIsValidPassword() throws Exception {
        Account account = AccountCreator.createTestAccount("testIsValidPassword", false);
        controller.updatePassword(account.getEmail(), "p455W0rd");
        Assert.assertTrue(controller.isValidPassword(account, "p455W0rd"));
        Assert.assertFalse(controller.isValidPassword(account, "p455W0rd1"));
    }

    @Test
    public void testGetAccountBySessionKey() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetAccountBySessionKey", false);
        controller.updatePassword(account.getEmail(), "p4ssw0rd");
        AccountTransfer info = controller.authenticate(new AccountTransfer(account.getEmail(), "p4ssw0rd"));
        Assert.assertNotNull(info);
        Assert.assertFalse(info.getSessionId().isEmpty());
        Account sessIdAccount = controller.getAccountBySessionKey(info.getSessionId());
        Assert.assertNotNull(sessIdAccount);
        Assert.assertEquals(account.getEmail(), sessIdAccount.getEmail());
        Assert.assertEquals(account.getSalt(), sessIdAccount.getSalt());
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
