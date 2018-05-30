package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PermissionDAOTest extends HibernateRepositoryTest {

    private PermissionDAO dao = new PermissionDAO();

    @Test
    public void testGet() {
        Permission model = new Permission();
        model.setSecret("s3crit");
        model = dao.create(model);
        Assert.assertNotNull(model);
        model = dao.get(model.getId());
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getSecret(), "s3crit");
    }

    @Test
    public void testGetByFolder() throws Exception {
        Account account = AccountCreator.createTestAccount("RemoteShareModelDAOTest.testGetByFolder", false);

        Permission model = new Permission();
        model.setSecret("s3crit3");

        Folder folder = new Folder();
        folder.setName("test");
        folder.setType(FolderType.PRIVATE);
        folder.setOwnerEmail("foo");
        folder = DAOFactory.getFolderDAO().create(folder);
        Assert.assertNotNull(folder);

        model.setAccount(account);
        model.setCanRead(true);
        model.setFolder(folder);

        RemoteClientModel remoteClientModel = new RemoteClientModel();
        remoteClientModel.setEmail(account.getEmail());
        RemotePartner partner = new RemotePartner();
        partner.setUrl("test-test.jbei.org");
        partner = DAOFactory.getRemotePartnerDAO().create(partner);
        Assert.assertNotNull(partner);

        remoteClientModel.setRemotePartner(partner);
        remoteClientModel = DAOFactory.getRemoteClientModelDAO().create(remoteClientModel);
        model.setClient(remoteClientModel);

        Assert.assertNotNull((model = dao.create(model)));

        List<Permission> results = dao.getFolderPermissions(folder);
        Assert.assertNotNull(results);
    }
}
