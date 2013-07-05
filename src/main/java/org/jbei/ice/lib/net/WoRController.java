package org.jbei.ice.lib.net;

import java.util.ArrayList;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

/**
 * Controller for Web of Registries functionality
 *
 * @author Hector Plahar
 */
public class WoRController {

    private final RemotePartnerDAO dao;

    public WoRController() {
        dao = new RemotePartnerDAO();
    }

    public WebOfRegistries getRegistryPartners() throws ControllerException {
        String value = ControllerFactory.getConfigurationController().getPropertyValue(
                ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        WebOfRegistries webOfRegistries = new WebOfRegistries();
        webOfRegistries.setWebEnabled("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));

        // retrieve actual partners
        ArrayList<RemotePartner> partners;

        try {
            partners = dao.retrieveRegistryPartners();
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        for (RemotePartner partner : partners)
            webOfRegistries.getPartners().add(RemotePartner.toDTO(partner));

        return webOfRegistries;
    }

    /**
     * Upgrades older versions of the registry to use the more fully featured data model for web of registries
     */
    public void upgradeWebOfRegistries() {

    }
}
