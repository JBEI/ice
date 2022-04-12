package org.jbei.ice.folder;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.folder.FolderType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Folder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderPermissionsTest {

    @Before
    public void setUp() throws Exception {
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }

    @Test
    public void testCreateFolderPermission() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("FolderPermissionsTest.testCreateFolderPermission", false);
        String userId = account.getEmail();
        Folder folder = new Folder();
        folder.setOwnerEmail(userId);
        folder.setType(FolderType.PRIVATE);
        folder.setDescription("test folder");
        folder.setName("test");
        folder = DAOFactory.getFolderDAO().create(folder);
        Assert.assertNotNull(folder);

        FolderPermissions folderPermissions = new FolderPermissions(userId, folder.getId());
        AccessPermission accessPermission = new AccessPermission();

        // create a new account
        AccountModel account2 = AccountCreator.createTestAccount("FolderPermissionsTest.testCreateFolderPermission2", false);

        // give read permission to folder for account
        accessPermission.setArticle(AccessPermission.Article.ACCOUNT);
        accessPermission.setType(AccessPermission.Type.READ_FOLDER);
        accessPermission.setArticleId(account2.getId());
        accessPermission.setTypeId(folder.getId());

        Assert.assertNotNull(folderPermissions.createPermission(accessPermission));
    }
}
