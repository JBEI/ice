package org.jbei.ice.storage.hibernate;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class HibernateRepositoryTest {

    @BeforeClass
    public static void runOnce() {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.rollbackTransaction();
    }
}