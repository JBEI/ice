package org.jbei.ice.lib.group;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.web.RemoteUser;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.RemotePartner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class GroupsTest {

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

//    @Test
//    public void testGet() throws Exception {
//
//    }
//
//    @Test
//    public void testGet1() throws Exception {
//
//    }
//
//    @Test
//    public void testGetMatchingGroups() throws Exception {
//
//    }

    @Test
    public void testAddGroup() throws Exception {
        Account account = AccountCreator.createTestAccount("GroupsTest.testAddGroup", false);
        String email = account.getEmail();

        UserGroup group = new UserGroup();
        group.setLabel("label");
        group.setDescription("description");

        Account account1 = AccountCreator.createTestAccount("GroupsTest.testAddGroup2", false);
        group.getMembers().add(account1.toDataTransferObject());

        // add remote account
        RemoteUser remoteUser = new RemoteUser();
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail("Remote.GroupsTest.testAddGroup3");
        remoteUser.setUser(accountTransfer);

        // create remote partner
        RemotePartner partner = new RemotePartner();
        partner.setUrl("registry-test3.jbei.org");
        partner = DAOFactory.getRemotePartnerDAO().create(partner);
        remoteUser.setPartner(partner.toDataTransferObject());

        group.getRemoteMembers().add(remoteUser);

        Groups groups = new Groups(email);
        UserGroup result = groups.addGroup(group);
        Assert.assertNotNull(result);

        result = groups.getGroupMembers(result.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getMembers().size());
        Assert.assertEquals(1, result.getRemoteMembers().size());
    }

    //    @Test
//    public void testGetGroupMembers() throws Exception {
//
//    }
//
    @Test
    public void testUpdate() throws Exception {
        Account account = AccountCreator.createTestAccount("GroupsTest.testUpdate", false);

        // create local test accounts
        Account account1 = AccountCreator.createTestAccount("GroupsTest.testUpdate1", false);
        Account account2 = AccountCreator.createTestAccount("GroupsTest.testUpdate2", false);
        Account account3 = AccountCreator.createTestAccount("GroupsTest.testUpdate3", false);

        // create remote test accounts
        RemotePartner partner = new RemotePartner();
        partner.setUrl("registry-test2.jbei.org");
        partner = DAOFactory.getRemotePartnerDAO().create(partner);

        RemoteUser remoteUser1 = new RemoteUser();
        remoteUser1.setUser(new AccountTransfer("Remote.GroupsTest.testUpdate1", ""));
        remoteUser1.setPartner(partner.toDataTransferObject());

        RemoteUser remoteUser2 = new RemoteUser();
        remoteUser2.setUser(new AccountTransfer("Remote.GroupsTest.testUpdate2", ""));
        remoteUser2.setPartner(partner.toDataTransferObject());

        RemoteUser remoteUser3 = new RemoteUser();
        remoteUser3.setUser(new AccountTransfer("Remote.GroupsTest.testUpdate3", ""));
        remoteUser3.setPartner(partner.toDataTransferObject());

        // create group with account1, account2 and remote1, remote2 as a members
        UserGroup group = new UserGroup();
        group.setLabel("label");
        group.setDescription("description");
        group.getMembers().add(account1.toDataTransferObject());
        group.getMembers().add(account2.toDataTransferObject());
        group.getRemoteMembers().add(remoteUser1);
        group.getRemoteMembers().add(remoteUser2);

        Groups groups = new Groups(account.getEmail());
        UserGroup result = groups.addGroup(group);
        Assert.assertNotNull(result);

        // get members and test
        result = groups.getGroupMembers(result.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.getMembers().size());
        Assert.assertEquals(2, result.getRemoteMembers().size());

        // update group to account2 as the only local and 1 remote account
        result.getMembers().clear();
        result.getRemoteMembers().clear();

        result.getMembers().add(account3.toDataTransferObject());
        result.getRemoteMembers().add(remoteUser3);

        // update
        result.setLabel("updated label");
        result.setDescription("updated description");
        Assert.assertTrue(groups.update(result.getId(), result));

        // get members and test
        group = groups.getGroupMembers(result.getId());
        Assert.assertEquals("updated label", group.getLabel());
        Assert.assertEquals("updated description", group.getDescription());
        Assert.assertEquals(1, group.getMembers().size());
        Assert.assertEquals(account3.getEmail(), group.getMembers().get(0).getEmail());
        Assert.assertEquals(1, group.getRemoteMembers().size());
        Assert.assertEquals(remoteUser3.getUser().getEmail(), group.getRemoteMembers().get(0).getUser().getEmail());
    }
//
//    @Test
//    public void testSetGroupMembers() throws Exception {
//
//    }
}