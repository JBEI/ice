package org.jbei.ice.lib.group;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.shared.dto.group.GroupType;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class GroupControllerTest {

    private GroupController controller;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
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
        Assert.assertTrue(group.getType() == GroupType.PUBLIC);
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
        Group group = controller.createOrRetrievePublicGroup();
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getMembers().size() == 0);
        AccountController accountController = new AccountController();
        accountController.createNewAccount("Test", "Tester", "TT", "test@tester", "LBL", "test account");
        controller.addMemberToGroup(group.getId(), "test@tester");
        Assert.assertTrue(group.getMembers().size() == 1);
    }

    @Test
    public void testSetGroupMembers() throws Exception {
    }
}
