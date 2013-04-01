package org.jbei.ice.lib.entry;

import java.util.List;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.entry.EntryType;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for EntryDAO
 *
 * @author Hector Plahar
 */
public class EntryDAOTest {
    private EntryDAO dao;

    @Before
    public void setUp() {
//        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        dao = new EntryDAO();
    }

    @After
    public void tearDown() {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testSave() throws Exception {
        Entry entry = new Entry();
        entry.setRecordId(Utils.generateUUID());
        entry.setRecordType(EntryType.PLASMID.getName());
        entry.setVersionId(entry.getRecordId());
        entry = dao.save(entry);
        System.out.println(entry.getId());
        Entry retrieved = dao.get(entry.getId());
        Assert.assertNotNull(retrieved);

        Entry entry2 = new Entry();
        entry2.setRecordId(Utils.generateUUID());
        entry2.setRecordType(EntryType.STRAIN.getName());
        entry2.setVersionId(entry.getRecordId());
        entry2 = dao.save(entry2);
        System.out.println(entry2.getId());
    }

    @Test
    public void testRetrieveStrainsForPlasmid() throws Exception {
    }

    @Test
    public void testGet() throws Exception {
    }

    @Test
    public void testGetByRecordId() throws Exception {
    }

    @Test
    public void testGetByPartNumber() throws Exception {
    }

    @Test
    public void testGetByName() throws Exception {
    }

    @Test
    public void testGetOwnerEntryCount() throws Exception {
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
        List<Entry> entries = dao.retrieveOwnerEntries("haplahar@lbl.gov", null, true, 0, 15);
        for (Entry entry : entries) {
            System.out.println(entry.getId());
        }
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
    }


}
