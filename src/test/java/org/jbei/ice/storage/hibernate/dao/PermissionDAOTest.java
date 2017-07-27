package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.junit.Test;

public class PermissionDAOTest extends HibernateRepositoryTest {

    private PermissionDAO dao = new PermissionDAO();

//    public List<Long> makePrivateEntryIds() throws Exception {
//        List<Long> entryIds = new ArrayList<>(3);
//        for (int i = 0; i < 3; i++) {
//            Strain strain = TestEntryCreator.createTestStrain(ownerAccount);
//            entryIds.add(strain.getId());
//        }
//        return entryIds;
//    }

    @Test
    public void testNonAdminCantReadWithoutPermissions() throws Exception {
//        Account regularAccount = AccountCreator.createTestAccount("Joe", false);
//        List<Long> entryIds = makePrivateEntryIds(adminAccount);
//        Assert.assertArrayEquals(new Object[0],
//                dao.getCanReadEntries(regularAccount, new ArrayList<>(regularAccount.getGroups()), entryIds).toArray());
    }

    @Test
    public void testHasPermission() throws Exception {

    }

    @Test
    public void testHasPermissionMulti() throws Exception {

    }

    @Test
    public void testRetrievePermission() throws Exception {

    }

    @Test
    public void testCreatePermissionQuery() throws Exception {

    }

    @Test
    public void testRemovePermission() throws Exception {

    }

    @Test
    public void testGetEntryPermissions() throws Exception {

    }

    @Test
    public void testGetFolderPermissions() throws Exception {

    }

    @Test
    public void testRetrieveAccountPermissions() throws Exception {

    }

    @Test
    public void testHasSetWriteFolderPermission() throws Exception {

    }

    @Test
    public void testRetrieveGroupPermissions() throws Exception {

    }

    @Test
    public void testClearPermissions() throws Exception {

    }

    @Test
    public void testClearPermissions1() throws Exception {

    }

    @Test
    public void testRetrieveFolderPermissions() throws Exception {

    }

    @Test
    public void testGetFolders() throws Exception {

    }

    @Test
    public void testGetCanReadEntries() throws Exception {

    }

    @Test
    public void testGet() throws Exception {

    }
}
