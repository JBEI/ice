package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.HashMap;

/**
 * @author Hector Plahar
 */
public class RemoteFolder {

    private final RemotePartner partner;
    private final RemotePartnerDAO remotePartnerDAO;
    private final long folderId;
    private IceRestClient restClient;

    public RemoteFolder(long partnerId, long remoteFolderId) {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.partner = this.remotePartnerDAO.get(partnerId);
        if (this.partner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + partnerId);
        this.folderId = remoteFolderId;
        this.restClient = IceRestClient.getInstance();
    }

    public FolderDetails getEntries(String sort, boolean asc, int offset, int limit) {
        try {
            String restPath = "rest/folders/" + folderId + "/entries";
            HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("offset", offset);
            queryParams.put("limit", limit);
            queryParams.put("asc", asc);
            queryParams.put("sort", sort);
            FolderDetails result = this.restClient.getWor(partner.getUrl(), restPath, FolderDetails.class, queryParams,
                    partner.getApiKey());
            if (result == null)
                return null;

            return result;
        } catch (Exception e) {
            Logger.error("Error getting public folder entries from \"" + partner.getUrl() + "\": " + e.getMessage());
            return null;
        }
    }
}
