package org.jbei.ice.lib.bulkimport;

import junit.framework.Assert;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

/**
 * @auther Hector Plahar
 */
public class BulkImportDraftDAOTest {

    private BulkImportDraftDAO dao;
    private AccountController controller;

    @Before
    public void setUp() throws Exception {
//        HibernateHelper.initializeMock();
        dao = new BulkImportDraftDAO();
        controller = new AccountController();
    }

    @Test
    public void testRetrieveById() throws Exception {
        Account account = controller.getSystemAccount();
        controller.save(account);
        BulkImportDraft draft = new BulkImportDraft();
        draft.setCreationTime(new Date());
        draft.setLastUpdateTime(new Date());
        draft.setAccount(account);
        dao.save(draft);

        ArrayList<BulkImportDraft> result = dao.retrieveByAccount(account);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testRetrieveByAccount() throws Exception {
    }

    @Test
    public void testUpdateWithNewEntry() throws Exception {
        BulkImportDraft draft = dao.retrieveByIdWithContents(2);
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
        BulkImportDraft draft = dao.retrieveByIdWithContents(2);
        Assert.assertNotNull(draft);
        dao.delete(draft);
    }

    @After
    public void tearDown() throws Exception {
    }
}
