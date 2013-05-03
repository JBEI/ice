package org.jbei.ice.lib.group;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class GroupControllerTest {

    private GroupController controller;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateHelper.beginTransaction();
        controller = new GroupController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testGetGroupByUUID() throws Exception {
    }

    @Test
    public void testGetGroupById() throws Exception {
    }

    @Test
    public void testSave() throws Exception {
    }

    @Test
    public void testCreate() throws Exception {
        Group group = controller.createOrRetrievePublicGroup();
    }

    @Test
    public void testCreateOrRetrievePublicGroup() throws Exception {
        Group group = controller.createOrRetrievePublicGroup();
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getType() == GroupType.SYSTEM);
    }

    @Test
    public void testGetMatchingGroups() throws Exception {

    }

    @Test
    public void testGetAllGroups() throws Exception {

    }

    @Test
    public void testRetrieveGroupMembers() throws Exception {

    }

    @Test
    public void testAddMemberToGroup() throws Exception {
        Account account = createTestAccount("testAddMemberToGroup", false);
        GroupInfo info = new GroupInfo();
        info.setDescription("test");
        info.setLabel("test");
        info.setType(GroupType.PRIVATE);
        info = controller.createGroup(account, info);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getMembers().size() == 0);
        AccountController accountController = new AccountController();
        accountController.createNewAccount("Test", "Tester", "TT", "test@tester", "LBL", "test account");
        controller.addMemberToGroup(info.getId(), "test@tester");
        Assert.assertTrue(controller.getGroupById(info.getId()).getMembers().size() == 1);
    }

    @Test
    public void testSetGroupMembers() throws Exception {
    }

    protected Account createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountController accountController = new AccountController();
        Account account = accountController.getByEmail(email);
        if (account != null)
            throw new Exception("duplicate account");

        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
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
