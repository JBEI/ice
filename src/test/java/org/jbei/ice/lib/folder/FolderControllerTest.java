package org.jbei.ice.lib.folder;

import junit.framework.Assert;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.folder.FolderDetails;
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
        Account account = createTestAccount("testCreateNewFolder", false);
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

    protected Account createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountController accountController = new AccountController();
        Account account = accountController.getByEmail(email);
        if (account != null)
            throw new Exception("duplicate account");

        AccountInfo info = new AccountInfo();
        info.setFirstName("");
        info.setLastName("TEST");
        info.setEmail(email);
        String pass = accountController.createNewAccount(info, false);
        Assert.assertNotNull(pass);
        account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        if (admin) {
            account.setType(AccountType.ADMIN);
            accountController.save(account);
        }
        return account;
    }
}
