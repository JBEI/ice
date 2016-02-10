package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.permission.RemoteAccessPermission;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePermissionDAO;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.storage.model.RemotePermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Controller for access remote registries. This registries must be in a web of registries configuration
 * with them since it requires an api key for communication.
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class RemoteAccessController {

    private final RemotePermissionDAO dao;
    private final WoRController webController;
    private final RemotePartnerDAO remotePartnerDAO;
    private final IceRestClient iceRestClient;

    public RemoteAccessController() {
        this.dao = DAOFactory.getRemotePermissionDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        webController = new WoRController();
        iceRestClient = IceRestClient.getInstance();
    }

    /**
     * Retrieves all folders with status "PUBLIC" on the registry partner with id specified in parameter
     *
     * @param partnerId unique (local) identifier for remote partner
     * @return list of folders returned by the partner that are marked with status "PUBLIC",
     *         null on exception
     */
    public List<FolderDetails> getAvailableFolders(long partnerId) {
        RemotePartner partner = this.remotePartnerDAO.get(partnerId);
        if (partner == null)
            return null;

        try {
            String restPath = "rest/folders/public";
            return iceRestClient.get(partner.getUrl(), restPath, ArrayList.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public Setting getMasterVersion() {
        String value = new ConfigurationController().getPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        if (StringUtils.isEmpty(value))
            return new Setting("version", ConfigurationKey.APPLICATION_VERSION.getDefaultValue());

        // retrieve version
        return iceRestClient.get(value, "rest/config/version", Setting.class);
    }

    public void addPermission(String requester, RemoteAccessPermission permission) {
        WebOfRegistries registries = webController.getRegistryPartners(true);

        // search for partners with user
        for (RegistryPartner partner : registries.getPartners()) {
            String url = partner.getUrl();

            try {
                Object result = iceRestClient.get(url, "rest/users/" + permission.getUserId(), AccountTransfer.class);
                if (result == null)
                    continue;

                // user found, create new permission user secret sauce
                String privateSecret = Utils.encryptSha256(UUID.randomUUID().toString());
                String secretSource = Utils.encryptSha256(permission.getUserId() + privateSecret);
                if (StringUtils.isEmpty(secretSource))
                    continue;

                // local record
                RemotePermission remotePermission = new RemotePermission();
                RemotePartner remotePartner = remotePartnerDAO.getByUrl(url);
                remotePermission.setSecret(privateSecret);
                dao.create(remotePermission);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    public AccountTransfer getRemoteUser(long remoteId, String email) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        AccountTransfer result = iceRestClient.get(partner.getUrl(), "rest/users/" + email, AccountTransfer.class);
        if (result == null)
            return null;

        return result;
    }

    public FolderDetails getPublicFolderEntries(long remoteId, long folderId, String sort, boolean asc, int offset,
            int limit) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        try {
            String restPath = "rest/folders/" + folderId + "/entries";
            HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("offset", offset);
            queryParams.put("limit", limit);
            queryParams.put("asc", asc);
            queryParams.put("sort", sort);
            FolderDetails result = iceRestClient.get(partner.getUrl(), restPath, FolderDetails.class, queryParams);
            if (result == null)
                return null;

            return result;
        } catch (Exception e) {
            Logger.error("Error getting public folder entries from \"" + partner.getUrl() + "\": " + e.getMessage());
            return null;
        }
    }

    public List<PartSample> getRemotePartSamples(long remoteId, long partId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        String restPath = "rest/parts/" + partId + "/samples";
        return iceRestClient.get(partner.getUrl(), restPath, ArrayList.class);
    }

    public List<UserComment> getRemotePartComments(long remoteId, long partId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        String restPath = "rest/parts/" + partId + "/comments";
        return iceRestClient.get(partner.getUrl(), restPath, ArrayList.class);
    }

    public FeaturedDNASequence getRemoteSequence(long remoteId, long partId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        try {
            String restPath = "rest/parts/" + partId + "/sequence";
            Object result = iceRestClient.get(partner.getUrl(), restPath, FeaturedDNASequence.class);
            if (result == null)
                return null;

            return (FeaturedDNASequence) result;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    public List<TraceSequenceAnalysis> getRemoteTraces(long remoteId, long partId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        try {
            String restPath = "/rest/parts/" + partId + "/traces";
            ArrayList<TraceSequenceAnalysis> result = iceRestClient.get(partner.getUrl(), restPath, ArrayList.class);
            if (result == null)
                return null;

            return result;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }
}
