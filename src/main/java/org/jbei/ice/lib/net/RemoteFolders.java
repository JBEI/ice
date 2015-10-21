package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
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

    private final RemotePartner remotePartner;
    private final RemotePartnerDAO remotePartnerDAO;

    public RemoteFolders(final long partnerId) {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.remotePartner = this.remotePartnerDAO.get(partnerId);
        if (this.remotePartner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + partnerId);
    }

    public List<FolderDetails> getFolders(FolderType folderType) {
        try {
            String restPath = "/rest/folders/public";
            return IceRestClient.getInstance().get(remotePartner.getUrl(), restPath, ArrayList.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }
}
