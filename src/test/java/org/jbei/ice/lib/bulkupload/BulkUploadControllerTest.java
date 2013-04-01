package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;

/**
 * Unit test for BulkUploadController
 *
 * @author Hector Plahar
 */
public class BulkUploadControllerTest {

    private BulkUploadController controller;

    @Before
    public void setUp() throws Exception {
//        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new BulkUploadController();
        AccountController accountController = ControllerFactory.getAccountController();
        if (accountController.getByEmail("system") == null)
            accountController.createNewAccount("System", "Account", "", "system", "", "");
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.rollbackTransaction();
    }
}
