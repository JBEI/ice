package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.List;

/**
 * Task to enable or disable web of registries by contacting a master node to obtain a (potentially curated) list
 * of other ICE instances. This task then contacts each ICE instance in turn, to exchange access tokens
 *
 * @author Hector Plahar
 */
public class WebOfRegistriesTask extends Task {

    private final boolean enable;
    private final String myUrl;
    private final String userId;
    private final RemoteContact remoteContact;

    public WebOfRegistriesTask(String userId, String thisUrl, boolean enable) {
        this.enable = enable;
        this.myUrl = thisUrl;
        this.userId = userId;
        this.remoteContact = new RemoteContact();
    }

    @Override
    public void execute() {
        if (!UrlValidator.getInstance().isValid("https://" + this.myUrl)) {
            Logger.warn("Invalid url (" + this.myUrl + "). Aborting run of web of registries task");
            return;
        }

        final String NODE_MASTER = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);
        if (NODE_MASTER.equalsIgnoreCase(this.myUrl) || StringUtils.isEmpty(NODE_MASTER)) {
            Logger.warn("Aborting contact of node master.");
            return;
        }

        if (!this.enable) {
            // delete from the node master
            RemotePartner masterPartner = DAOFactory.getRemotePartnerDAO().getByUrl(NODE_MASTER);
            if (masterPartner != null)
                this.remoteContact.deleteInstanceFromMaster(NODE_MASTER, masterPartner.getApiKey(), this.myUrl);
            return;
        }

        // exchange key information with the master registry
        WebPartners webPartners = new WebPartners();
        RegistryPartner masterPartner = new RegistryPartner();
        masterPartner.setUrl(NODE_MASTER);

        masterPartner = webPartners.addNewPartner(this.userId, masterPartner);
        if (masterPartner == null) {
            Logger.error("Could not connect to master node");
            return;
        }

        // get partners from master (this call requires that this ice instance already be a partner, so
        // the requestToJoin call above must succeed)
        List<RegistryPartner> partners = this.remoteContact.getPartners(masterPartner.getUrl(), masterPartner.getApiKey());
        if (partners == null) {
            Logger.error("Could not retrieve list of partners from master node");
            return;
        }

        Logger.info("Received " + partners.size() + " partner(s) from master");
        // for potential, check already in partner list and add if not by performing exchange
        for (RegistryPartner registryPartner : partners) {
            if (registryPartner.getUrl().equalsIgnoreCase(myUrl))
                continue;

            // perform exchange with partners
            webPartners.addNewPartner(userId, registryPartner);
        }
    }
}
