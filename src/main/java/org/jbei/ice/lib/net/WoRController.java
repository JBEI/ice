package org.jbei.ice.lib.net;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.services.webservices.ServiceException;

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

    public WebOfRegistries addWebPartner(String partnerUrl, String partnerName) throws ControllerException {
        if (partnerUrl == null || partnerUrl.trim().isEmpty())
            return null;

        addRegistryPartner(partnerUrl, partnerName);

        WebOfRegistries partners = getRegistryPartners();
        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myURL);
        thisPartner.setName(myName);
        thisPartner.setStatus(RemotePartnerStatus.APPROVED);

        ArrayList<RegistryPartner> partnerArrayList = partners.getPartners();
        partnerArrayList.add(thisPartner);

        partners.setPartners(partnerArrayList);
        Logger.info("Returning partners of size " + partnerArrayList.size());
        return partners;
    }

    private void addRegistryPartner(String url, String name) throws ControllerException {
        RemotePartner partner = dao.getByUrl(url);
        if (partner != null) {
            partner.setName(name);
            try {
                dao.update(partner);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            if (name == null || name.trim().isEmpty())
                name = url;

            partner = new RemotePartner();
            partner.setUrl(url);
            partner.setName(name);
            partner.setAdded(new Date());
            partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
            try {
                dao.save(partner);
            } catch (DAOException de) {
                throw new ControllerException(de);
            }
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
        String NODE_MASTER = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);

        try {
            controller.setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, Boolean.toString(value));
            controller.setPropertyValue(ConfigurationKey.URI_PREFIX, url);

            if (NODE_MASTER.equalsIgnoreCase(url))
                return true;

            String name = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

            if (name.trim().isEmpty()) {
                name = url;
            }

            RegistryAPIServiceClient client = RegistryAPIServiceClient.getInstance();
            IRegistryAPI api = client.getAPIPortForURL(NODE_MASTER);
            WebOfRegistries wor = api.setRegistryPartnerAdd(url, name, value);
            if (!value)
                return true;

            // set values
            for (RegistryPartner partner : wor.getPartners()) {
                addRegistryPartner(partner.getUrl(), partner.getName());
            }
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
