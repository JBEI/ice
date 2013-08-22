package org.jbei.ice.lib.net;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

import junit.framework.Assert;
import org.junit.After;
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
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testIsWebEnabled() throws Exception {
        Assert.assertFalse(controller.isWebEnabled());
        ControllerFactory.getConfigurationController().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "yes");
        Assert.assertTrue(controller.isWebEnabled());
        ControllerFactory.getConfigurationController().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "no");
        Assert.assertFalse(controller.isWebEnabled());
    }

    @Test
    public void testIsValidWebPartner() throws Exception {
        String webPartner = "public-registry.jbei.org";
        Assert.assertFalse(controller.isValidWebPartner(webPartner));
        controller.addWebPartner(webPartner, webPartner);
        Assert.assertTrue(controller.isValidWebPartner(webPartner));
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
    public void testRemoveWebPartner() throws Exception {
        String webPartner = "public-registry.jbei.org";
        Assert.assertFalse(controller.isValidWebPartner(webPartner));
        controller.addWebPartner(webPartner, webPartner);
        Assert.assertTrue(controller.isValidWebPartner(webPartner));
        controller.removeWebPartner(webPartner);
        Assert.assertFalse(controller.isValidWebPartner(webPartner));
    }

    @Test
    public void testSetEnable() throws Exception {
        Assert.assertFalse(controller.isWebEnabled());
        ControllerFactory.getConfigurationController().setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, "yes");
    }
}
