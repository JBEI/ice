package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.List;

/**
 * Task to enable or disable web of registries by contacting a master node to obtain a (potentially curated) list
 * of other ICE instances. This task then contacts each ICE instance in turn, to exchange access tokens
 *
 * @author Hector Plahar
 */
public class WebOfRegistriesTask extends Task {

    private final boolean enable;
    private final TokenHash tokenHash;
    private final String myUrl;

    public WebOfRegistriesTask(String thisUrl, boolean enable) {
        this.enable = enable;
        this.myUrl = thisUrl;
        this.tokenHash = new TokenHash();
    }

    @Override
    public void execute() {
        if (!UrlValidator.getInstance().isValid(this.myUrl)) {
            Logger.warn("Local instance detected. Aborting run of web of registries task");
            return;
        }

        final String NODE_MASTER = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);
        if (NODE_MASTER.equalsIgnoreCase(this.myUrl) || StringUtils.isEmpty(NODE_MASTER)) {
            Logger.warn("Aborting contact of node master.");
            return;
        }

        if (!this.enable) {
            RemotePartner masterPartner = DAOFactory.getRemotePartnerDAO().getByUrl(NODE_MASTER);
            requestToDisjoin(masterPartner.getApiKey(), NODE_MASTER);
            return;
        }

        String name = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        if (StringUtils.isEmpty(name))
            name = myUrl;

        RemoteContact remoteContact = new RemoteContact();

        // exchange key information with the master registry
//        RegistryPartner masterPartner = requestToJoin(NODE_MASTER, partner);
        RegistryPartner masterPartner = new RegistryPartner();
        masterPartner.setUrl(NODE_MASTER);
        masterPartner = remoteContact.addWebPartner(name, myUrl, masterPartner);
        if (masterPartner == null) {
            Logger.error("Could not connect to master node");
            return;
        }

        // get partners from master (this call requires that this ice instance already be a partner, so
        // the requestToJoin call above must succeed)
        List<RegistryPartner> partners = getWebOfRegistryPartners(masterPartner.getUrl(), masterPartner.getApiKey());

        // for potential, check already in partner list and add if not by performing exchange
        for (RegistryPartner registryPartner : partners) {
            if (registryPartner.getUrl().equalsIgnoreCase(myUrl))
                continue;
            remoteContact.addWebPartner(name, myUrl, registryPartner);
        }
    }

    // contacts the master node for other ice instances
    protected RegistryPartner requestToJoin(String masterUrl, RegistryPartner partner) {
        IceRestClient restClient = IceRestClient.getInstance();
        return restClient.post(masterUrl, "/rest/partners", partner, RegistryPartner.class);
    }

    @SuppressWarnings("unchecked")
    protected List<RegistryPartner> getWebOfRegistryPartners(String url, String token) {
        IceRestClient restClient = IceRestClient.getInstance();
        return restClient.getWor(url, "/rest/web/partners", ArrayList.class, null, token);
    }

    /**
     * Sends a message to the master that this instance no longer wants to be a part of the web of registries
     */
    protected void requestToDisjoin(String token, String masterUrl) {
        IceRestClient restClient = IceRestClient.getInstance();
        restClient.delete(token, masterUrl, "/rest/web/partners");
    }
}
