package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dao.hibernate.HibernateUtil;

import org.junit.After;
import org.junit.Before;

/**
 * @author Hector Plahar
 */
public class EntryCreatorTest {

    private EntryCreator creator = new EntryCreator();

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
