package org.jbei.ice.lib.entry;

import junit.framework.Assert;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.server.dao.hibernate.HibernateHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for EntryDAO
 *
 * @author Hector Plahar
 */
public class EntryDAOTest {
    private EntryDAO dao;

    @Before
    public void setUp() {
        HibernateHelper.initializeMock();
        dao = new EntryDAO();
    }

    @Test
    public void testRetrieveStrainsForPlasmid() throws Exception {

    }

    @Test
    public void testGet() throws Exception {
        Entry newEntry = new Strain();
        Account account = Mockito.mock(Account.class);
        newEntry = EntryFactory.createEntry(account, "JBEI-XOA", newEntry);
        newEntry = dao.save(newEntry);
        long savedID = newEntry.getId();

        // check that the object exists
        Assert.assertNotNull(dao.get(savedID));
    }

    @Test
    public void testGetByRecordId() throws Exception {

    }

    @Test
    public void testGetByPartNumber() throws Exception {

    }

    @Test
    public void testGetByName() throws Exception {
        Entry newEntry = new Strain();
        Account account = Mockito.mock(Account.class);
        newEntry = EntryFactory.createEntry(account, "JBEI-XOA", newEntry);

        // create random number of fake names
        Set<Name> fakeNames = new HashSet<Name>();
        int nameSize = new Random().nextInt(50);
        for (int i = 0; i < nameSize; i += 1) {
            fakeNames.add(new Name("TEST-" + i, newEntry));
        }
        newEntry.setNames(fakeNames);
        newEntry = dao.save(newEntry);

        // check that the object exists
        Set<Name> names = newEntry.getNames();
        Assert.assertNotNull(names);
        Assert.assertEquals(fakeNames.size(), names.size());

        for (Name name : names) {
            Entry entry = dao.getByName(name.getName());
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry.equals(newEntry));
        }
    }

    @Test
    public void testGetOwnerEntryCount() throws Exception {
        Account account = mock(Account.class);
        when(account.getEmail()).thenReturn("test");
        int count = dao.getOwnerEntryCount(account.getEmail(), new Integer(0));

        // no entries so
        Assert.assertEquals(0, count);
    }

    @Test
    public void testGetNumberOfVisibleEntries() throws Exception {

    }

    @Test
    public void testGetAllVisibleEntries() throws Exception {

    }

    @Test
    public void testGetAllEntries() throws Exception {

    }

    @Test
    public void testGetAllEntryCount() throws Exception {

    }

    @Test
    public void testGetEntries() throws Exception {

    }

    @Test
    public void testGetEntriesByOwner() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSetSort() throws Exception {

    }

    @Test
    public void testGetEntriesSortByName() throws Exception {

    }

    @Test
    public void testGetEntriesSortByPartNumber() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSet() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSetSortByType() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSetSortByName() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSetSortByCreated() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSetSortByPartNumber() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSetSortByStatus() throws Exception {

    }

    @Test
    public void testSortList() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {
        Entry newEntry = new Strain();
        Account account = Mockito.mock(Account.class);
        newEntry = EntryFactory.createEntry(account, "JBEI-XOA", newEntry);
        newEntry = dao.save(newEntry);
        long savedID = newEntry.getId();

        // check that the object exists
        newEntry = dao.get(savedID);
        Assert.assertNotNull(newEntry);

        // delete it
        dao.delete(newEntry);

        // check does not exist anymore
        Assert.assertNull(dao.get(savedID));
    }

    @Test
    public void testSave() throws Exception {
        Entry newEntry = new Strain();
        Account account = Mockito.mock(Account.class);
        newEntry = EntryFactory.createEntry(account, "JBEI-XOA", newEntry);
        Entry savedEntry = dao.save(newEntry);
        Assert.assertNotNull(savedEntry);
        Assert.assertFalse(savedEntry.getId() <= 0);
        Assert.assertNotNull(savedEntry.getRecordId());
        Assert.assertFalse(savedEntry.getRecordId().isEmpty());
    }
}
