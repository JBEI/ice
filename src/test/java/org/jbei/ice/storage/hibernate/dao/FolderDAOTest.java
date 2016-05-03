package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Part;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class FolderDAOTest {

    private FolderDAO dao;

    @Before
    public void setUp() {
        HibernateUtil.initializeMock();
        dao = new FolderDAO();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testGet() throws Exception {
        Folder folder = createFolderObject("testGet");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);
        Folder info = createFolderObject("testGet");
        folder = dao.get(folder.getId());
        Assert.assertEquals(folder.getDescription(), info.getDescription());
        Assert.assertEquals(folder.getName(), info.getName());
    }

    @Test
    public void testRemoveFolderEntries() throws Exception {
        Account account = AccountCreator.createTestAccount("testRemoveFolderEntries", false);
        String email = account.getEmail();

        // create test folder
        Folder folder = createFolderObject("testRemoveFolderEntries");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);

        List<Entry> entries = new ArrayList<>();
        EntryCreator creator = new EntryCreator();

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            Part part = new Part();
            part.setName("name" + i);
            part.setOwnerEmail(email);
            part.setAlias("alias" + i);
            part.setShortDescription("short description");
            Entry entry = creator.createEntry(account, part, null);
            entries.add(entry);
        }

        // add entries to folder
        folder = dao.addFolderContents(folder, entries);

        // remove entries from folder
        List<Long> entriesToRemove = new ArrayList<>();
        entriesToRemove.add(entries.get(2).getId());
        entriesToRemove.add(entries.get(8).getId());
        entriesToRemove.add(entries.get(1).getId());
        entriesToRemove.add(entries.get(3).getId());
        dao.removeFolderEntries(folder, entriesToRemove);

        folder = dao.get(folder.getId());
        Assert.assertNotNull(folder);
        Assert.assertEquals(6, folder.getContents().size());
    }

    @Test
    public void testGetFolderSize() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetFolderSize", false);
        String email = account.getEmail();

        // create test folder
        Folder folder = createFolderObject("testGetFolderSize");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);

        List<Entry> entries = new ArrayList<>();
        EntryCreator creator = new EntryCreator();

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            Part part = new Part();
            part.setName("name" + i);
            part.setOwnerEmail(email);
            part.setAlias("alias" + i);
            part.setShortDescription("short description");
            Entry entry = creator.createEntry(account, part, null);
            entries.add(entry);
        }

        // add entries to folder
        folder = dao.addFolderContents(folder, entries);
        Assert.assertEquals(10, dao.getFolderSize(folder.getId(), null, true).intValue());
    }

    @Test
    public void testGetFolderContentIds() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {
        Folder folder = createFolderObject("testDelete");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);
        Assert.assertNotNull(dao.get(folder.getId()));
        dao.delete(folder);
        Assert.assertNull(dao.get(folder.getId()));
    }

    @Test
    public void testGetFolderContents() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetFolderContents", false);
        String email = account.getEmail();

        // create test folder
        Folder folder = createFolderObject("testGetFolderContents");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);

        List<Entry> entries = new ArrayList<>();
        EntryCreator creator = new EntryCreator();

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            Part part = new Part();
            part.setName("name" + i);
            part.setOwnerEmail(email);
            part.setAlias("alias" + i);
            part.setShortDescription("short description");
            Entry entry = creator.createEntry(account, part, null);
            entries.add(entry);
        }

        // add entries to folder
        folder = dao.addFolderContents(folder, entries);
        Assert.assertNotNull(folder);
    }

    @Test
    public void testAddFolderContents() throws Exception {
        Account account = AccountCreator.createTestAccount("testAddFolderContents", false);
        String email = account.getEmail();

        // create test folder
        Folder folder = createFolderObject("testAddFolderContents");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);

        List<Entry> entries = new ArrayList<>();
        EntryCreator creator = new EntryCreator();

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            Part part = new Part();
            part.setName("name" + i);
            part.setOwnerEmail(email);
            part.setAlias("alias" + i);
            part.setShortDescription("short description");
            Entry entry = creator.createEntry(account, part, null);
            entries.add(entry);
        }

        // add entries to folder
        folder = dao.addFolderContents(folder, entries);
        Assert.assertNotNull(folder);
    }

    @Test
    public void testRetrieveFolderContents() throws Exception {
        Account account = AccountCreator.createTestAccount("FolderDAOTest.testRetrieveFolderContents", false);
        String email = account.getEmail();
        Folder folder = createFolderObject(email);
        folder = dao.create(folder);
        Assert.assertNotNull(folder);

        // add entries to folder
        EntryCreator creator = new EntryCreator();
        List<Entry> entries = new ArrayList<>();

        for (int i = 1; i <= 9; i += 1) {
            Part part = new Part();
            part.setName("name" + i);
            part.setOwnerEmail(email);
            part.setAlias("alias" + i);
            part.setShortDescription("short description");
            Entry entry = creator.createEntry(account, part, null);
            entries.add(entry);
        }

        // add to folder
        folder = dao.addFolderContents(folder, entries);
        Assert.assertNotNull(folder);

        List<Entry> result = dao.retrieveFolderContents(folder.getId(), new PageParameters(0, 15, ColumnField.NAME, true, null), false);
        Assert.assertNotNull(result);
        for (int i = 1; i <= 9; i += 1) {
            Entry entry = result.get(i - 1);
            Assert.assertEquals(entry.getName(), "name" + i);
        }
    }

    private Folder createFolderObject(String ownerEmail) {
        Folder folder = new Folder();
        folder.setDescription("test");
        folder.setName("testFolderName");
        folder.setOwnerEmail(ownerEmail);
        folder.setPropagatePermissions(false);
        return folder;
    }
}
