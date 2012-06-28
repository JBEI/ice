package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.permissions.PermissionsController;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author Hector Plahar
 */
public class EntryControllerTest {
    private EntryController controller;

    @Before
    public void setUp() {
//        HibernateHelper.initializeMock();
//        dao = new EntryDAO();
        controller = new EntryController();
        EntryDAO mockDAO = mock(EntryDAO.class);
        controller.setDAO(mockDAO);
        PermissionsController permissionsController = mock(PermissionsController.class);
        controller.setPermissionsController(permissionsController);
    }

    @Test
    public void testCreateEntry() throws Exception {
//        Entry entry = controller.createEntry(new Entry());
//        Assert.assertNotNull(entry);

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
    public void testHasReadPermission() throws Exception {

    }

    @Test
    public void testHasWritePermission() throws Exception {

    }

    @Test
    public void testHasAttachments() throws Exception {

    }

    @Test
    public void testGetAllVisibleEntryIDs() throws Exception {

    }

    @Test
    public void testGetAllEntryIDs() throws Exception {

    }

    @Test
    public void testGetNumberOfVisibleEntries() throws Exception {

    }

    @Test
    public void testGetEntryIdsByOwner() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testGetAllEntryCount() throws Exception {

    }

    @Test
    public void testGetOwnerEntryCount() throws Exception {

    }

    @Test
    public void testGetAllEntries() throws Exception {

    }

    @Test
    public void testRetrieveEntriesByIdSetSort() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSet() throws Exception {

    }

    @Test
    public void testSortList() throws Exception {

    }

    @Test
    public void testRetrieveEntryByType() throws Exception {

    }

    @Test
    public void testRetrieveStrainsForPlasmid() throws Exception {

    }
}
