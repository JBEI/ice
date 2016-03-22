package org.jbei.ice.lib.access;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.folder.collection.CollectionType;
import org.jbei.ice.lib.folder.collection.Collections;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
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
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testAdd() throws Exception {
        Account account = AccountCreator.createTestAccount("RemoteAccessTest.testAdd", false);
        RemoteAccess remoteAccess = new RemoteAccess();
        RemotePartner partner = new RemotePartner();
        partner.setUrl("registry.jbei.org");
        partner = DAOFactory.getRemotePartnerDAO().create(partner);

        // create permission to share with this user
        AccessPermission permission = new AccessPermission();
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail("I wanna share from far away");
        permission.setAccount(accountTransfer);
        permission.setDisplay("Folder Name");
        permission.setUserId(account.getEmail());
        permission.setType(AccessPermission.Type.READ_FOLDER);
        permission.setSecret("supersekrit");

        remoteAccess.add(partner.toDataTransferObject(), permission);

        Collections collections = new Collections(account.getEmail());
        List<FolderDetails> subFolders = collections.getSubFolders(CollectionType.SHARED);
        Assert.assertEquals(1, subFolders.size());
    }
}