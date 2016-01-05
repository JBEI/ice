package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.AccessTokens;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

/**
 * Partners for web of registries
 *
 * @author Hector Plahar
 */
public class WebPartners {

    private final RemotePartnerDAO dao;

    public WebPartners() {
        this.dao = DAOFactory.getRemotePartnerDAO();
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

    public RegistryPartner addNewPartner(String userId, String uri, RegistryPartner partner) {
        if (!isInWebOfRegistries())
            return null;

        if (!StringUtils.isEmpty(uri)) {
            return addPartnerFromRemoteRequest(uri, partner);
        }

        // todo : enable for production
//        UrlValidator validator = new UrlValidator();
//        boolean isValid = validator.isValid(uri);
//        if (!isValid) {
//            Logger.error("Invalid url " + uri);
//            return null;
//        }

        RemoteContact remoteContact = new RemoteContact();
        return remoteContact.addWebPartner(userId, partner);
    }

    protected RegistryPartner addPartnerFromRemoteRequest(String detectedUrl, RegistryPartner newPartner) {
        if (newPartner == null || StringUtils.isEmpty(newPartner.getApiKey())) {
            String errMsg = "Cannot add partner with null info or no api key";
            Logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }

        String partnerUrl = newPartner.getUrl();
        if (!StringUtils.isEmpty(partnerUrl)) {
            if (!partnerUrl.equalsIgnoreCase(detectedUrl)) {
                Logger.warn("Adding web partner with reported url ('"
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
        RemoteContact remoteContact = new RemoteContact();
        return remoteContact.handleRemoteAddRequest(newPartner);
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

    private boolean isMasterNode() {
        String value = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);
        String thisUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        return value.equalsIgnoreCase(thisUrl);
    }
}
