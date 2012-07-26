package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.server.dao.hibernate.HibernateHelper;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author Hector Plahar
 */
public class BulkUploadDAOTest {

    private BulkUploadDAO dao;
    private AccountController controller;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
        dao = new BulkUploadDAO();
        controller = mock(AccountController.class);
    }

    @Test
    public void testRetrieveById() throws Exception {
        Account account = mock(Account.class);

        BulkUpload draft = new BulkUpload();
        draft.setCreationTime(new Date());
        draft.setLastUpdateTime(new Date());
        draft.setAccount(account);
        dao.save(draft);

        ArrayList<BulkUpload> result = dao.retrieveByAccount(account);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testRetrieveByAccount() throws Exception {
    }

    @Test
    public void testUpdateWithNewEntry() throws Exception {
        BulkUpload draft = dao.retrieveByIdWithContents(2);
        EntryController entryController = new EntryController();
        Account account = controller.getSystemAccount();
        Entry entry = entryController.get(account, 2346);
        Assert.assertNotNull(entry);
        dao.addEntry(draft, entry);
        draft = dao.retrieveByIdWithContents(2);
        Assert.assertEquals(1, draft.getContents().size());
    }

    @Test
    public void testDelete() throws Exception {
        BulkUpload draft = dao.retrieveByIdWithContents(2);
        Assert.assertNotNull(draft);
        dao.delete(draft);
    }

    @After
    public void tearDown() throws Exception {
    }
}
