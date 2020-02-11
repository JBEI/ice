package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class RemoteFolders {

    private final RemotePartner partner;

    public RemoteFolders(long partnerId) {
        RemotePartnerDAO partnerDAO = DAOFactory.getRemotePartnerDAO();
        partner = partnerDAO.get(partnerId);
        if (partner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + partnerId);
    }

    @SuppressWarnings("unchecked")
    public List<FolderDetails> getAvailableFolders() {
        try {
            String restPath = "rest/folders/public";
            IceRestClient client = new IceRestClient(partner.getUrl(), partner.getApiKey());
            return client.get(restPath, ArrayList.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }
}
