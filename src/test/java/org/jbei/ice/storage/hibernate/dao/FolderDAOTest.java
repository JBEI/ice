package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class FolderDAOTest extends HibernateRepositoryTest {

    private FolderDAO dao = new FolderDAO();

    @Test
    public void testGet() {
        Folder folder = createFolderObject("testGet");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);
        Folder info = createFolderObject("testGet");
        folder = dao.get(folder.getId());
        Assert.assertEquals(folder.getDescription(), info.getDescription());
        Assert.assertEquals(folder.getName(), info.getName());
    }

    @Test
    public void testGetRemote() {
        Folder folder = createFolderObject("testGetRemote");
        folder.setType(FolderType.REMOTE);
        folder.setDescription("remoteFolderId");
        folder = dao.create(folder);
        Assert.assertNotNull(folder);

        Folder result = dao.getRemote("remoteFolderId", "testGetRemote");
        Assert.assertNotNull(result);

        Assert.assertEquals(result.getId(), folder.getId());
        Assert.assertEquals(result.getDescription(), folder.getDescription());
        Assert.assertEquals(result.getName(), folder.getName());
        Assert.assertEquals(result.getOwnerEmail(), folder.getOwnerEmail());
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

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            long id = TestEntryCreator.createTestPart(email);
            Entry entry = new EntryDAO().get(id);
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

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            long id = TestEntryCreator.createTestPart(email);
            Entry entry = new EntryDAO().get(id);
            entries.add(entry);
        }

        // add entries to folder
        folder = dao.addFolderContents(folder, entries);
        Assert.assertEquals(10, dao.getFolderSize(folder.getId(), null, true).intValue());
    }

    @Test
    public void testGetFolderContentIds() {

    }

    @Test
    public void testDelete() {
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

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            long id = TestEntryCreator.createTestPart(email);
            Entry entry = new EntryDAO().get(id);
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

        // create 10 entries
        for (int i = 0; i < 10; i += 1) {
            long id = TestEntryCreator.createTestPart(email);
            Entry entry = new EntryDAO().get(id);
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
        List<Entry> entries = new ArrayList<>();

        for (int i = 1; i <= 9; i += 1) {
            PartData part = new PartData(EntryType.PART);
            part.setName("name" + i);
            part.setOwnerEmail(email);
            part.setAlias("alias" + i);
            part.setShortDescription("short description");
            part = new Entries(account.getEmail()).create(part);
            Entry entry = DAOFactory.getEntryDAO().get(part.getId());
            entries.add(entry);
        }

        // add to folder
        folder = dao.addFolderContents(folder, entries);
        Assert.assertNotNull(folder);

        List<Long> result = dao.retrieveFolderContents(folder.getId(), new PageParameters(0, 15, ColumnField.NAME, true, null), false);
        Assert.assertNotNull(result);
        for (int i = 1; i <= 9; i += 1) {
            long entryId = result.get(i - 1);
            Entry entry = DAOFactory.getEntryDAO().get(entryId);
            Assert.assertNotNull(entry);
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
