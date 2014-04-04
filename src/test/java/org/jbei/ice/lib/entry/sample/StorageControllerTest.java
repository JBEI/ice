package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class StorageControllerTest {
    StorageController storageController;

    @Before
    public void setUp() throws Exception {
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        storageController = new StorageController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testCreateStrainStorageRoot() throws Exception {
        storageController.createStrainStorageRoot();
    }
}
