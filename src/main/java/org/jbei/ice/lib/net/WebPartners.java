package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.AccessTokens;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.List;

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

//    public List<RegistryPartner> get() {
//        if (!isInWebOfRegistries())
//            return null;
//
//        RemotePartner partner = dao.getByUrl(url);
//        if (partner == null)
//            return null;
//
//        TokenHash hash = new TokenHash();
//        if (!partner.getAuthenticationToken().equals(hash.encryptPassword(apiKey, partner.getSalt())))
//            return null;
//
//        return getWebPartners();
//    }

    /**
     * Adds a new partner to the web of registries configuration, if the functionality is enabled.
     * <p>
     * There are two instances where the request can be made
     * <p>
     * <ol>
     * <li>This instance is the node master and therefore process all requests</li>
     * <li>An administrator at the instance at <code>url</code> manually added this instance and thereby
     * triggered the key exchange</li>
     * </ol>
     */
    public RegistryPartner addNewPartner(String uri, RegistryPartner partner) {
        if (!isInWebOfRegistries())
            return null;

        if (partner == null || StringUtils.isEmpty(partner.getApiKey())) {
            String errMsg = "Cannot add partner with null info or no api key";
            Logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }

        // todo : enable for production
//        UrlValidator validator = new UrlValidator();
//        boolean isValid = validator.isValid(uri);
//        if (!isValid) {
//            Logger.error("Invalid url " + uri);
//            return null;
//        }
        // todo : url must match the one being sent in partner.get uri

        RemoteContact remoteContact = new RemoteContact();
        return remoteContact.handleRemoteAddRequest(partner);
    }

    protected Results<RegistryPartner> getWebPartners() {
        List<RemotePartner> partners = DAOFactory.getRemotePartnerDAO().getRegistryPartners();
        if (partners == null)
            return null;

        Results<RegistryPartner> results = new Results<>();
        for (RemotePartner remotePartner : partners) {
            results.getData().add(remotePartner.toDataTransferObject());
        }

        return results;
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
