package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.account.model.Account;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for BulkUploadController
 *
 * @author Hector Plahar
 */
public class BulkUploadControllerTest {

    private BulkUploadController controller;

    @Before
    public void setUp() throws Exception {
        controller = new BulkUploadController();
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
