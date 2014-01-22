package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;

/**
 * @author Hector Plahar
 */
public class EntryControllerTest {

    private EntryController controller;

    @Before
    public void setUp() {
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new EntryController();
    }

    @After
    public void tearDown() {
        HibernateHelper.commitTransaction();
    }


}
