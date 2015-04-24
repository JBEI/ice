package org.jbei.ice.lib.net;

import org.apache.commons.lang.StringUtils;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.RestClient;

import java.util.Date;

/**
 * Remote communications with other ice instances
 *
 * @author Hector Plahar
 */
public final class RemoteContact {

    private final RemotePartnerDAO dao;

    public RemoteContact() {
        dao = DAOFactory.getRemotePartnerDAO();
    }

    /**
     * Checks if the web of registries admin config value has been set to enable this ICE instance
     * to join the web of registries configuration
     *
     * @return true if value has been set to the affirmative, false otherwise
     */
    private boolean isInWebOfRegistries() {
        String value = Utils.getConfigValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        if (StringUtils.isEmpty(value))
            return false;

        if (!"yes".equalsIgnoreCase(value) && !"true".equalsIgnoreCase(value))
            return false;

        return true;
    }

    /**
     * Adds the registry instance specified by the url to the list of existing partners (if not already in there)
     * and sends a request to the remote instance that includes a security token that the remote instance
     * can use to communicate with this instance.
     * <p>
     * Information about the remote instance is still saved even when it cannot be communicated with. This
     * allows a future communication attempt.
     *
     * @param userId  id of user performing action (must have admin privs)
     * @param partner registry partner object that contains unique uniform resource identifier for the registry
     * @return add partner ofr
     */
    public RegistryPartner addWebPartner(String userId, RegistryPartner partner) {
        if (!isInWebOfRegistries())
            return null;

        boolean isAdmin = new AccountController().isAdministrator(userId);
        if (!isAdmin || partner.getUrl() == null)
            return null;

        RestClient client = RestClient.getInstance();

        // check if partner already exists
        RemotePartner remotePartner = dao.getByUrl(partner.getUrl());
        if (remotePartner != null) {
            if (remotePartner.getPartnerStatus() == RemotePartnerStatus.NOT_CONTACTED) {
                // attempt to make contact
                try {
                    // todo : check response
                    client.post(partner.getUrl(), "/rest/web/partner/remote",
                            remotePartner.toDataTransferObject(), RegistryPartner.class);
                } catch (Exception e) {
                    Logger.error("Exception adding remote partner " + e);
                }
            }
            return remotePartner.toDataTransferObject();
        }

        Logger.info(userId + ": adding WoR partner [" + partner.getUrl() + "]");

        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myURL);
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
            client.post(partner.getUrl(), "/rest/web/partner/remote", thisPartner, RegistryPartner.class);
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
    public boolean handleRemoteAddRequest(RegistryPartner request) {
        if (!isInWebOfRegistries())
            return false;

        if (request == null || StringUtils.isEmpty(request.getUrl()) || StringUtils.isEmpty(request.getApiKey()))
            return false;

        Logger.info(request.getUrl() + ": request to connect. Details [" + request.toString() + "]");

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
            dao.update(partner);
            return true;
        }

        // todo : contact request.getUrl() at /rest/accesstoken to validate api key before saving

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

    protected boolean isTrue() {
        return true;
    }
}
