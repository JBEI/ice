package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.RemotePartner;

/**
 * @author Hector Plahar
 */
public class RemoteFolder {

    private final RemotePartner partner;
    private final long folderId;

    public RemoteFolder(long partnerId, long remoteFolderId) {
        this.partner = DAOFactory.getRemotePartnerDAO().get(partnerId);
        if (this.partner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + partnerId);
        this.folderId = remoteFolderId;
    }

    public FolderDetails getEntries(String sort, boolean asc, int offset, int limit) {
        try {
            IceRestClient restClient = new IceRestClient(this.partner.getUrl(), this.partner.getApiKey());
            restClient.queryParam("offset", offset);
            restClient.queryParam("limit", limit);
            restClient.queryParam("asc", asc);
            restClient.queryParam("sort", sort);
            restClient.queryParam("fields", "creationTime", "hasSequence", "status");
            String restPath = "rest/folders/" + folderId + "/entries";
            return restClient.get(restPath, FolderDetails.class);
        } catch (Exception e) {
            Logger.error("Error getting public folder entries from \"" + partner.getUrl() + "\": " + e.getMessage());
            return null;
        }
    }
}
