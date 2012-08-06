package org.jbei.ice.lib.entry;

import java.util.ArrayList;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.server.dao.hibernate.HibernateHelper;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class EntryControllerTest {
    private EntryController controller;

    @Before
    public void setUp() {
        HibernateHelper.initializeMock();
        controller = new EntryController();
    }

    @Test
    public void testCreateEntry() throws Exception {
        String email = "testCreateEntry@TESTER.org";

        AccountController accountController = new AccountController();
        Account account = accountController.createAdminAccount(email, "popop");
        Assert.assertNotNull(account);

        Entry entry = new Strain();
        entry = controller.createEntry(account, entry, new GroupController().createOrRetrievePublicGroup());
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.getId() > 0);

        // account should only have a single entry
        ArrayList<Long> list = controller.getEntryIdsByOwner(email);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(entry.getId(), list.get(0).intValue());

        // check permission
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
