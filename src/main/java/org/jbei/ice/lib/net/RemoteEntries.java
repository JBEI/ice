package org.jbei.ice.lib.net;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
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
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.RemoteAccessModel;
import org.jbei.ice.storage.model.RemotePartner;

import java.io.File;
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
    private final RemoteContact remoteContact;

    public RemoteEntries() {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.remoteContact = new RemoteContact();
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

    public WebEntries getPublicEntries(long remoteId, int offset, int limit, String sort, boolean asc) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        FolderDetails details;
        try {
            final String restPath = "rest/folders/public/entries";
            HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("offset", offset);
            queryParams.put("limit", limit);
            queryParams.put("asc", asc);
            queryParams.put("sort", sort);
            details = this.remoteContact.getFolderEntries(partner.getUrl(), restPath, queryParams, partner.getApiKey());
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

        return this.remoteContact.getAttachmentList(partner.getUrl(), entryId, partner.getApiKey());
    }

    public PartData getEntryDetails(String userId, long folderId, long partId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Folder folder = DAOFactory.getFolderDAO().get(folderId);

        RemoteAccessModel remoteAccessModel = DAOFactory.getRemoteAccessModelDAO().getByFolder(account, folder);
        if (remoteAccessModel == null) {
            Logger.error("Could not retrieve remote access for folder " + folder.getId());
            return null;
        }

        RemotePartner remotePartner = remoteAccessModel.getRemoteClientModel().getRemotePartner();
        String url = remotePartner.getUrl();
        String token = remoteAccessModel.getToken();
        long remoteFolderId = Long.decode(remoteAccessModel.getIdentifier());
        return remoteContact.getRemoteEntry(url, userId, partId, remoteFolderId, token, remotePartner.getApiKey());
    }

    // contact the remote partner to get the tool tip
    public PartData retrieveRemoteToolTip(String userId, long folderId, long partId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Folder folder = DAOFactory.getFolderDAO().get(folderId);

        RemoteAccessModel remoteAccessModel = DAOFactory.getRemoteAccessModelDAO().getByFolder(account, folder);
        if (remoteAccessModel == null) {
            Logger.error("Could not retrieve remote access for folder " + folder.getId());
            return null;
        }

        RemotePartner remotePartner = remoteAccessModel.getRemoteClientModel().getRemotePartner();
        String url = remotePartner.getUrl();
        String token = remoteAccessModel.getToken();
        return remoteContact.getToolTipDetails(url, userId, partId, token, remotePartner.getApiKey());
    }

    public FeaturedDNASequence getSequence(String userId, long folderId, String entryId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Folder folder = DAOFactory.getFolderDAO().get(folderId);

        RemoteAccessModel remoteAccessModel = DAOFactory.getRemoteAccessModelDAO().getByFolder(account, folder);
        if (remoteAccessModel == null) {
            Logger.error("Could not retrieve remote access for folder " + folder.getId());
            return null;
        }

        RemotePartner remotePartner = remoteAccessModel.getRemoteClientModel().getRemotePartner();
        String token = remoteAccessModel.getToken();
        long remoteFolderId = Long.decode(remoteAccessModel.getIdentifier());
        return remoteContact.getSequence(remotePartner.getUrl(), userId, entryId, remoteFolderId, token, remotePartner.getApiKey());
    }

    /**
     * Schedules a task to handle the transfer
     *
     * @param userId    identifier of user making request
     * @param remoteId  local unique identifier for partner to transfer to
     * @param selection context for generating entries to transfer or list of entries
     * @throws PermissionException if user making request is not an administrator
     */
    public void transferEntries(String userId, long remoteId, EntrySelection selection) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            throw new PermissionException("Administrative privileges required to transfer entries");
        TransferTask task = new TransferTask(userId, remoteId, selection);
        IceExecutorService.getInstance().runTask(task);
    }

    public PartData getPublicEntry(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        return remoteContact.getPublicEntry(partner.getUrl(), entryId, partner.getApiKey());
    }

    public PartData getPublicEntryTooltip(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        return remoteContact.getPublicTooltipDetails(partner.getUrl(), entryId, partner.getApiKey());
    }

    public PartStatistics getPublicEntryStatistics(String userId, long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        return remoteContact.getPublicEntryStatistics(partner.getUrl(), entryId, partner.getApiKey());
    }

    public FeaturedDNASequence getPublicEntrySequence(long remoteId, long entryId) {
        if (!hasRemoteAccessEnabled())
            return null;

        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
            return null;

        return remoteContact.getPublicEntrySequence(partner.getUrl(), entryId, partner.getApiKey());
    }

    public File getPublicAttachment(String userId, long remoteId, String fileId) {
        if (!hasRemoteAccessEnabled())
            return null;

        String path = "/rest/file/attachment/" + fileId; // todo
        return null;
    }
}
