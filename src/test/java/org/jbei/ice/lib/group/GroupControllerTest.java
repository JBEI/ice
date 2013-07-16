package org.jbei.ice.lib.group;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.shared.dto.AccountInfo;
import org.jbei.ice.lib.shared.dto.group.GroupInfo;
import org.jbei.ice.lib.shared.dto.group.GroupType;

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
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("test Group");
        groupInfo.setDescription("test");
        groupInfo = controller.createGroup(account, groupInfo);
        Assert.assertNotNull(groupInfo);
        Group group = controller.getGroupByUUID(groupInfo.getUuid());
        Assert.assertNotNull(group);
    }

    @Test
    public void testGetGroupById() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetGroupById", false);
        GroupInfo group1 = new GroupInfo();
        group1.setDescription("test1");
        group1.setType(GroupType.PRIVATE);
        group1.setLabel("label1");
        long id = controller.createGroup(account, group1).getId();
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
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("test Group");
        groupInfo.setDescription("test");
        groupInfo = controller.createGroup(account, groupInfo);
        Assert.assertNotNull(groupInfo);
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
        GroupInfo g1 = new GroupInfo();
        g1.setDescription("desc");
        g1.setLabel("label");
        Assert.assertNotNull(controller.createGroup(account, g1));

        GroupInfo g2 = new GroupInfo();
        g2.setDescription("desc");
        g2.setLabel("myg2");
        Assert.assertNotNull(controller.createGroup(account, g2));

        Set<Group> groups = controller.getMatchingGroups("myg", 10);
        Assert.assertNotNull(groups);
        Assert.assertEquals(1, groups.size());
    }

    @Test
    public void testRetrieveGroupMembers() throws Exception {
        Account a1 = AccountCreator.createTestAccount("testRetrieveGroupMembers1", false);
        Account a2 = AccountCreator.createTestAccount("testRetrieveGroupMembers2", false);
        Account a3 = AccountCreator.createTestAccount("testRetrieveGroupMembers3", false);

        GroupInfo info = new GroupInfo();
        info.setDescription("desc");
        info.setLabel("label");
        info.setType(GroupType.PRIVATE);

        // create group
        info = controller.createGroup(a1, info);
        Assert.assertNotNull(info);

        ArrayList<AccountInfo> infos = new ArrayList<>();
        infos.add(Account.toDTO(a2));
        infos.add(Account.toDTO(a3));

        infos = controller.setGroupMembers(a1, info, infos);
        Assert.assertNotNull(infos);
        Assert.assertTrue(infos.size() == 2);
        ArrayList<AccountInfo> list = controller.retrieveGroupMembers(info.getUuid());
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testSetGroupMembers() throws Exception {
        Account a1 = AccountCreator.createTestAccount("testSetGroupMembers1", false);
        Account a2 = AccountCreator.createTestAccount("testSetGroupMembers2", false);
        Account a3 = AccountCreator.createTestAccount("testSetGroupMembers3", false);

        GroupInfo info = new GroupInfo();
        info.setDescription("desc");
        info.setLabel("label");
        info.setType(GroupType.PRIVATE);

        // create group
        info = controller.createGroup(a1, info);
        Assert.assertNotNull(info);

        ArrayList<AccountInfo> infos = new ArrayList<>();
        infos.add(Account.toDTO(a2));
        infos.add(Account.toDTO(a3));

        infos = controller.setGroupMembers(a1, info, infos);
        Assert.assertNotNull(infos);
        Assert.assertTrue(infos.size() == 2);
    }
}
