package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.services.rest.RestClient;

import java.util.Date;
import java.util.HashMap;

/**
 * Remote communications with other ice instances
 *
 * @author Hector Plahar
 */
public final class RemoteContact {

    private final RemotePartnerDAO dao;
    private final TokenHash tokenHash;
    private final RestClient restClient;

    public RemoteContact() {
        dao = DAOFactory.getRemotePartnerDAO();
        tokenHash = new TokenHash();
        restClient = IceRestClient.getInstance();
    }

    /**
     * Checks if the web of registries admin config value has been set to enable this ICE instance
     * to join the web of registries configuration
     *
     * @return true if value has been set to the affirmative, false otherwise
     */
    private boolean isInWebOfRegistries() {
        String value = Utils.getConfigValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        return ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
    }

    /**
     * Adds the registry instance specified by the url to the list of existing partners (if not already in there)
     * and sends a request to the remote instance that includes a security token that the remote instance
     * can use to communicate with this instance.
     * <p>
     * Information about the remote instance is still saved even when it cannot be communicated with. This
     * allows a future communication attempt.
     *
     * @param userId  id of user performing action (must have admin privileges)
     * @param partner registry partner object that contains unique uniform resource identifier for the registry
     * @return add partner ofr
     */
    public RegistryPartner addWebPartner(String userId, RegistryPartner partner) {
        if (!isInWebOfRegistries())
            return null;

        boolean isAdmin = new AccountController().isAdministrator(userId);
        if (!isAdmin || partner.getUrl() == null)
            return null;

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        Logger.info(userId + ": adding WoR partner [" + partner.getUrl() + "]");
        return addWebPartner(myName, myURL, partner);
    }

    protected RegistryPartner addWebPartner(String myName, String myUrl, RegistryPartner partner) {
        // check if partner already exists
        RemotePartner remotePartner = dao.getByUrl(partner.getUrl());
        if (remotePartner != null) {
            if (remotePartner.getPartnerStatus() == RemotePartnerStatus.NOT_CONTACTED) {
                // attempt to make contact
                try {
                    // todo : check response
                    restClient.post(partner.getUrl(), "/rest/web/partner/remote",
                            remotePartner.toDataTransferObject(), RegistryPartner.class);
                } catch (Exception e) {
                    Logger.error("Exception adding remote partner " + e);
                }
            }
            return remotePartner.toDataTransferObject();
        }

        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myUrl);
        thisPartner.setName(myName);
        String apiKey = Utils.generateToken();
        thisPartner.setApiKey(apiKey);  // key to use in contacting this instance

        // send notice to remote for key
        remotePartner = new RemotePartner();
        remotePartner.setName(partner.getName());
        remotePartner.setUrl(partner.getUrl());
        remotePartner.setPartnerStatus(RemotePartnerStatus.NOT_CONTACTED);
        remotePartner.setAuthenticationToken(apiKey);
        remotePartner.setAdded(new Date());

        try {
            restClient.post(partner.getUrl(), "/rest/web/partner/remote", thisPartner, RegistryPartner.class);
            remotePartner.setPartnerStatus(RemotePartnerStatus.PENDING);
        } catch (Exception e) {
            Logger.error("Exception adding remote partner " + e);
        }

        return dao.create(remotePartner).toDataTransferObject();
    }

    /**
     * Handles requests from remote ice instances that will like to be in a WoR config with this instance
     * Serves the dual purpose of:
     * <ul>
     * <li>please add me as a partner to your list with token</li>
     * <li>add accepted; use this as the authorization token</li>
     * </ul>
     * <p>
     * Note that the request is rejected if this ICE instance has not opted to be a member of web of
     * registries
     *
     * @param request partner request object containing all information needed
     * @return true if request is processed successfully, false otherwise
     */
    public RegistryPartner handleRemoteAddRequest(RegistryPartner request) {
        if (!isInWebOfRegistries())
            return null;

        if (request == null || StringUtils.isEmpty(request.getUrl()) || StringUtils.isEmpty(request.getApiKey()))
            return null;

        Logger.info(request.getUrl() + ": request to connect. Details [" + request.toString() + "]");

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (request.getUrl().equalsIgnoreCase(myURL))
            return null;

        String name = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        if (StringUtils.isEmpty(name))
            name = myURL;

        boolean apiKeyValidates = apiKeyValidates(myURL, request);
        if (!apiKeyValidates)
            return null;

        // request should contain api key for use to contact third party
        RemotePartner partner = dao.getByUrl(request.getUrl());
        String token;

        if (partner != null) {
            Logger.info("Updating authentication");
            // validated. update the authorization token
            partner.setApiKey(request.getApiKey());
            partner.setSalt(tokenHash.generateSalt());
            token = tokenHash.generateRandomToken();
            partner.setAuthenticationToken(tokenHash.encryptPassword(token, partner.getSalt()));
            dao.update(partner);
        } else {
            // save in db
            partner = new RemotePartner();
            partner.setName(request.getName());
            partner.setUrl(request.getUrl());
            partner.setApiKey(request.getApiKey());
            partner.setAdded(new Date());
            partner.setPartnerStatus(RemotePartnerStatus.APPROVED);
            partner.setSalt(tokenHash.generateSalt());
            token = tokenHash.generateRandomToken();
            partner.setAuthenticationToken(tokenHash.encryptPassword(token, partner.getSalt()));
            dao.create(partner);
        }

        // send information about this instance
        RegistryPartner newPartner = new RegistryPartner();
        newPartner.setName(name);
        newPartner.setUrl(myURL);
        newPartner.setApiKey(token);
        return newPartner;
    }

    protected boolean apiKeyValidates(String myURL, RegistryPartner registryPartner) {
        if (StringUtils.isEmpty(registryPartner.getApiKey()))
            return false;

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("url", myURL);
        RegistryPartner response = restClient.get(registryPartner.getUrl(), "/accesstoken/web",
                RegistryPartner.class, queryParams);
        if (response == null) { // todo : should retry up to a certain number of times
            Logger.error("Could not validate request");
            return false;
        }
        return true;
    }

    public boolean handleRemoteRemoveRequest(String worToken, String url) {
        if (StringUtils.isEmpty(worToken) || StringUtils.isEmpty(url))
            return false;

        RemotePartner partner = dao.getByUrl(url);
        if (partner == null)
            return false;

        if (!partner.getAuthenticationToken().equals(tokenHash.encryptPassword(worToken, partner.getSalt()))) {
            Logger.error("Attempt to remove remote partner " + url + " with invalid worToken " + worToken);
            return false;
        }

        Logger.info("Deleting partner " + url + " at their request");
        dao.delete(partner); // todo : contact other instances (if this is a master node)
        return true;
    }
}
