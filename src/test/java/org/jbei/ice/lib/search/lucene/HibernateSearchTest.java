package org.jbei.ice.lib.search.lucene;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class HibernateSearchTest {

    @Before
    public void setUp() {
//        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
    }

    @After
    public void tearDown() {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testExecuteSearch() throws Exception {
    }

    @Test
    public void testExecuteMultiTerm() throws Exception {
    }
}
