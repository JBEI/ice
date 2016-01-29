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
    private RemoteContact remoteContact;

    public WebPartners() {
        this.dao = DAOFactory.getRemotePartnerDAO();
        this.tokenHash = new TokenHash();
    }

    public WebPartners(RemoteContact remoteContact) {
        this();
        this.remoteContact = remoteContact;
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

        if (!isValidUrl(partnerUrl)) {
            Logger.error("Invalid url " + partnerUrl);
            return null;
        }
        return handleRemoteAddRequest(newPartner);
    }

    /**
     * Validates the url by prepending "https://" as the scheme
     *
     * @param url without scheme
     * @return true if url validates successfully, false otherwise
     */
    protected boolean isValidUrl(String url) {
        url = "https://" + url;
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
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
        if (!isAdmin)
            throw new PermissionException("Non admin attempting to add remote partner");

        if (StringUtils.isEmpty(partner.getUrl()))
            throw new IllegalArgumentException("Cannot add partner without valid url");

        String myUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        // check if there is a partner that that url
        RemotePartner remotePartner = dao.getByUrl(partner.getUrl());
        if (remotePartner != null) {
            return remotePartner.toDataTransferObject();
        }

        Logger.info(userId + ": adding WoR partner [" + partner.getUrl() + "]");

        // create information about this instance to send to potential partner
        // including a random token for use when contacting this instance
        String token = tokenHash.generateRandomToken();

        // check that url is valid (rest client pre-prepends https so do the same)
        boolean isValid = isValidUrl(myUrl);
        RemotePartnerStatus partnerStatus;
        if (!isValid) {
            // will not contact
            Logger.error("Cannot exchange api token with remote host due to invalid local url \"" + myUrl + "\"");
            partnerStatus = RemotePartnerStatus.NOT_CONTACTED;
        } else {
            RegistryPartner thisPartner = new RegistryPartner();
            thisPartner.setUrl(myUrl);
            thisPartner.setName(myName);
            thisPartner.setApiKey(token);

            RegistryPartner newPartner = remoteContact.contactPotentialPartner(thisPartner, partner.getUrl());
            if (newPartner == null) {
                // contact failed
                Logger.error("Remote contact of partner " + partner.getUrl() + " failed");
                partnerStatus = RemotePartnerStatus.CONTACT_FAILED;
            } else {
                // contact succeeded with return of api key
                partnerStatus = RemotePartnerStatus.APPROVED;
                partner.setApiKey(newPartner.getApiKey()); // todo : check api key (validate?)
            }
        }

        // if status is not approved, then the token is irrelevant since it is not stored and was not
        // successfully transmitted
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
     * @param request partner request object containing all information needed with a validated url
     * @return information about this instance to be sent to the remote
     */
    protected RegistryPartner handleRemoteAddRequest(RegistryPartner request) {
        if (request == null || StringUtils.isEmpty(request.getApiKey())) {
            Logger.error("Received invalid partner add request");
            return null;
        }

        Logger.info("Processing request to connect by " + request.getUrl());

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (request.getUrl().equalsIgnoreCase(myURL))
            return null;

        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        if (StringUtils.isEmpty(myName))
            myName = myURL;

        boolean apiKeyValidates = remoteContact.apiKeyValidates(myURL, request);
        if (!apiKeyValidates) {
            Logger.error("Received api token could not be validated");
            return null;
        }

        // request should contain api key for use to contact third party
        RemotePartner partner = dao.getByUrl(request.getUrl());
        String token = tokenHash.generateRandomToken();

        // create new partner object or update existing if it exists
        if (partner != null) {
            Logger.info("Updating authentication for existing");
            // validated. update the authorization token
            partner.setApiKey(request.getApiKey());
            partner.setSalt(tokenHash.generateSalt());
            partner.setAuthenticationToken(tokenHash.encryptPassword(token + request.getUrl(), partner.getSalt()));
            dao.update(partner);
        } else {
            // save in db
            createRemotePartnerObject(request, token, RemotePartnerStatus.APPROVED);
        }

        // send information about this instance
        RegistryPartner newPartner = new RegistryPartner();
        newPartner.setName(myName);
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
    protected boolean isInWebOfRegistries() {
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
        remotePartner.setUrl(newPartner.getUrl());
        remotePartner.setPartnerStatus(status);
        if (status == RemotePartnerStatus.APPROVED) {
            remotePartner.setSalt(tokenHash.generateSalt());
            String hash = tokenHash.encryptPassword(token + newPartner.getUrl(), remotePartner.getSalt());
            remotePartner.setAuthenticationToken(hash);
            remotePartner.setApiKey(newPartner.getApiKey());
        }
        remotePartner.setAdded(new Date());
        return DAOFactory.getRemotePartnerDAO().create(remotePartner);
    }
}
