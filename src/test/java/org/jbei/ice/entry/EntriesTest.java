package org.jbei.ice.entry;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.TestEntryCreator;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.folder.FolderDetails;
import org.jbei.ice.folder.FolderContents;
import org.jbei.ice.folder.FolderController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Entry;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EntriesTest extends HibernateRepositoryTest {

    @Test
    public void testGetEntriesFromSelectionContext() throws Exception {

        // create accounts
        AccountModel person1 = AccountCreator.createTestAccount("testGetEntriesFromSelectionContext1", false);
        Assert.assertNotNull(person1);
        AccountModel person2 = AccountCreator.createTestAccount("testGetEntriesFromSelectionContext2", false);
        Assert.assertNotNull(person2);
        AccountModel admin = AccountCreator.createTestAccount("testGetEntriesFromSelectionContext3", true);
        Assert.assertNotNull(admin);

        // create folder
        FolderController folderController = new FolderController();
        FolderDetails details = new FolderDetails();
        details.setName("testFolder");
        details.setDescription("folder for unit test");
        details = folderController.createPersonalFolder(admin.getEmail(), details);
        System.out.println(details.getId());
        Assert.assertNotNull(details);

        //create entries
        Entry p1Plasmid = TestEntryCreator.createTestPlasmid(person1);
        Entry p2Plasmid = TestEntryCreator.createTestPlasmid(person2);
        Entry adminPlasmid = TestEntryCreator.createTestPlasmid(admin);

        // add entries to folder
        EntrySelection selection = new EntrySelection();
        selection.getEntries().add(p1Plasmid.getId());
        selection.getEntries().add(p2Plasmid.getId());
        List<FolderDetails> destination = new ArrayList<>(1);
        destination.add(details);

        selection.setDestination(destination);
        selection.setSelectionType(EntrySelectionType.FOLDER);
        FolderContents folderContents = new FolderContents();
        List<FolderDetails> added = folderContents.addEntrySelection(admin.getEmail(), selection);
        Assert.assertEquals(1, added.size());

        // test get entries from the selected context (folder)
        EntrySelection context = new EntrySelection();
        context.setFolderId(Long.toString(details.getId()));
        context.setAll(true);
        context.setSelectionType(EntrySelectionType.FOLDER);

        List<Long> results = new Entries(admin.getEmail()).getEntriesFromSelectionContext(context);
        Assert.assertEquals(2, results.size());

        // test get entries from selected context (folder) by non-admin
        // todo

        // share folder first since it is a private folder
//        FolderPermissions folderPermissions = new FolderPermissions(admin.getEmail(), details.getId());
//        AccessPermission permission = new AccessPermission();
//        permission.setArticle(AccessPermission.Article.ACCOUNT);
//        permission.setType(AccessPermission.Type.READ_FOLDER);
//        permission.setTypeId(person1.getId());
//        permission.setArticleId(details.getId());
//        Assert.assertNotNull(folderPermissions.createPermission(permission));
//
//        results = new Entries(person1.getEmail()).getEntriesFromSelectionContext(context);
//        Assert.assertEquals(2, results.size());

        // todo : get context from collection
//        context = new EntrySelection();
//        context.setSelectionType(EntrySelectionType.COLLECTION);
//        context.setFolderId("available");
//        results = new Entries(admin.getEmail()).getEntriesFromSelectionContext(context);
//        Assert.assertEquals(3, results.size()); // 3 because admins can see all entries
//
//        results = new Entries(person1.getEmail()).getEntriesFromSelectionContext(context);
//        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testCreatePart() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testCreatePart", false);
        Assert.assertNotNull(account);
        String userId = account.getEmail();

        // create strain
        PartData strain = new PartData(EntryType.STRAIN);
        strain.setOwner("Tester");
        strain.setOwnerEmail("tester");

        Entries entries = new Entries(userId);
        long id = entries.create(strain).getId();
        Entry entry = DAOFactory.getEntryDAO().get(id);
        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getOwnerEmail(), strain.getOwnerEmail());


        // create arabidopsis seed
        PartData seed = new PartData(EntryType.SEED);
        long seedId = entries.create(seed).getId();
    }
}
