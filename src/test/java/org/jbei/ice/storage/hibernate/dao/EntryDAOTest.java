package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.SelectionMarker;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class EntryDAOTest {

    private EntryDAO entryDAO;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        entryDAO = new EntryDAO();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testGetEntrySummary() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetEntrySummary", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        String summary = entryDAO.getEntrySummary(id);
        Assert.assertEquals("summary for test", summary);
    }

    @Test
    public void testGetMatchingSelectionMarkers() throws Exception {
        String email = "testGetMatchingSelectionMarkers";
        Account account = AccountCreator.createTestAccount(email, false);
        Assert.assertNotNull(account);
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        strain.setShortDescription("test strain");

        SelectionMarker marker = new SelectionMarker();
        marker.setName("xkcd");
        SelectionMarker marker2 = new SelectionMarker();
        marker2.setName("test");

        Set<SelectionMarker> markerSet = new HashSet<>();
        markerSet.add(marker);
        markerSet.add(marker2);
        strain.setSelectionMarkers(markerSet);
        EntryCreator creator = new EntryCreator();
        strain = (Strain) creator.createEntry(account, strain, null);
        Assert.assertNotNull(strain);

        Assert.assertEquals(2, strain.getSelectionMarkers().size());

        List<String> results = entryDAO.getMatchingSelectionMarkers("xkcd", 5);
        Assert.assertEquals(1, results.size());

        List<String> res = entryDAO.getMatchingSelectionMarkers("tes", 5);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("test", res.get(0));
    }

    @Test
    public void testMatchingPlasmidField() throws Exception {
        // create plasmids
    }

    @Test
    public void testGet() throws Exception {
        Account account = AccountCreator.createTestAccount("testGet", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = entryDAO.get(id);
        Assert.assertNotNull(entry);
        Assert.assertEquals(id, entry.getId());
    }

    @Test
    public void testGetByRecordId() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetByRecordId", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = entryDAO.get(id);
        Assert.assertNotNull(entry);
        Entry rEntry = entryDAO.getByRecordId(entry.getRecordId());
        Assert.assertNotNull(rEntry);
        Assert.assertEquals(entry.getRecordId(), rEntry.getRecordId());
    }

    @Test
    public void testGetByPartNumber() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetByPartNumber", false);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = entryDAO.get(id);
        Assert.assertNotNull(entry);
        Entry result = entryDAO.getByPartNumber(entry.getPartNumber());
        Assert.assertNotNull(result);
        Assert.assertEquals(entry.getPartNumber(), result.getPartNumber());
    }

    @Test
    public void testGetByUniqueName() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetByUniqueName", false);
        PartData data = new PartData(EntryType.PART);
        data.setShortDescription("summary for test");
        String uniqueName = "pTest" + account.getEmail();
        data.setName(uniqueName);
        data.setBioSafetyLevel(1);
        EntryCreator creator = new EntryCreator();
        creator.createPart(account.getEmail(), data);
        List<Entry> entries = entryDAO.getByName("pTest");
        Assert.assertTrue(entries == null || entries.isEmpty());
        entries = entryDAO.getByName(uniqueName);
        Assert.assertNotNull(entries);
    }
}