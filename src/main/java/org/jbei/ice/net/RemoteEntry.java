package org.jbei.ice.net;

import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.comment.UserComment;
import org.jbei.ice.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.dto.sample.PartSample;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.utils.Utils;

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
