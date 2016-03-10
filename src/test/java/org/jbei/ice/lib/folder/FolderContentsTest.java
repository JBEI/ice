package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.EntrySelectionType;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @author Hector Plahar
 */
public class FolderContentsTest {

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testAddEntrySelection() throws Exception {
        // create account
        Account account = AccountCreator.createTestAccount("FolderContentsTest.testAddEntrySelection", true);
        String userId = account.getEmail();

        Account user = AccountCreator.createTestAccount("FolderContentsTest.testAddEntrySelection2", false);

        // create folder
        FolderDetails folderDetails = new FolderDetails();
        folderDetails.setName("testAdd");
        folderDetails.setOwner(user.toDataTransferObject());

        FolderController controller = new FolderController();
        folderDetails = controller.createPersonalFolder(userId, folderDetails);
        Assert.assertNotNull(folderDetails);

        // check folder ownership
        Assert.assertEquals(1, controller.getUserFolders(user.getEmail()).size());

        // entry selection context for adding to folder
        EntrySelection selection = new EntrySelection();
        selection.setSelectionType(EntrySelectionType.FOLDER);
        selection.getDestination().add(folderDetails);

        // create entries
        long id = TestEntryCreator.createTestPart(userId);
        selection.getEntries().add(id);

        id = TestEntryCreator.createTestPart(userId);
        selection.getEntries().add(id);

        // add to folder
        FolderContents folderContents = new FolderContents();
        List<FolderDetails> folders = folderContents.addEntrySelection(userId, selection);
        Assert.assertNotNull(folders);
    }

    @Test
    public void testAddEntriesToFolders() throws Exception {

    }

    @Test
    public void testGetContents() throws Exception {
        FolderContents folderContents = new FolderContents();


        // test with null id
        folderContents.getContents(null, 0, new PageParameters(0, 10, ColumnField.PART_ID, false, null));

        Account account = AccountCreator.createTestAccount("testRetrieveFolderContents", false);
        String userId = account.getEmail();

        FolderDetails folderDetails = new FolderDetails();
        folderDetails.setName("test");

        // create folder
        FolderDetails folder = createPersonalFolder(userId, folderDetails);
        Assert.assertNotNull(folder);
        final short size = 105;

        // create 100 test strains
        HashMap<String, Entry> parts = new HashMap<>();
        List<Long> entryList = new ArrayList<>();
        for (int i = 0; i < size; i += 1) {
            Strain strain = TestEntryCreator.createTestStrain(account);
            Assert.assertNotNull(strain);
            parts.put(strain.getPartNumber(), strain);
            entryList.add(strain.getId());
        }
        Assert.assertEquals(size, parts.size());

        // add to folder
        List<FolderDetails> foldersToAdd = new ArrayList<>();
        foldersToAdd.add(folder);
        foldersToAdd = folderContents.addEntriesToFolders(account.getEmail(), entryList, foldersToAdd);
        Assert.assertNotNull(foldersToAdd);

        // keep track to find duplicates
        HashSet<Long> set = new HashSet<>();

        // retrieve (supported sort types created, status, name, part_id, type)
        FolderDetails details = folderContents.getContents(account.getEmail(), folder.getId(),
                new PageParameters(0, 15, ColumnField.PART_ID, false, null));
        Assert.assertNotNull(details);

        short pageSize = 15;

        int it = 1;
        while (!details.getEntries().isEmpty()) {
            Assert.assertEquals(pageSize, details.getEntries().size());
            for (PartData partData : details.getEntries()) {
                Assert.assertNotNull(parts.remove(partData.getPartId()));
                Assert.assertFalse(set.contains(partData.getId()));
                set.add(partData.getId());
            }
            // check remaining
            Assert.assertEquals((size - (it * pageSize)), parts.size());
            details = folderContents.getContents(account.getEmail(), folder.getId(),
                    new PageParameters(pageSize * it, pageSize, ColumnField.PART_ID, false, null));
            it += 1;
        }
    }

    @Test
    public void testGetAndFilterFolderPermissions() throws Exception {

    }

    public FolderDetails createPersonalFolder(String userId, FolderDetails folderDetails) {
        if (folderDetails.getName() == null)
            return null;
        Folder folder = new Folder(folderDetails.getName());
        folder.setOwnerEmail(userId);
        folder.setType(FolderType.PRIVATE);
        folder.setCreationTime(new Date());
        folder = DAOFactory.getFolderDAO().create(folder);
        FolderDetails details = folder.toDataTransferObject();
        details.setCanEdit(true);
        return details;
    }
}