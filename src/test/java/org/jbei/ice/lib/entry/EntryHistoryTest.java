package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Plasmid;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class EntryHistoryTest {

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testGet() throws Exception {
        Account account = AccountCreator.createTestAccount("EntryHistoryTest.testGet", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Account reader = AccountCreator.createTestAccount("EntryHistoryTest.testGetReader", false);
        EntryHistory entryHistory = new EntryHistory(reader.getEmail(), plasmid.getId());
        entryHistory.add();

        entryHistory = new EntryHistory(account.getEmail(), plasmid.getId());
        Results<History> history = entryHistory.get(20, 0, true, null);
        Assert.assertNotNull(history);
        Assert.assertEquals(1, history.getResultCount());
        History object = history.getData().get(0);
        Assert.assertEquals(reader.getEmail(), object.getAccount().getEmail());
    }

    @Test
    public void testAdd() throws Exception {
        Account account = AccountCreator.createTestAccount("EntryHistoryTest.testAdd", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Account reader = AccountCreator.createTestAccount("EntryHistoryTest.testAddReader", false);
        EntryHistory entryHistory = new EntryHistory(reader.getEmail(), plasmid.getId());
        Assert.assertTrue(entryHistory.add());
    }

    @Test
    public void testDelete() throws Exception {
        Account account = AccountCreator.createTestAccount("EntryHistoryTest.testDelete", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Account reader = AccountCreator.createTestAccount("EntryHistoryTest.testDeleteReader", false);
        EntryHistory entryHistory = new EntryHistory(reader.getEmail(), plasmid.getId());
        entryHistory.add();

        entryHistory = new EntryHistory(account.getEmail(), plasmid.getId());
        Results<History> history = entryHistory.get(20, 0, true, null);
        Assert.assertNotNull(history);

        Assert.assertTrue(entryHistory.delete(history.getData().get(0).getId()));
        history = entryHistory.get(20, 0, true, null);
        Assert.assertEquals(0, history.getResultCount());
    }

    @Test
    public void testDeleteAll() throws Exception {
        Account account = AccountCreator.createTestAccount("EntryHistoryTest.testDeleteAll", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Account reader = AccountCreator.createTestAccount("EntryHistoryTest.testDeleteAllReader", false);
        EntryHistory entryHistory = new EntryHistory(reader.getEmail(), plasmid.getId());
        entryHistory.add();

        entryHistory = new EntryHistory(account.getEmail(), plasmid.getId());
        Results<History> history = entryHistory.get(20, 0, true, null);
        Assert.assertNotNull(history);

        Assert.assertEquals(1, entryHistory.deleteAll());
        history = entryHistory.get(20, 0, true, null);
        Assert.assertEquals(0, history.getResultCount());
    }
}