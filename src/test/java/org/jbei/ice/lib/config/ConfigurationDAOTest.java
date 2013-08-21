package org.jbei.ice.lib.config;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

import org.junit.After;
import org.junit.Assert;
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
        Configuration config = new Configuration();
        config.setKey("foo");
        config.setValue("bar");
        config = dao.save(config);
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getId() > 0);
    }

    @Test
    public void testGet() throws Exception {
        Configuration config = new Configuration();
        config.setKey(ConfigurationKey.NEW_REGISTRATION_ALLOWED.name());
        config.setValue("true");
        config = dao.save(config);
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getId() > 0);

        config = dao.get(ConfigurationKey.NEW_REGISTRATION_ALLOWED);
        Assert.assertNotNull(config);
        Assert.assertEquals("true", config.getValue());

        config = dao.get(ConfigurationKey.NEW_REGISTRATION_ALLOWED.name());
        Assert.assertNotNull(config);
        Assert.assertEquals("true", config.getValue());
    }
}
