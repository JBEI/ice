package org.jbei.ice.lib.net;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.services.webservices.ServiceException;

/**
 * Controller for Web of Registries functionality
 *
 * @author Hector Plahar
 */
public class WoRController {

    private final RemotePartnerDAO dao;
    public static final String NODE_MASTER = "registry-test.jbei.org";

    public WoRController() {
        dao = new RemotePartnerDAO();
    }

    public boolean isWebEnabled() {
        try {
            String value = ControllerFactory.getConfigurationController().getPropertyValue(
                    ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
            return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
        } catch (ControllerException e) {
            return false;
        }
    }

    /**
     * @param partnerId unique identifier for web partner
     * @return true if partner identified by the id is determined to be a valid
     *         web of registries partner for part transfer
     */
    public boolean isValidWebPartner(String partnerId) {
        RemotePartner partner = dao.getByUrl(partnerId);
        return partner != null && partner.getPartnerStatus() == RemotePartnerStatus.APPROVED;
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

    public void addWebPartner(String partnerUrl, String partnerName) throws ControllerException {
        if (partnerUrl == null || partnerUrl.trim().isEmpty())
            return;

        RemotePartner partner = dao.getByUrl(partnerUrl);
        if (partner != null) {
            partner.setName(partnerName);
            try {
                dao.update(partner);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        }

        if (partnerName == null || partnerName.trim().isEmpty())
            partnerName = partnerUrl;

        partner = new RemotePartner();
        partner.setUrl(partnerUrl);
        partner.setName(partnerName);
        partner.setAdded(new Date());
        try {
            dao.update(partner);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Removes the web partner uniquely identified by the url
     *
     * @param partnerUrl url identifier for partner
     * @throws ControllerException on error removing partner
     */
    public void removeWebPartner(String partnerUrl) throws ControllerException {
        RemotePartner partner = dao.getByUrl(partnerUrl);
        if (partner == null)
            return;

        try {
            dao.delete(partner);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Enables or disables web of registries (WoR) functionality
     *
     * @param url   this site's url
     * @param value if true, enables WoR; disables it otherwise
     * @return true if operation is successful, false otherwise
     */
    public boolean setEnable(String url, boolean value) throws ControllerException {
        ConfigurationController controller = ControllerFactory.getConfigurationController();
        try {
            controller.setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, Boolean.toString(value));
            controller.setPropertyValue(ConfigurationKey.URI_PREFIX, url);
            String name = controller.getConfiguration(ConfigurationKey.PROJECT_NAME).getValue();
            if (name == null || name.trim().isEmpty()
                    || (name.equals(ConfigurationKey.PROJECT_NAME.getDefaultValue())
                    && !NODE_MASTER.equalsIgnoreCase(url))) {
                name = url;
            }

            RegistryAPIServiceClient client = RegistryAPIServiceClient.getInstance();
            client.getAPIPortForURL(NODE_MASTER).addRegistryPartner(url, name);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        } catch (ServiceException e) {
            Logger.warn("Error contacting master node to remove this server from web of registries");
            return true;
        }
    }
}
