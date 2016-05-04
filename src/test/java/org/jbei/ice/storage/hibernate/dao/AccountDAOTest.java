package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.junit.*;

import java.util.List;
import java.util.Set;

public class AccountDAOTest {

    private AccountDAO dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() {
        HibernateUtil.beginTransaction();
        dao = new AccountDAO();
    }

    @Test
    public void testGet() throws DAOException {
        Assert.assertNull(dao.get(0));
        Account account = createAccountObject("testGet");
        Account saved = dao.create(account);
        Assert.assertNotNull(saved);
        Account ret = dao.get(saved.getId());
        Assert.assertTrue(saved.getEmail().equals(ret.getEmail()));
    }

    @Test
    public void testGetMatchingAccounts() throws DAOException {
        for (int i = 15; i < 24; i += 1) {
            Account account = createAccountObject("testGetMatchingAccounts" + i);
            Assert.assertNotNull(dao.create(account));
        }

        // get by first name (return 4)
        int limit = 4;
        Set<Account> accounts = dao.getMatchingAccounts("Fir", limit);
        Assert.assertEquals(limit, accounts.size());
        for (Account account : accounts) {
            Assert.assertTrue(account.getFirstName().contains("Fir"));
        }

        // get by last name (return 7)
        limit = 7;
        accounts = dao.getMatchingAccounts("ast", limit);
        Assert.assertEquals(limit, accounts.size());
        for (Account account : accounts) {
            Assert.assertTrue(account.getLastName().contains("ast"));
        }

        // get by email
        limit = 10;
        accounts = dao.getMatchingAccounts("testGetMatchingAccounts1", limit);
        Assert.assertEquals(5, accounts.size());
    }

    @Test
    public void testGetByEmail() throws DAOException {
        Account account = createAccountObject("testGetByEmail");
        account = dao.create(account);
        Assert.assertNotNull(account);
        account = dao.getByEmail(account.getEmail());
        Assert.assertNotNull(account);
        Assert.assertEquals("testGetByEmail", account.getEmail());
    }

    @Test
    public void testGetAccounts() throws DAOException {
        for (int i = 0; i < 16; i += 1) {
            Account account = createAccountObject("testGetAccounts" + i);
            Assert.assertNotNull(dao.create(account));
        }

        // get all accounts (no filters), 4 at a time
        for (int i = 0; i < 16; i += 4) {
            List<Account> accountList = dao.getAccounts(i, 4, "id", true, null);
            Assert.assertEquals(4, accountList.size());
        }

        // get all accounts, filter by email
        for (int i = 0; i < 16; i += 4) {
            List<Account> accountList = dao.getAccounts(i, 4, "id", true, "testGetAccounts");
            Assert.assertEquals(4, accountList.size());
        }
    }

    @Test
    public void testGetAccountsCount() throws DAOException {
        for (int i = 15; i < 24; i += 1) {
            Account account = createAccountObject("testGetAccountsCount" + i);
            Assert.assertNotNull(dao.create(account));
        }

        Assert.assertEquals(5, dao.getAccountsCount("testGetAccountsCount1"));
    }

    private Account createAccountObject(String email) {
        Account account = new Account();
        account.setEmail(email);
        account.setFirstName("First");
        account.setLastName("Last");
        account.setInitials("FL");
        account.setInstitution("");
        account.setDescription("");
        account.setIp("127.0.0.1");
        account.setPassword("40ntH@cKm3br0");
        return account;
    }

    @After
    public void tearDown() {
        HibernateUtil.commitTransaction();
    }
}
