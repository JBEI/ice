package org.jbei.ice.lib.entry;

import org.jbei.ice.server.dao.hibernate.HibernateHelper;

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
        HibernateHelper.initializeMock();
        dao = new EntryDAO();
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
    }

    @Test
    public void testSave() throws Exception {
    }
}
