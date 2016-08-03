package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PermissionDAOTest {
    Account adminAccount, regularAccount;
    PermissionDAO dao;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();

        adminAccount = new AccountController().createAdminAccount();
        regularAccount = AccountCreator.createTestAccount("Joe", false);
        dao = new PermissionDAO();
    }

    public List<Long> makePrivateEntryIds(Account ownerAccount) throws Exception {
        List<Long> entryIds = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            Strain strain = TestEntryCreator.createTestStrain(ownerAccount);
            entryIds.add(strain.getId());
        }
        return entryIds;
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testNonAdminCantReadWithoutPermissions() throws Exception {
        List<Long> entryIds = makePrivateEntryIds(adminAccount);
        Assert.assertArrayEquals(new Object[0], dao.getCanReadEntries(regularAccount, regularAccount.getGroups(), entryIds).toArray());
    }
}
