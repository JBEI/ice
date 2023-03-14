package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.TestEntryCreator;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.entry.Entries;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Entry;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Hector Plahar
 */
public class EntryDAOTest extends HibernateRepositoryTest {

    private EntryDAO entryDAO = new EntryDAO();

    @Test
    public void testGetEntrySummary() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetEntrySummary", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
    }


    @Test
    public void testMatchingPlasmidField() throws Exception {
        // create plasmids
    }

    @Test
    public void testGet() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGet", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = entryDAO.get(id);
        Assert.assertNotNull(entry);
        Assert.assertEquals(id, entry.getId());
    }

    @Test
    public void testGetByRecordId() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetByRecordId", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = entryDAO.get(id);
        Assert.assertNotNull(entry);
        Entry rEntry = entryDAO.getByRecordId(entry.getRecordId());
        Assert.assertNotNull(rEntry);
        Assert.assertEquals(entry.getRecordId(), rEntry.getRecordId());
    }

    @Test
    public void testGetByPartNumber() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetByPartNumber", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = entryDAO.get(id);
        Assert.assertNotNull(entry);
        Entry result = entryDAO.getByPartNumber(entry.getPartNumber());
        Assert.assertNotNull(result);
        Assert.assertEquals(entry.getPartNumber(), result.getPartNumber());
    }

    @Test
    public void testGetByUniqueName() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetByUniqueName", false);
        PartData data = new PartData(EntryType.PART);
        String uniqueName = "pTest" + account.getEmail();
        data.setName(uniqueName);
        Entries creator = new Entries(account.getEmail());
        creator.create(data);
        List<Entry> entries = entryDAO.getByName("pTest");
        Assert.assertTrue(entries == null || entries.isEmpty());
        entries = entryDAO.getByName(uniqueName);
        Assert.assertNotNull(entries);
    }

    @Test
    public void testGetOwnerEntryIds() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("EntryDAOTest.testGetOwnerEntryIds", false);
        Random random = new Random();
        int entryCount = random.nextInt(30);
        while (entryCount == 0)
            entryCount = random.nextInt(30);

        List<Long> created = new ArrayList<>(entryCount);
        for (int i = 0; i < entryCount; i += 1) {
            long id = TestEntryCreator.createTestPart(account.getEmail());
            Entry entry = entryDAO.get(id);
            created.add(id);
            Assert.assertNotNull(entry);
            Assert.assertEquals(entry.getOwnerEmail(), account.getEmail());
        }

        List<Long> ids = entryDAO.getOwnerEntryIds(account.getEmail(), null);
        Assert.assertEquals(ids.size(), entryCount);
        Assert.assertTrue(ids.removeAll(created));
        Assert.assertTrue(ids.isEmpty());
    }
}
