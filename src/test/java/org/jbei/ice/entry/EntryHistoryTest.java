package org.jbei.ice.entry;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.TestEntryCreator;
import org.jbei.ice.dto.History;
import org.jbei.ice.dto.common.Results;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Entry;
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
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }

    @Test
    public void testGet() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("EntryHistoryTest.testGet", false);
        Entry plasmid = TestEntryCreator.createTestPlasmid(account);
        AccountModel reader = AccountCreator.createTestAccount("EntryHistoryTest.testGetReader", false);
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
        AccountModel account = AccountCreator.createTestAccount("EntryHistoryTest.testAdd", false);
        Entry plasmid = TestEntryCreator.createTestPlasmid(account);
        AccountModel reader = AccountCreator.createTestAccount("EntryHistoryTest.testAddReader", false);
        EntryHistory entryHistory = new EntryHistory(reader.getEmail(), plasmid.getId());
        Assert.assertTrue(entryHistory.add());
    }

    @Test
    public void testDelete() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("EntryHistoryTest.testDelete", false);
        Entry plasmid = TestEntryCreator.createTestPlasmid(account);
        AccountModel reader = AccountCreator.createTestAccount("EntryHistoryTest.testDeleteReader", false);
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
        AccountModel account = AccountCreator.createTestAccount("EntryHistoryTest.testDeleteAll", false);
        Entry plasmid = TestEntryCreator.createTestPlasmid(account);
        AccountModel reader = AccountCreator.createTestAccount("EntryHistoryTest.testDeleteAllReader", false);
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
