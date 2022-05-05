/**
 *
 */
package org.jbei.ice.lib.account;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
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
    public void testResetPassword() throws Exception {
        Account account = AccountCreator.createTestAccount("testResetPassword", false);
        String oldPassword = account.getPassword();
        Assert.assertTrue(controller.resetPassword(account.getEmail()));
        Assert.assertFalse(oldPassword.equalsIgnoreCase(account.getPassword()));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        Account account = AccountCreator.createTestAccount("testUpdatePassword", false);
        AccountTransfer transfer = account.toDataTransferObject();
        transfer.setPassword("p455W0rd");
        controller.updatePassword(account.getEmail(), transfer.getId(), transfer);
    }

    @Test
    public void testCreateNewAccount() throws Exception {
        AccountTransfer info = new AccountTransfer();
        info.setEmail("testCreateNewAccount");
        info.setFirstName("Test");
        info.setLastName("Test");
        info = controller.createNewAccount(info, false);
        Assert.assertNotNull(info.getPassword());
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
        Assert.assertFalse(controller.isAdministrator(account.getEmail()));

        account = AccountCreator.createTestAccount("testIsAdministratorAdmin", true);
        Assert.assertTrue(controller.isAdministrator(account.getEmail()));
    }

    @Test
    public void testGetAccountBySessionKey() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetAccountBySessionKey", false);
        AccountTransfer transfer = account.toDataTransferObject();
        transfer.setPassword("p455W0rd");
        controller.updatePassword(account.getEmail(), transfer.getId(), transfer);
        AccountTransfer info = controller.authenticate(new AccountTransfer(account.getEmail(), "p455W0rd"));
        Assert.assertNotNull(info);
        Assert.assertFalse(info.getSessionId().isEmpty());
        AccountTransfer sessIdAccount = controller.getAccountBySessionKey(info.getSessionId());
        Assert.assertNotNull(sessIdAccount);
        Assert.assertEquals(account.getEmail(), sessIdAccount.getEmail());
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
