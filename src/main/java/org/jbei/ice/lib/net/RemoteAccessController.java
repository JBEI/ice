package org.jbei.ice.lib.net;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.jbei.ice.lib.access.RemotePermission;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dao.hibernate.RemotePermissionDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderWrapper;
import org.jbei.ice.lib.dto.permission.RemoteAccessPermission;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.executor.TransferTask;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.services.rest.RestClient;

import org.apache.commons.lang.StringUtils;

/**
 * Controller for access remote registries. This registries must be in a web of registries configuration
 * with them since it requires an api key for communication.
 *
 * @author Hector Plahar
 */
public class RemoteAccessController {

    private final RemotePermissionDAO dao;
    private final WoRController webController;
    private final RemotePartnerDAO remotePartnerDAO;
    private final RestClient restClient;

    public RemoteAccessController() {
        this.dao = DAOFactory.getRemotePermissionDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        webController = new WoRController();
        restClient = RestClient.getInstance();
    }

    /**
     * Retrieves all folders with status "PUBLIC" on the registry partner with id specified in parameter
     *
     * @param partnerId unique (local) identifier for remote partner
     * @return list of folders returned by the partner that are marked with status "PUBLIC",
     *         null on exception
     */
    public ArrayList<FolderDetails> getAvailableFolders(long partnerId) {
        RemotePartner partner = this.remotePartnerDAO.get(partnerId);
        if (partner == null)
            return null;

        try {
            String restPath = "/rest/folders/public";
            FolderWrapper detail = (FolderWrapper) restClient.get(partner.getUrl(), restPath, FolderWrapper.class);
            return detail.getFolders();
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
        return (Setting) restClient.get(value, "/rest/config/version");
    }

    public PartData getPublicEntry(long remoteId, long entryId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        return (PartData) restClient.get(partner.getUrl(), "/rest/parts/" + entryId, PartData.class);
    }

    public PartData getPublicEntryTooltip(long remoteId, long entryId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/tooltip";
        return (PartData) restClient.get(partner.getUrl(), path, PartData.class);
    }

    public PartStatistics getPublicEntryStatistics(long remoteId, long entryId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/statistics";
        return (PartStatistics) restClient.get(partner.getUrl(), path, PartStatistics.class);
    }

    public FeaturedDNASequence getPublicEntrySequence(long remoteId, long entryId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/sequence";
        return (FeaturedDNASequence) restClient.get(partner.getUrl(), path, FeaturedDNASequence.class);
    }

    public ArrayList<AttachmentInfo> getPublicEntryAttachments(long remoteId, long entryId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/attachments";
        ArrayList<AttachmentInfo> resp = (ArrayList) restClient.get(partner.getUrl(), path, ArrayList.class);
        return resp;
    }

    public File getPublicAttachment(long remoteId, String fileId) {
        String path = "/rest/file/attachment/" + fileId; // todo
        return null;
    }

    public WebEntries getPublicEntries(long remoteId, int offset, int limit, String sort, boolean asc) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        FolderDetails details;
        try {
            String restPath = "/rest/folders/public/entries";
            HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("offset", offset);
            queryParams.put("limit", limit);
            queryParams.put("asc", asc);
            queryParams.put("sort", sort);
            details = (FolderDetails) restClient.get(partner.getUrl(), restPath, FolderDetails.class, queryParams);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }

        if (details == null)
            return null;

        WebEntries entries = new WebEntries();
        entries.setRegistryPartner(partner.toDataTransferObject());
        entries.setCount(details.getCount());
        entries.setEntries(details.getEntries());
        return entries;
    }

    public void addPermission(String requester, RemoteAccessPermission permission) {
        WebOfRegistries registries = webController.getRegistryPartners(true);

        // search for partners with user
        for (RegistryPartner partner : registries.getPartners()) {
            String url = partner.getUrl();

            try {
                Object result = restClient.get(url, "/rest/users/" + permission.getUserId(), AccountTransfer.class);
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
                remotePermission.setRemotePartner(remotePartner);
                remotePermission.setUserId(permission.getUserId());
                remotePermission.setSecret(privateSecret);
                remotePermission.setAccessType(permission.getAccessType());
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

        Object result = restClient.get(partner.getUrl(), "/rest/users/" + email, AccountTransfer.class);
        if (result == null)
            return null;

        return (AccountTransfer) result;
    }

    public FolderDetails getPublicFolderEntries(long remoteId, long folderId, String sort, boolean asc, int offset,
            int limit) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        try {
            String restPath = "/rest/folders/" + folderId + "/entries";
            HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("offset", offset);
            queryParams.put("limit", limit);
            queryParams.put("asc", asc);
            queryParams.put("sort", sort);
            Object result = restClient.get(partner.getUrl(), restPath, FolderDetails.class, queryParams);
            if (result == null)
                return null;

            return (FolderDetails) result;
        } catch (Exception e) {
            Logger.error("Error getting public folder entries from \"" + partner.getUrl() + "\": " + e.getMessage());
            return null;
        }
    }

    public void transferEntries(String userId, long remoteId, ArrayList<Long> data) {
        TransferTask task = new TransferTask(userId, remoteId, data);
        IceExecutorService.getInstance().runTask(task);
    }

    public FeaturedDNASequence getRemoteSequence(long remoteId, long partId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        try {
            String restPath = "/rest/parts/" + partId + "/sequence";
            Object result = restClient.get(partner.getUrl(), restPath, FeaturedDNASequence.class);
            if (result == null)
                return null;

            return (FeaturedDNASequence) result;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }
}
