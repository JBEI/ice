package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class RemoteEntry {

    private final RemotePartner partner;
    private final long remotePartId;

    public RemoteEntry(long partnerId, long remotePartId) {
        if (!hasRemoteAccessEnabled())
            throw new IllegalArgumentException("Not a member of web of registries");

        RemotePartnerDAO partnerDAO = DAOFactory.getRemotePartnerDAO();
        partner = partnerDAO.get(partnerId);
        if (partner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + partnerId);

        this.remotePartId = remotePartId;
    }

    /**
     * Checks if the web of registries admin config value has been set to enable this ICE instance
     * to join the web of registries configuration
     *
     * @return true if value has been set to the affirmative, false otherwise
     */
    private boolean hasRemoteAccessEnabled() {
        String value = Utils.getConfigValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        return ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
    }

    public List<TraceSequenceAnalysis> getTraces() {
        try {
            IceRestClient client = new IceRestClient(partner.getUrl(), this.partner.getApiKey());
            String restPath = "/rest/parts/" + remotePartId + "/traces";
            return client.get(restPath, ArrayList.class);
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    public List<PartSample> getSamples() {
        IceRestClient client = new IceRestClient(partner.getUrl(), this.partner.getApiKey());
        String restPath = "rest/parts/" + remotePartId + "/samples";
        return client.get(restPath, ArrayList.class);
    }

    public List<UserComment> getComments() {
        IceRestClient client = new IceRestClient(partner.getUrl(), this.partner.getApiKey());
        String restPath = "rest/parts/" + remotePartId + "/comments";
        return client.get(restPath, ArrayList.class);
    }
}
