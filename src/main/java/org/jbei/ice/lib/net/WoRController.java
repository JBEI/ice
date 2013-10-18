package org.jbei.ice.lib.net;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.executor.IceExecutorService;
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

    /**
     * @return true if the administrator of this registry instance has explicitly
     *         enable the web of registries functionality
     */
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
     * @param apiKey    authentication key known only to this partner
     * @return true if partner identified by the id is determined to be a valid
     *         web of registries partner for part transfer based on the status and authentication key
     */
    public boolean isValidWebPartner(String partnerId, String apiKey) throws ControllerException {
        try {
            RemotePartner partner = dao.getByUrl(partnerId);
            return partner != null && partner.getPartnerStatus() == RemotePartnerStatus.APPROVED
                    && apiKey != null && apiKey.equalsIgnoreCase(partner.getAuthenticationToken());
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
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

        ArrayList<RegistryPartner> registryPartners = new ArrayList<>();
        for (RemotePartner partner : partners)
            registryPartners.add(RemotePartner.toDTO(partner));

        webOfRegistries.setPartners(registryPartners);

        return webOfRegistries;
    }

    /**
     * Adds the registry instance specified by the url to the list of existing partners (if not already in there)
     *
     * @param partnerUrl  unique uniform resource identifier for the registry
     * @param partnerName display name for the registry instance
     * @return list of existing instances
     * @throws ControllerException
     */
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
        thisPartner.setStatus(RemotePartnerStatus.APPROVED.name());

        ArrayList<RegistryPartner> partnerArrayList = new ArrayList<>(partners.getPartners());
        partnerArrayList.add(thisPartner);
        partners.setPartners(partnerArrayList);

        Logger.info("Returning partners of size " + partnerArrayList.size());
        return partners;
    }

    private void addRegistryPartner(String url, String name) throws ControllerException {
        RemotePartner partner;
        try {
            partner = dao.getByUrl(url);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (partner != null) {
            partner.setName(name);
            if (partner.getAuthenticationToken() == null) {
                partner.setAuthenticationToken(UUID.randomUUID().toString());
                partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
            }
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
            partner.setAuthenticationToken(UUID.randomUUID().toString());
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
        RemotePartner partner;
        try {
            partner = dao.getByUrl(partnerUrl);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (partner == null)
            return;

        try {
            dao.delete(partner);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Enables or disables web of registries (WoR) functionality and contacts the node master (if this
     * system is not it) with request to join the web of registries.
     * <p/>
     * If enabling web of registries, a task is started in a separate thread to contact each of the
     * registries in the WoR configuration for authentication keys and to provide them with the same
     * when making requests to this server
     *
     * @param url    this site's url
     * @param enable if true, enables WoR; disables it otherwise
     * @return list of received partners if WoR functionality is being enabled, is successful and
     *         this is not the master node, otherwise it just returns an empty list, or null in the event of an
     *         exeption
     */
    public ArrayList<RegistryPartner> setEnable(String url, boolean enable) throws ControllerException {
        ConfigurationController controller = ControllerFactory.getConfigurationController();
        String NODE_MASTER = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);

        try {
            controller.setPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES, Boolean.toString(enable));
            controller.setPropertyValue(ConfigurationKey.URI_PREFIX, url);

            if (NODE_MASTER.equalsIgnoreCase(url))
                return new ArrayList<>();

            // use url if name is empty
            String name = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
            if (name.trim().isEmpty()) {
                name = url;
            }

            RegistryAPIServiceClient client = RegistryAPIServiceClient.getInstance();
            IRegistryAPI api = client.getAPIPortForURL(NODE_MASTER);
            WebOfRegistries wor = api.setRegistryPartnerAdd(url, name, enable);
            if (!enable)
                return new ArrayList<>();

            // set values
            Iterator<RegistryPartner> iterator = wor.getPartners().iterator();
            while (iterator.hasNext()) {
                RegistryPartner partner = iterator.next();
                if (partner.getUrl().isEmpty() || url.equalsIgnoreCase(partner.getUrl())) {
                    iterator.remove();
                    continue;
                }
                addRegistryPartner(partner.getUrl(), partner.getName());
            }

            WebOfRegistriesContactTask contactTask = new WebOfRegistriesContactTask(wor.getPartners());
            IceExecutorService.getInstance().runTask(contactTask);
            return wor.getPartners();
        } catch (ServiceException e) {
            Logger.warn("Error contacting master node to remove this server from web of registries");
            return null;
        }
    }

    /**
     * Retrieves the stored authentication key for the server specified in the parameter. If no key exists,
     * one is generated and stored
     *
     * @param url remote registry identifier
     * @return generated authentication key or null if the server is not in the list
     * @throws ControllerException
     */
    public String getAuthenticationKey(String url) throws ControllerException {
        try {
            RemotePartner partner = dao.getByUrl(url);
            if (partner == null)
                return null;

            String token = partner.getAuthenticationToken();
            if (token != null && !token.isEmpty())
                return token;

            // generate authentication token
            token = UUID.randomUUID().toString();
            partner.setAuthenticationToken(token);
            dao.update(partner);
            return token;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Retrieves the stored api key for the server specified in the parameter
     * and the server status is approved
     *
     * @param url remote registry identifier
     * @return stored api key if one exists
     * @throws ControllerException
     */
    public String getApiKey(String url) throws ControllerException {
        RemotePartner partner;
        try {
            partner = dao.getByUrl(url);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        if (partner == null || partner.getPartnerStatus() == RemotePartnerStatus.BLOCKED)
            return null;

        return partner.getApiKey();
    }

    public boolean isValidApiKey(String url, String apiKey) throws ControllerException {
        RemotePartner partner;
        try {
            partner = dao.getByUrl(url);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return (partner != null && partner.getPartnerStatus() == RemotePartnerStatus.APPROVED
                && partner.getAuthenticationToken().equals(apiKey));
    }

    /**
     * Associates the api key specified with the registry instance. This key is generated on and
     * received from the remote registry
     *
     * @param url    remote registry identifier
     * @param apiKey authentication key to be used when communicating with the remote registry.
     * @throws ControllerException
     */
    public void setApiKeyForPartner(String url, String apiKey) throws ControllerException {
        try {
            RemotePartner partner = dao.getByUrl(url);
            partner.setApiKey(apiKey);
            dao.update(partner);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public String requestApiKeyForNewPartner(String url, String name, String authenticationKey)
            throws ControllerException {
        try {
            RemotePartner partner = dao.getByUrl(url);
            if (partner == null) {
                partner = new RemotePartner();
                partner.setUrl(url);
                partner.setName(name);
                partner.setAdded(new Date());
                partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
                partner.setAuthenticationToken(UUID.randomUUID().toString());
                partner.setApiKey(authenticationKey);
                dao.save(partner);
            } else {
                if (partner.getAuthenticationToken() == null)
                    partner.setAuthenticationToken(UUID.randomUUID().toString());
                partner.setApiKey(authenticationKey);
                dao.update(partner);
            }
            return partner.getAuthenticationToken();
        } catch (DAOException ce) {
            throw new ControllerException(ce);
        }
    }

    public RegistryPartner setPartnerStatus(Account account, String url, RemotePartnerStatus status)
            throws ControllerException {
        if (!ControllerFactory.getAccountController().isAdministrator(account))
            return null;

        Logger.info(account.getEmail() + ": setting partner (" + url + ") status to " + status.toString());
        try {
            RemotePartner partner = dao.getByUrl(url);
            if (partner == null)
                return null;

            partner.setPartnerStatus(status);
            partner = dao.update(partner);
            return RemotePartner.toDTO(partner);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }
}
