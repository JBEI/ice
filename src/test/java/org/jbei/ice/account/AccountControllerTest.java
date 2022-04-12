/**
 *
 */
package org.jbei.ice.account;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.AccountModel;
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
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
        controller = new AccountController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.rollbackTransaction();
    }

    @Test
    public void testResetPassword() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testResetPassword", false);
        String oldPassword = account.getPassword();
        Assert.assertTrue(controller.resetPassword(account.getEmail()));
        Assert.assertFalse(oldPassword.equalsIgnoreCase(account.getPassword()));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testUpdatePassword", false);
        Account transfer = account.toDataTransferObject();
        transfer.setPassword("p455W0rd");
        controller.updatePassword(account.getEmail(), transfer.getId(), transfer);
    }

    @Test
    public void testCreateNewAccount() throws Exception {
        Account info = new Account();
        info.setEmail("testCreateNewAccount");
        info.setFirstName("Test");
        info.setLastName("Test");
        info = controller.createNewAccount(info, false);
        Assert.assertNotNull(info.getPassword());
    }

    @Test
    public void testGetByEmail() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetByEmail", false);
        account = controller.getByEmail(account.getEmail());
        Assert.assertNotNull(account);
    }

    @Test
    public void testSave() throws Exception {
        AccountModel account = new AccountModel();
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
        AccountModel account = AccountCreator.createTestAccount("testIsAdministratorNonAdmin", false);
        Assert.assertFalse(controller.isAdministrator(account.getEmail()));

        account = AccountCreator.createTestAccount("testIsAdministratorAdmin", true);
        Assert.assertTrue(controller.isAdministrator(account.getEmail()));
    }

    @Test
    public void testGetAccountBySessionKey() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetAccountBySessionKey", false);
        Account transfer = account.toDataTransferObject();
        transfer.setPassword("p455W0rd");
        controller.updatePassword(account.getEmail(), transfer.getId(), transfer);
        Account info = controller.authenticate(new Account(account.getEmail(), "p455W0rd"));
        Assert.assertNotNull(info);
        Assert.assertFalse(info.getSessionId().isEmpty());
        Account sessIdAccountModel = controller.getAccountBySessionKey(info.getSessionId());
        Assert.assertNotNull(sessIdAccountModel);
        Assert.assertEquals(account.getEmail(), sessIdAccountModel.getEmail());
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
