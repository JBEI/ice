package org.jbei.ice.lib.account;

import junit.framework.TestCase;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.server.dao.hibernate.HibernateHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountDAOTest extends TestCase {

    private AccountDAO dao;

    @Before
    public void setUp() {
        HibernateHelper.initializeMock();
        dao = new AccountDAO();
    }

    @Test
    public void testGet() throws DAOException {
        Assert.assertNull(dao.get(0));
        Account account = new Account();
        account.setEmail("test_email");
        account.setFirstName("First");
        account.setLastName("Last");
        account.setInitials("FL");
        account.setInstitution("");
        account.setDescription("");
        account.setIp("127.0.0.1");
        account.setPassword("40ntH@cKm3br0");
        Account saved = dao.save(account);
        Assert.assertNotNull(saved);
        Account ret = dao.get(saved.getId());
        Assert.assertTrue(saved.getEmail().equals(ret.getEmail()));
    }

    @After
    public void tearDown() {
    }
}
