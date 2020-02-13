package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.ConfigurationModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class ConfigurationModelDAOTest extends HibernateRepositoryTest {

    private ConfigurationDAO dao = new ConfigurationDAO();

    @Test
    public void testSave() throws Exception {
        ConfigurationModel config = new ConfigurationModel();
        config.setKey("foo");
        config.setValue("bar");
        config = dao.create(config);
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getId() > 0);
    }

    @Test
    public void testGet() throws Exception {
        ConfigurationModel config = new ConfigurationModel();
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
