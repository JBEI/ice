package org.jbei.ice.lib.folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderControllerTest {

    private FolderController controller;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new FolderController();
    }

    @Test
    public void testGetPublicFolders() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetPublicFolders", false);
        Assert.assertNotNull(account);
        String userId = account.getEmail();

        FolderDetails details = new FolderDetails();
        details.setName("test1");
        details = controller.createPersonalFolder(userId, details);
        Assert.assertNotNull(details);

        PermissionsController permissionsController = new PermissionsController();
        AccessPermission accessPermission = new AccessPermission();
        accessPermission.setArticle(AccessPermission.Article.GROUP);
        long publicGroupId = new GroupController().createOrRetrievePublicGroup().getId();
        accessPermission.setArticleId(publicGroupId);
        accessPermission.setType(AccessPermission.Type.READ_FOLDER);
        accessPermission.setTypeId(details.getId());
        permissionsController.addPermission(userId, accessPermission);

        ArrayList<FolderDetails> results = controller.getPublicFolders();
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(details.getName(), results.get(0).getName());
    }

    @Test
    public void testCreateNewFolder() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateNewFolder", false);
        String userId = account.getEmail();
        FolderDetails folderDetails = new FolderDetails();
        folderDetails.setName("test");
        FolderDetails folder = controller.createPersonalFolder(userId, folderDetails);
        Assert.assertNotNull(folder);
        Folder f = DAOFactory.getFolderDAO().get(folder.getId());
        Assert.assertNotNull(f);
        Assert.assertEquals("test", f.getName());
    }

    @Test
    public void testRetrieveFolderContents() throws Exception {
        // test with null id
        controller.retrieveFolderContents(null, 0, ColumnField.PART_ID, false, 0, 10);

        Account account = AccountCreator.createTestAccount("testRetrieveFolderContents", false);
        String userId = account.getEmail();

        FolderDetails folderDetails = new FolderDetails();
        folderDetails.setName("test");

        // create folder
        FolderDetails folder = controller.createPersonalFolder(userId, folderDetails);
        Assert.assertNotNull(folder);
        final short size = 105;

        // create 100 test strains
        HashMap<String, Entry> parts = new HashMap<>();
        for (int i = 0; i < size; i += 1) {
            Strain strain = TestEntryCreator.createTestStrain(account);
            Assert.assertNotNull(strain);
            parts.put(strain.getPartNumber(), strain);
        }
        Assert.assertEquals(size, parts.size());

        // add to folder
        Folder added = controller.addFolderContents(account, folder.getId(), new ArrayList<>(parts.values()));
        Assert.assertNotNull(added);

        // keep track to find duplicates
        HashSet<Long> set = new HashSet<>();

        // retrieve (supported sort types created, status, name, part_id, type)
        FolderDetails details = controller.retrieveFolderContents(account.getEmail(), folder.getId(),
                                                                  ColumnField.PART_ID, false,
                                                                  0, 15);
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
            details = controller.retrieveFolderContents(account.getEmail(), folder.getId(), ColumnField.PART_ID, false,
                                                        pageSize * it, pageSize);
            it += 1;
        }
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
