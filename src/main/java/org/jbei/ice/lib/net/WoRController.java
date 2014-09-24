package org.jbei.ice.lib.net;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.RestClient;

/**
 * Controller for Web of Registries functionality
 *
 * @author Hector Plahar
 */
public class WoRController {

    private final RemotePartnerDAO dao;

    public WoRController() {
        dao = DAOFactory.getRemotePartnerDAO();
    }

    /**
     * @return true if the administrator of this registry instance has explicitly
     *         enable the web of registries functionality
     */
    public boolean isWebEnabled() {
        String value = new ConfigurationController().getPropertyValue(
                ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
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

    public WebOfRegistries getRegistryPartners(boolean approvedOnly) {
        String value = new ConfigurationController().getPropertyValue(
                ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        WebOfRegistries webOfRegistries = new WebOfRegistries();
        webOfRegistries.setWebEnabled("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));

        // retrieve actual partners
        ArrayList<RemotePartner> partners = dao.retrieveRegistryPartners();

        ArrayList<RegistryPartner> registryPartners = new ArrayList<>();
        for (RemotePartner partner : partners) {
            if (approvedOnly && partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;
            registryPartners.add(partner.toDataTransferObject());
        }

        webOfRegistries.setPartners(registryPartners);
        return webOfRegistries;
    }

    // serves the dual purpose of :
    // please add me as a partner to your list with token
    // add accepted; use this as the authorization token
    public boolean addRemoteWebPartner(RegistryPartner request) {
        Logger.info("Adding remote partner [" + request.toString() + "]");

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (request.getUrl().equalsIgnoreCase(myURL))
            return false;

        // request should contain api key for use to contact third party
        RemotePartner partner = dao.getByUrl(request.getUrl());
        if (partner != null) {
            if (request.getApiKey() == null) {
                Logger.error("Attempting to add partner (" + request.getUrl() + ") that already exists");
                return false;
            }

            // just update the authorization token
            partner.setApiKey(request.getApiKey());
            partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
            dao.update(partner);
            return true;
        }

        // check for api key
        if (request.getApiKey() == null) {
            Logger.error("No api key found for " + request.toString());
            return false;
        }

        // todo : contact request.getUrl() at /rest/accesstoken to validate api key

        // save in db with status pending approval
        partner = new RemotePartner();
        partner.setName(request.getName());
        partner.setUrl(request.getUrl());
        partner.setApiKey(request.getApiKey());
        partner.setAdded(new Date());
        partner.setPartnerStatus(RemotePartnerStatus.PENDING_APPROVAL);
        partner.setAuthenticationToken(Utils.generateToken());
        dao.create(partner);
        return true;
    }

    /**
     * Adds the registry instance specified by the url to the list of existing partners (if not already in there)
     *
     * @param userId  id of user performing action (must have admin privs)
     * @param partner registry partner object that contains unique uniform resource identifier for the registry
     * @return add partner ofr
     */
    public RegistryPartner addWebPartner(String userId, RegistryPartner partner) {
        boolean isAdmin = new AccountController().isAdministrator(userId);
        if (!isAdmin || partner.getUrl() == null)
            return null;

        // todo check if partner already exists
        RemotePartner remotePartner = dao.getByUrl(partner.getUrl());
        if (remotePartner != null) {
            return null;
        }

        Logger.info("Adding partner [" + partner.getUrl() + "]");

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myURL);
        thisPartner.setName(myName);
        String apiKey = Utils.generateToken();
        thisPartner.setApiKey(apiKey);  // key to use in contacting this instance

        // send notice to remote for key
        RestClient client = RestClient.getInstance();
        try {
            client.post(partner.getUrl(), "/rest/web/partner/remote", thisPartner, RegistryPartner.class);

            // save
            remotePartner = new RemotePartner();
            remotePartner.setName(partner.getName());
            remotePartner.setUrl(partner.getUrl());
            remotePartner.setPartnerStatus(RemotePartnerStatus.PENDING);
            remotePartner.setAuthenticationToken(apiKey);
            remotePartner.setAdded(new Date());
            return dao.create(remotePartner).toDataTransferObject();
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    private RegistryPartner addRegistryPartner(String url, String name) {
        RemotePartner partner = dao.getByUrl(url);

        if (partner != null) {
            partner.setName(name);
            if (partner.getAuthenticationToken() == null) {
                partner.setAuthenticationToken(Utils.generateToken());
                partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
            }
            return dao.update(partner).toDataTransferObject();
        } else {
            if (name == null || name.trim().isEmpty())
                name = url;

            partner = new RemotePartner();
            partner.setUrl(url);
            partner.setName(name);
            partner.setAdded(new Date());
            partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
            partner.setAuthenticationToken(Utils.generateToken());
            return dao.create(partner).toDataTransferObject();
        }
    }

    /**
     * Removes the web partner uniquely identified by the url
     *
     * @param partnerUrl url identifier for partner
     */
    public boolean removeWebPartner(String userId, String partnerUrl) {
        RemotePartner partner = dao.getByUrl(partnerUrl);
        if (partner == null)
            return true;

        dao.delete(partner);
        return true;
    }

    public boolean updateWebPartner(String userId, String url, RegistryPartner partner) {
        if (!new AccountController().isAdministrator(userId))
            return false;

        Logger.info(userId + ": updating partner (" + url + ") to " + partner.toString());
        RemotePartner existing = dao.getByUrl(url);
        if (existing == null)
            return false;

        RemotePartnerStatus newStatus = RemotePartnerStatus.APPROVED;
        if (newStatus == existing.getPartnerStatus())
            return true;

        // contact remote with new api key that allows them to contact this instance
        String apiKey = Utils.generateToken();
        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myURL);
        thisPartner.setName(myName);
        thisPartner.setApiKey(apiKey);  // key to use in contacting this instance

        RestClient client = RestClient.getInstance();
        try {
            client.post(partner.getUrl(), "/rest/web/partner/remote", thisPartner, RegistryPartner.class);
            existing.setPartnerStatus(newStatus);
            existing.setAuthenticationToken(apiKey);
            dao.update(existing);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
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
     *         exception
     */
    public ArrayList<RegistryPartner> setEnable(String url, boolean enable) throws ControllerException {
        ConfigurationController controller = new ConfigurationController();
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

            // tODO
//            IRegistryAPI api = client.getAPIPortForURL(NODE_MASTER);
//            WebOfRegistries wor = api.setRegistryPartnerAdd(url, name, enable);
//            if (!enable)
            return new ArrayList<>();

//            // set values
//            Iterator<RegistryPartner> iterator = wor.getPartners().iterator();
//            while (iterator.hasNext()) {
//                RegistryPartner partner = iterator.next();
//                if (partner.getUrl().isEmpty() || url.equalsIgnoreCase(partner.getUrl())) {
//                    iterator.remove();
//                    continue;
//                }
//                addRegistryPartner(partner.getUrl(), partner.getName());
//            }
//
//            WebOfRegistriesContactTask contactTask = new WebOfRegistriesContactTask(wor.getPartners());
//            IceExecutorService.getInstance().runTask(contactTask);
//            return wor.getPartners();
        } catch (Exception e) {
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
            token = Utils.generateToken();
            partner.setAuthenticationToken(token);
            dao.update(partner);
            return token;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public RegistryPartner getWebPartner(String userId, long partnerId) {
        return dao.get(partnerId).toDataTransferObject();
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
                dao.create(partner);
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

}
