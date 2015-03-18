package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oge on 3/20/15.
 */
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

    public List<Long> makePrivateEntryIds() throws Exception{
        List<Long> entryIds = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            Strain strain = TestEntryCreator.createTestStrain(regularAccount);
            entryIds.add(strain.getId());
        }
        return entryIds;
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.rollbackTransaction();
    }

    @Test
    public void testAdminCanReadEverything() throws Exception {
        List<Long> entryIds = makePrivateEntryIds();
        Assert.assertArrayEquals(entryIds.toArray(), dao.getCanReadEntries(adminAccount, entryIds).toArray());
    }

    @Test
    public void testNonAdminCantReadWithoutPermissions() throws Exception {
        List<Long> entryIds = makePrivateEntryIds();
        Assert.assertArrayEquals(new Object[0], dao.getCanReadEntries(regularAccount, entryIds).toArray());
    }
}
