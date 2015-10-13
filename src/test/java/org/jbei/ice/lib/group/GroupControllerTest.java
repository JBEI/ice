package org.jbei.ice.lib.group;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;
import org.junit.*;

import java.util.Set;

/**
 * @author Hector Plahar
 */
public class GroupControllerTest {

    private GroupController controller;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateUtil.beginTransaction();
        controller = new GroupController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testGetGroupById() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetGroupById", false);
        UserGroup userGroup1 = new UserGroup();
        userGroup1.setDescription("test1");
        userGroup1.setType(GroupType.PRIVATE);
        userGroup1.setLabel("label1");
        long id = controller.createGroup(account.getEmail(), userGroup1).getId();
        Assert.assertNotNull(controller.getGroupById(account.getEmail(), id));
    }

    @Test
    public void testSave() throws Exception {
        Account account = AccountCreator.createTestAccount("testSave", false);
        Group group = new Group();
        group.setOwner(account);
        group.setLabel("group label");
        group.setDescription("group description");
        Assert.assertNotNull(controller.save(group));
    }

    @Test
    public void testCreate() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreate", false);
        UserGroup userGroup = new UserGroup();
        userGroup.setLabel("test Group");
        userGroup.setDescription("test");
        userGroup = controller.createGroup(account.getEmail(), userGroup);
        Assert.assertNotNull(userGroup);
    }

    @Test
    public void testCreateOrRetrievePublicGroup() throws Exception {
        Group group = controller.createOrRetrievePublicGroup();
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getType() == GroupType.SYSTEM);
    }

    @Test
    public void testGetMatchingGroups() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetMatchingGroups", false);
        UserGroup g1 = new UserGroup();
        g1.setDescription("desc");
        g1.setLabel("label");
        g1 = controller.createGroup(account.getEmail(), g1);
        Assert.assertNotNull(g1);

        Group group1 = DAOFactory.getGroupDAO().get(g1.getId());
        account.getGroups().add(group1);
        Assert.assertNotNull(group1);

        UserGroup g2 = new UserGroup();
        g2.setDescription("desc");
        g2.setLabel("myg2");
        g2 = controller.createGroup(account.getEmail(), g2);
        Assert.assertNotNull(g2);
        Group group2 = DAOFactory.getGroupDAO().get(g2.getId());
        Assert.assertNotNull(group2);
        account.getGroups().add(group2);

        // save to add groups to account
        DAOFactory.getAccountDAO().create(account);

        Account account2 = AccountCreator.createTestAccount("testGetMatchingGroups2", false);
        UserGroup g3 = new UserGroup();
        g3.setDescription("desc");
        g3.setLabel("myg3");
        Assert.assertNotNull(controller.createGroup(account2.getEmail(), g3));

        Set<Group> groups = controller.getMatchingGroups(account.getEmail(), "myg", 10);
        Assert.assertNotNull(groups);
        Assert.assertEquals(1, groups.size());
    }

    @Test
    public void testRetrieveGroupMembers() throws Exception {
        Account a1 = AccountCreator.createTestAccount("testRetrieveGroupMembers1", false);
        Account a2 = AccountCreator.createTestAccount("testRetrieveGroupMembers2", false);
        Account a3 = AccountCreator.createTestAccount("testRetrieveGroupMembers3", false);

        UserGroup user = new UserGroup();
        user.setDescription("desc");
        user.setLabel("label");
        user.setType(GroupType.PRIVATE);

        // create group
        user = controller.createGroup(a1.getEmail(), user);
        Assert.assertNotNull(user);
    }

    @Test
    public void testSetGroupMembers() throws Exception {
        Account a1 = AccountCreator.createTestAccount("testSetGroupMembers1", false);
        Account a2 = AccountCreator.createTestAccount("testSetGroupMembers2", false);
        Account a3 = AccountCreator.createTestAccount("testSetGroupMembers3", false);

        UserGroup user = new UserGroup();
        user.setDescription("desc");
        user.setLabel("label");
        user.setType(GroupType.PRIVATE);

        // create group
        user = controller.createGroup(a1.getEmail(), user);
        Assert.assertNotNull(user);
    }
}
