package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Hector Plahar
 */
public class FolderControllerTest extends HibernateRepositoryTest {

    private FolderController controller = new FolderController();

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
}
