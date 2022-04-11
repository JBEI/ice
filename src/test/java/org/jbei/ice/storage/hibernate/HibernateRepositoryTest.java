package org.jbei.ice.storage.hibernate;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class HibernateRepositoryTest {

    @BeforeClass
    public static void runOnce() {
        HibernateConfiguration.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateConfiguration.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.rollbackTransaction();
    }
}