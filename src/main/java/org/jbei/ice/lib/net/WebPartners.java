package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jbei.ice.lib.access.AccessTokens;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.Date;

/**
 * Partners for web of registries
 *
 * @author Hector Plahar
 */
public class WebPartners {

    private final RemotePartnerDAO dao;
    private final TokenHash tokenHash;
    private final RemoteContact remoteContact;

    public WebPartners() {
        this.dao = DAOFactory.getRemotePartnerDAO();
        this.tokenHash = new TokenHash();
        this.remoteContact = new RemoteContact();
    }

    public RegistryPartner get(String token, String url) {
        String urlToken = AccessTokens.getUrlToken(url);
        if (urlToken == null || token == null || !token.equalsIgnoreCase(urlToken))
            return null;

        RemotePartner remotePartner = dao.getByUrl(url);
        if (remotePartner == null)
            return null;

        return remotePartner.toDataTransferObject();
    }

    /**
     * Process a web partner add request from a remote instance
     *
     * @param detectedUrl detected url of client that sent the request
     * @param newPartner  information about partner
     * @return
     */
    public RegistryPartner processRemoteWebPartnerAdd(String detectedUrl, RegistryPartner newPartner) {
        if (!isInWebOfRegistries())
            return null;

        if (newPartner == null || StringUtils.isEmpty(newPartner.getApiKey())) {
            String errMsg = "Cannot add partner with null info or no api key";
            Logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }

        String partnerUrl = newPartner.getUrl();
        if (!StringUtils.isEmpty(partnerUrl)) {
            if (!partnerUrl.equalsIgnoreCase(detectedUrl)) {
                Logger.warn("Adding web partner with self reported url ('"
                        + partnerUrl + "') different from detected ('" + detectedUrl + "')");
            }
        } else {
            newPartner.setUrl(detectedUrl);
        }

        // todo : enable for production
//        UrlValidator validator = new UrlValidator();
//        boolean isValid = validator.isValid(uri);
//        if (!isValid) {
//            Logger.error("Invalid url " + uri);
//            return null;
//        }
        return handleRemoteAddRequest(newPartner);
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
    public RegistryPartner addNewPartner(String userId, RegistryPartner partner) {
        if (!isInWebOfRegistries())
            return null;

        // check for admin privileges before granting request
        boolean isAdmin = new AccountController().isAdministrator(userId);
        if (!isAdmin || partner.getUrl() == null)
            throw new PermissionException("Non admin attempting to add remote partner");

        String myUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        Logger.info(userId + ": adding WoR partner [" + partner.getUrl() + "]");

        RemotePartner remotePartner = dao.getByUrl(partner.getUrl());
        if (remotePartner != null) {
            return remotePartner.toDataTransferObject();
        }

        // create information about this instance to send to potential partner
        // including a random token for use when contacting this instance
        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myUrl);
        thisPartner.setName(myName);
        String token = tokenHash.generateRandomToken();
        thisPartner.setApiKey(token);

        // check that url is valid (rest client pre-prepends https so do the same)
        UrlValidator validator = new UrlValidator();
        boolean isValid = validator.isValid("https://" + myUrl);
        RemotePartnerStatus partnerStatus;
        if (!isValid) {
            Logger.error("Cannot exchange api token with remote host due to invalid local url \"" + myUrl + "\"");
            partnerStatus = RemotePartnerStatus.NOT_CONTACTED;
        } else {
            RegistryPartner newPartner = remoteContact.contactPotentialPartner(thisPartner, partner.getUrl());
            if (newPartner == null) {
                Logger.error("Could not add web partner. Remote contact failed");
                return null;
            }
            partnerStatus = RemotePartnerStatus.APPROVED;
        }

        remotePartner = createRemotePartnerObject(partner, token, partnerStatus);
        return remotePartner.toDataTransferObject();
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
    protected RegistryPartner handleRemoteAddRequest(RegistryPartner request) {
        if (!isInWebOfRegistries())
            return null;

        if (request == null || !UrlValidator.getInstance().isValid(request.getUrl()) ||
                StringUtils.isEmpty(request.getApiKey()))
            return null;

        Logger.info(request.getUrl() + ": request to connect.");

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (request.getUrl().equalsIgnoreCase(myURL))
            return null;

        String name = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        if (StringUtils.isEmpty(name))
            name = myURL;

        boolean apiKeyValidates = remoteContact.apiKeyValidates(myURL, request);
        if (!apiKeyValidates)  // todo : save and based on state, enable admin to retry at some point
            return null;

        // request should contain api key for use to contact third party
        RemotePartner partner = dao.getByUrl(request.getUrl());
        String token;

        if (partner != null) {
            Logger.info("Updating authentication for existing");
            // validated. update the authorization token
            partner.setApiKey(request.getApiKey());
            partner.setSalt(tokenHash.generateSalt());
            token = tokenHash.generateRandomToken();
            partner.setAuthenticationToken(tokenHash.encryptPassword(token + request.getUrl(), partner.getSalt()));
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
            String hash = tokenHash.encryptPassword(token + request.getUrl(), partner.getSalt());
            partner.setAuthenticationToken(hash);
            dao.create(partner);
        }

        // send information about this instance
        RegistryPartner newPartner = new RegistryPartner();
        newPartner.setName(name);
        newPartner.setUrl(myURL);
        newPartner.setApiKey(token);
        return newPartner;
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

//    private boolean isMasterNode() {
//        String value = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);
//        String thisUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
//        return value.equalsIgnoreCase(thisUrl);
//    }

    protected RemotePartner createRemotePartnerObject(RegistryPartner newPartner, String token,
                                                      RemotePartnerStatus status) {
        RemotePartner remotePartner = new RemotePartner();
        remotePartner.setName(newPartner.getName());
        remotePartner.setUrl(newPartner.getUrl());   // todo : validate url
        remotePartner.setPartnerStatus(status);
        remotePartner.setSalt(tokenHash.generateSalt());
        String hash = tokenHash.encryptPassword(token + newPartner.getUrl(), remotePartner.getSalt());
        remotePartner.setAuthenticationToken(hash);
        remotePartner.setApiKey(newPartner.getApiKey());
        remotePartner.setAdded(new Date());
        return DAOFactory.getRemotePartnerDAO().create(remotePartner);
    }
}
