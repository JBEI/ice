package org.jbei.ice.lib.config;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.shared.dto.ConfigurationKey;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class ConfigurationDAOTest {

    private ConfigurationDAO dao;

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
        Configuration vaa = dao.get(ConfigurationKey.PROJECT_NAME);
        Assert.assertNotNull(vaa);
    }
}
