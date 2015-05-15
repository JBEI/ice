package org.jbei.ice.lib.net;

import org.apache.commons.lang.StringUtils;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Task to to enable or disable web of registries by contacting a master node to obtain a (curated) list
 * of other ICE instances. The task then contacts each in turn to exchange tokens
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
        final String NODE_MASTER = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);
        if (NODE_MASTER.equalsIgnoreCase(this.myUrl) || StringUtils.isEmpty(NODE_MASTER)) {
            return;
        }

        if (!this.enable) {
            RemotePartner masterPartner = DAOFactory.getRemotePartnerDAO().getByUrl(NODE_MASTER);
            requestToDisjoin(masterPartner.getApiKey(), NODE_MASTER);
            return;
        }

        RegistryPartner partner = new RegistryPartner();
        partner.setApiKey(tokenHash.generateRandomToken());
        String name = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        if (StringUtils.isEmpty(name))
            name = myUrl;
        partner.setName(name);
        partner.setUrl(myUrl);

        // todo : if master then it should return link to retrieve (HATEOAS) which this can follow
        RegistryPartner masterPartner = requestToJoin(NODE_MASTER, partner);
        if (masterPartner == null) {
            Logger.error("Could not connect to master node");
            return;
        }

        // get partners from master (this call requires that this ice instance already be a partner, so
        // the requestToJoin call above must succeed)
        List<RegistryPartner> partners = getWebOfRegistryPartners(NODE_MASTER);
        String myURL = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        String myName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        RemoteContact remoteContact = new RemoteContact();

        // for potential, check already in partner list and add if not by performing exchange
        for (RegistryPartner registryPartner : partners) {
            remoteContact.addWebPartner(myName, myURL, registryPartner);
        }
    }

    // contacts the master node for other ice instances
    protected RegistryPartner requestToJoin(String masterUrl, RegistryPartner partner) {
        IceRestClient restClient = IceRestClient.getInstance();
        return (RegistryPartner) restClient.post(masterUrl, "/rest/web/partner/remote", partner, RegistryPartner.class);
    }

    @SuppressWarnings("unchecked")
    protected List<RegistryPartner> getWebOfRegistryPartners(String url) {
        IceRestClient restClient = IceRestClient.getInstance();
        return (ArrayList) restClient.get(url, "/rest/web/partners", ArrayList.class);
    }

    /**
     * Sends a message to the master that this instance no longer wants to be a part of the web of registries
     *
     * @param masterUrl
     */
    protected void requestToDisjoin(String token, String masterUrl) {
        IceRestClient restClient = IceRestClient.getInstance();
        restClient.delete(token, masterUrl, "/rest/web/partners");
    }
}
