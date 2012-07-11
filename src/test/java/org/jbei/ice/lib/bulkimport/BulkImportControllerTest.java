package org.jbei.ice.lib.bulkimport;

import org.jbei.ice.lib.account.model.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for BulkImportController
 * 
 * @author Hector Plahar
 */
public class BulkImportControllerTest {

    private BulkImportController controller;

    @Before
    public void setUp() throws Exception {
        controller = new BulkImportController();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRetrievePendingImports() throws Exception {
        Account account = Mockito.mock(Account.class);
        controller.retrievePendingImports(account);
    }

    @Test
    public void testRetrieveById() throws Exception {

    }

    @Test
    public void testRetrieveByUser() throws Exception {

    }

    @Test
    public void testDeleteDraftById() throws Exception {

    }

    @Test
    public void testCreateBulkImportDraft() throws Exception {

    }

    @Test
    public void testUpdateBulkImportDraft() throws Exception {

    }

    @Test
    public void testSubmitBulkImportForVerification() throws Exception {

    }
}
