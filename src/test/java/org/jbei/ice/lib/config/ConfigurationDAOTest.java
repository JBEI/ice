package org.jbei.ice.lib.config;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class ConfigurationDAOTest {

    private ConfigurationDAO dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateHelper.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateHelper.beginTransaction();
        dao = new ConfigurationDAO();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testSave() throws Exception {
    }

    @Test
    public void testGet() throws Exception {
    }
}
