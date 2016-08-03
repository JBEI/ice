package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Configuration;
import org.junit.*;

/**
 * @author Hector Plahar
 */
public class ConfigurationDAOTest {

    private ConfigurationDAO dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateUtil.beginTransaction();
        dao = new ConfigurationDAO();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testSave() throws Exception {
        Configuration config = new Configuration();
        config.setKey("foo");
        config.setValue("bar");
        config = dao.create(config);
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getId() > 0);
    }

    @Test
    public void testGet() throws Exception {
        Configuration config = new Configuration();
        config.setKey(ConfigurationKey.NEW_REGISTRATION_ALLOWED.name());
        config.setValue("true");
        config = dao.create(config);
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
