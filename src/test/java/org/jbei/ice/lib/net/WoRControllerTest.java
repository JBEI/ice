package org.jbei.ice.lib.net;

import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.WebOfRegistries;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link WoRController}
 *
 * @author Hector Plahar
 */
public class WoRControllerTest {

    private WoRController controller;

    @Before
    public void setUp() throws Exception {
        controller = new WoRController();
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.rollbackTransaction();
    }

    @Test
    public void testIsWebEnabled() throws Exception {
        Assert.assertFalse(controller.isWebEnabled());
        new ConfigurationController().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "yes");
        Assert.assertTrue(controller.isWebEnabled());
        new ConfigurationController().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "no");
        Assert.assertFalse(controller.isWebEnabled());
    }

    @Test
    public void testGetRegistryPartners() throws Exception {
        String partner1 = "public-registry.jbei.org";
        String partner2 = "registry.jbei.org";

        WebOfRegistries registries = controller.getRegistryPartners();
        Assert.assertFalse(registries.isWebEnabled());
        Assert.assertTrue(registries.getPartners().isEmpty());

        // add partners
        controller.addWebPartner(partner1, partner1);
        controller.addWebPartner(partner2, partner2);

        registries = controller.getRegistryPartners();
        Assert.assertFalse(registries.isWebEnabled());
        Assert.assertEquals(2, registries.getPartners().size());
    }

    @Test
    public void testAddWebPartner() throws Exception {
        String url = "public-registry.jbei.org";
        controller.addWebPartner(url, url);
    }

    @Test
    public void testSetEnable() throws Exception {
        Assert.assertFalse(controller.isWebEnabled());
        new ConfigurationController().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "yes");
    }
}
