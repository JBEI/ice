package org.jbei.ice.lib.net;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.List;

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
     * enable the web of registries functionality
     */
    public boolean isWebEnabled() {
        String value = new ConfigurationController().getPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    public WebOfRegistries getRegistryPartners(boolean approvedOnly) {
        WebOfRegistries webOfRegistries = new WebOfRegistries();
        webOfRegistries.setWebEnabled(isWebEnabled());

        // retrieve actual partners
        List<RemotePartner> partners = dao.getRegistryPartners();

        ArrayList<RegistryPartner> registryPartners = new ArrayList<>();
        for (RemotePartner partner : partners) {
            if (approvedOnly && partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;
            registryPartners.add(partner.toDataTransferObject());
        }

        webOfRegistries.setPartners(registryPartners);
        return webOfRegistries;
    }

    /**
     * Removes the web partner uniquely identified by the url
     *
     * @param partnerUrl url identifier for partner
     */
    public boolean removeWebPartner(String userId, String partnerUrl) {
        if (!new AccountController().isAdministrator(userId))
            return false;

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

        if (partner.getStatus() == existing.getPartnerStatus())
            return true;

        // contact remote with new api key that allows them to contact this instance
        String apiKey = Utils.generateToken();
        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);

        RegistryPartner thisPartner = new RegistryPartner();
        thisPartner.setUrl(myURL);
        thisPartner.setName(myName);
        thisPartner.setApiKey(apiKey);  // key to use in contacting this instance

        IceRestClient client = IceRestClient.getInstance();
        try {
            client.post(partner.getUrl(), "/rest/web/partner/remote", thisPartner, RegistryPartner.class, null);
            existing.setPartnerStatus(partner.getStatus());
            existing.setAuthenticationToken(apiKey);
            dao.update(existing);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    /**
     * Runs the web of registries task for contacting the appropriate partners to enable or disable
     * web of registries functionality
     *
     * @param enable if true, enables WoR; disables it otherwise
     * @param url    the url for this ice instance
     */
    public void setEnable(String userId, boolean enable, String url) {
        String thisUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (!thisUrl.equalsIgnoreCase(url)) {
            Logger.info("Auto updating uri to " + url);
            ConfigurationController configurationController = new ConfigurationController();
            configurationController.setPropertyValue(ConfigurationKey.URI_PREFIX, url);
        }

        WebOfRegistriesTask contactTask = new WebOfRegistriesTask(userId, url, enable);
        IceExecutorService.getInstance().runTask(contactTask);
    }

    public RegistryPartner getWebPartner(String userId, long partnerId) {
        return dao.get(partnerId).toDataTransferObject();
    }
}
