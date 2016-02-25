package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.executor.TransferTask;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Entries that are on other registry instances other than this instance.
 * An account is generally required to be able to access other instances through this instances
 *
 * @author Hector Plahar
 */
public class RemoteEntries {

    private final RemotePartnerDAO remotePartnerDAO;
    private final IceRestClient iceRestClient;

    public RemoteEntries() {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.iceRestClient = IceRestClient.getInstance();
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

    public WebEntries getPublicEntries(String userId, long remoteId, int offset, int limit, String sort, boolean asc) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        FolderDetails details;
        try {
            final String restPath = "/rest/folders/public/entries";
            HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("offset", offset);
            queryParams.put("limit", limit);
            queryParams.put("asc", asc);
            queryParams.put("sort", sort);
            details = iceRestClient.get(partner.getUrl(), restPath, FolderDetails.class, queryParams);
            if (details == null)
                return null;
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }

        WebEntries entries = new WebEntries();
        entries.setRegistryPartner(partner.toDataTransferObject());
        entries.setCount(details.getCount());
        entries.setEntries(details.getEntries());
        return entries;
    }

    @SuppressWarnings("unchecked")
    public List<AttachmentInfo> getEntryAttachments(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/attachments";
        return iceRestClient.get(partner.getUrl(), path, ArrayList.class);
    }

    public FeaturedDNASequence getEntrySequence(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/sequence";
        return iceRestClient.get(partner.getUrl(), path, FeaturedDNASequence.class);
    }

    public void transferEntries(String userId, long remoteId, EntrySelection selection) {
        TransferTask task = new TransferTask(userId, remoteId, selection);
        IceExecutorService.getInstance().runTask(task);
    }

    public PartData getPublicEntry(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        return iceRestClient.get(partner.getUrl(), "/rest/parts/" + entryId, PartData.class);
    }

    public PartData getPublicEntryTooltip(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "rest/parts/" + entryId + "/tooltip";
        return iceRestClient.getWor(partner.getUrl(), path, PartData.class, null, partner.getApiKey());
    }

    public PartStatistics getPublicEntryStatistics(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/statistics";
        return iceRestClient.get(partner.getUrl(), path, PartStatistics.class);
    }

    public FeaturedDNASequence getPublicEntrySequence(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        String path = "/rest/parts/" + entryId + "/sequence";
        return iceRestClient.get(partner.getUrl(), path, FeaturedDNASequence.class);
    }

    public File getPublicAttachment(String userId, long remoteId, String fileId) {
        if (!hasRemoteAccessEnabled())
            return null;

        String path = "/rest/file/attachment/" + fileId; // todo
        return null;
    }
}
