package org.jbei.ice.lib.group;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.user.User;

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
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testGetGroupByUUID() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetGroupByUUID", false);
        UserGroup userGroup = new UserGroup();
        userGroup.setLabel("test Group");
        userGroup.setDescription("test");
        userGroup = controller.createGroup(account, userGroup);
        Assert.assertNotNull(userGroup);
        Group group = controller.getGroupByUUID(userGroup.getUuid());
        Assert.assertNotNull(group);
    }

    @Test
    public void testGetGroupById() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetGroupById", false);
        UserGroup userGroup1 = new UserGroup();
        userGroup1.setDescription("test1");
        userGroup1.setType(GroupType.PRIVATE);
        userGroup1.setLabel("label1");
        long id = controller.createGroup(account, userGroup1).getId();
        Assert.assertNotNull(controller.getGroupById(id));
    }

    @Test
    public void testSave() throws Exception {
        Account account = AccountCreator.createTestAccount("testSave", false);
        Group group = new Group();
        group.setAutoJoin(false);
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
        userGroup = controller.createGroup(account, userGroup);
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
        Assert.assertNotNull(controller.createGroup(account, g1));

        UserGroup g2 = new UserGroup();
        g2.setDescription("desc");
        g2.setLabel("myg2");
        Assert.assertNotNull(controller.createGroup(account, g2));

        Account account2 = AccountCreator.createTestAccount("testGetMatchingGroups2", false);
        UserGroup g3 = new UserGroup();
        g3.setDescription("desc");
        g3.setLabel("myg3");
        Assert.assertNotNull(controller.createGroup(account2, g3));

        Set<Group> groups = controller.getMatchingGroups(account, "myg", 10);
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
        user = controller.createGroup(a1, user);
        Assert.assertNotNull(user);

        ArrayList<User> infos = new ArrayList<>();
        infos.add(Account.toDTO(a2));
        infos.add(Account.toDTO(a3));

        infos = controller.setGroupMembers(a1, user, infos);
        Assert.assertNotNull(infos);
        Assert.assertTrue(infos.size() == 2);
        ArrayList<User> list = controller.retrieveGroupMembers(user.getUuid());
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
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
        user = controller.createGroup(a1, user);
        Assert.assertNotNull(user);

        ArrayList<User> infos = new ArrayList<>();
        infos.add(Account.toDTO(a2));
        infos.add(Account.toDTO(a3));

        infos = controller.setGroupMembers(a1, user, infos);
        Assert.assertNotNull(infos);
        Assert.assertTrue(infos.size() == 2);
    }
}
