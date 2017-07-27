package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Hector Plahar
 */
public class GroupDAOTest extends HibernateRepositoryTest {

    private GroupDAO dao = new GroupDAO();

    private Group createGroup(String uuid, GroupType type) {
        Group group = new Group();
        group.setCreationTime(new Date());
        group.setLabel("new group");
        group.setDescription("this is a new group");
        if (type == null)
            type = GroupType.PRIVATE;

        group.setType(type);
        if (uuid != null)
            group.setUuid(uuid);
        return dao.create(group);
    }

    @Test
    public void testGet() throws Exception {
        Group group = createGroup(UUID.randomUUID().toString(), null);
        Group result = dao.get(group.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), group.getId());
        Assert.assertEquals(result.getUuid(), group.getUuid());
    }

    @Test
    public void testGetByUUID() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Group group = createGroup(uuid, null);
        Assert.assertNotNull(group);
        Group result = dao.getByUUID(uuid);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), group.getId());
        Assert.assertEquals(result.getUuid(), group.getUuid());

        // test fake retrieve
        Assert.assertNull(dao.getByUUID(UUID.randomUUID().toString()));
    }

    @Test
    public void testGetMemberCount() throws Exception {
        Group group = createGroup(UUID.randomUUID().toString(), null);
        long count = dao.getMemberCount(group.getUuid());
        Assert.assertEquals(0, count);

        Random random = new Random();
        int x = random.nextInt(30);
        for (int i = 0; i < x; i += 1) {
            Account account1 = AccountCreator.createTestAccount("GroupDAOTest.testGetMemberCount" + i, false);
            account1.getGroups().add(group);
            DAOFactory.getAccountDAO().update(account1);
            Assert.assertEquals(i + 1, dao.getMemberCount(group.getUuid()));
        }
        Assert.assertEquals(x, dao.getMemberCount(group.getUuid()));
    }

    @Test
    public void testGetMatchingGroups() throws Exception {
        // todo
    }

    @Test
    public void testRetrieveMemberGroups() throws Exception {
        Account account = AccountCreator.createTestAccount("GroupDAOTest.testRetrieveMemberGroups", false);
        Random random = new Random();
        int count = random.nextInt(5);
        List<String> uuids = new ArrayList<>(count);
        for (int i = 0; i < count; i += 1) {
            Group group = createGroup(UUID.randomUUID().toString(), null);
            account.getGroups().add(group);
            uuids.add(group.getUuid());
        }
        DAOFactory.getAccountDAO().update(account);
        List<Group> groups = dao.retrieveMemberGroups(account);
        Assert.assertNotNull(groups);
        Assert.assertEquals(count, groups.size());
        for (Group group : groups) {
            Assert.assertTrue(uuids.contains(group.getUuid()));
        }
    }

    @Test
    public void testGetMemberGroupUUIDs() throws Exception {
        Account account = AccountCreator.createTestAccount("GroupDAOTest.testGetMemberGroupUUIDs", false);
        Random random = new Random();
        int count = random.nextInt(5) + 1;
        List<String> uuids = new ArrayList<>(count);
        for (int i = 0; i < count; i += 1) {
            Group group = createGroup(UUID.randomUUID().toString(), null);
            account.getGroups().add(group);
            uuids.add(group.getUuid());
        }
        DAOFactory.getAccountDAO().update(account);
        List<String> groups = dao.getMemberGroupUUIDs(account);
        Assert.assertNotNull(groups);
        Assert.assertEquals(count, groups.size());
        Assert.assertTrue(uuids.removeAll(groups));
        Assert.assertTrue(uuids.isEmpty());
    }

    @Test
    public void testGetGroupsByType() throws Exception {
        Random random = new Random();
        int publicCount = random.nextInt(15);

        // create count public groups
        List<String> publicUUIDs = new ArrayList<>(publicCount);
        for (int i = 0; i < publicCount; i += 1) {
            Group group = createGroup(UUID.randomUUID().toString(), GroupType.PUBLIC);
            publicUUIDs.add(group.getUuid());
        }

        List<Group> results = dao.getGroupsByType(GroupType.PUBLIC, 0, 1000);
        Assert.assertTrue(publicCount <= results.size());
        for (Group group : results) {
            publicUUIDs.remove(group.getUuid());
        }
        Assert.assertTrue(publicUUIDs.size() + " public uuids remain", publicUUIDs.isEmpty());

        int privateCount = random.nextInt(15);
        List<String> privateUUIDs = new ArrayList<>(privateCount);
        for (int i = 0; i < privateCount; i += 1) {
            Group group = createGroup(UUID.randomUUID().toString(), null);
            privateUUIDs.add(group.getUuid());
        }

        results = dao.getGroupsByType(GroupType.PRIVATE, 0, 1000);
        Assert.assertTrue(privateCount <= results.size());
        for (Group group : results) {
            privateUUIDs.remove(group.getUuid());
        }
        Assert.assertTrue(privateUUIDs.size() + " private uuids remain", privateUUIDs.isEmpty());
    }

    @Test
    public void testGetGroupsByTypeCount() throws Exception {
        Random random = new Random();
        int publicCount = random.nextInt(30);

        // create count public groups
        for (int i = 0; i < publicCount; i += 1) {
            createGroup(UUID.randomUUID().toString(), GroupType.PUBLIC);
        }

        int privateCount = random.nextInt(30);
        for (int i = 0; i < privateCount; i += 1) {
            createGroup(UUID.randomUUID().toString(), null);
        }

        Assert.assertTrue(publicCount <= dao.getGroupsByTypeCount(GroupType.PUBLIC));
        Assert.assertTrue(privateCount <= dao.getGroupsByTypeCount(GroupType.PRIVATE));
    }

    @Test
    public void testGetGroupsBy() throws Exception {
        String uid = UUID.randomUUID().toString();
        Group group = createGroup(uid, GroupType.PUBLIC);
        group.setAutoJoin(true);
        dao.update(group);

        createGroup(UUID.randomUUID().toString(), GroupType.PUBLIC);
        createGroup(UUID.randomUUID().toString(), GroupType.PRIVATE);

        List<Group> groups = dao.getGroupsBy(GroupType.PUBLIC, true);
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(uid, groups.get(0).getUuid());
    }
}