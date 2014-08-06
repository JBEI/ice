package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dao.hibernate.HibernateUtil;

import org.junit.After;
import org.junit.Before;

/**
 * @author Hector Plahar
 */
public class EntryControllerTest {

    private EntryController controller;

    @Before
    public void setUp() {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new EntryController();
    }

    @After
    public void tearDown() {
        HibernateUtil.commitTransaction();
    }


}
