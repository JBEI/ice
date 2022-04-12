package org.jbei.ice.access;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.account.Account;
import org.jbei.ice.config.ConfigurationSettings;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.folder.FolderDetails;
import org.jbei.ice.folder.collection.CollectionType;
import org.jbei.ice.folder.collection.Collections;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.RemoteAccessModel;
import org.jbei.ice.storage.model.RemotePartner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class RemoteAccessTest {

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
    public void testAdd() throws Exception {
        new ConfigurationSettings().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "true");

        // this tests the scenario where a WoR partner shares a remote resource (folder or entry) with a user
        // on another instance
        AccountModel accountModel = AccountCreator.createTestAccount("RemoteAccessTest.testAdd", false);
        RemoteAccess remoteAccess = new RemoteAccess();

        // create mock remote partner
        RemotePartner partner = new RemotePartner();
        partner.setUrl("remote-test.jbei.org");
        partner = DAOFactory.getRemotePartnerDAO().create(partner);

        // create permission to share with this user
        AccessPermission permission = new AccessPermission();
        Account account = new Account();
        account.setEmail("I wanna share from far away"); // person sharing

        permission.setAccount(account);
        permission.setDisplay("Folder Name");
        permission.setUserId(account.getEmail());
        permission.setType(AccessPermission.Type.READ_FOLDER);
        permission.setTypeId(Integer.MAX_VALUE);
        permission.setSecret("supersekrit");

        AccessPermission accessPermission = remoteAccess.add(partner.toDataTransferObject(), permission);
        Folder folder = DAOFactory.getFolderDAO().get(accessPermission.getTypeId());
        RemoteAccessModel model = DAOFactory.getRemoteAccessModelDAO().getByFolder(accountModel, folder);
        Assert.assertNotNull(model);

        // shared folder should be in list of shared collection
        Collections collections = new Collections(account.getEmail());
        List<FolderDetails> subFolders = collections.getSubFolders(CollectionType.SHARED);
        boolean found = false;
        for (FolderDetails details : subFolders) {
            found = details.getName().equalsIgnoreCase(permission.getDisplay())
                    && Long.toString(permission.getTypeId()).equals(details.getDescription());
            if (found)
                break;
        }
        Assert.assertTrue(found);
    }
}
