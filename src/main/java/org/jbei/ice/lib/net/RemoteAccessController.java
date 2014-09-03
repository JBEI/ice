package org.jbei.ice.lib.net;

import java.util.ArrayList;
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
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderWrapper;
import org.jbei.ice.lib.dto.permission.RemoteAccessPermission;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.RestClient;

import org.apache.commons.lang.StringUtils;

/**
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

    public FolderDetails getPublicEntries(long remoteId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        FolderDetails details;
        try {
            details = (FolderDetails) restClient.get(partner.getUrl(), "/rest/folders/available/entries",
                                                     FolderDetails.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
        return details;
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

    public FolderDetails getPublicFolderEntries(long remoteId, long folderId) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        Object result = restClient.get(partner.getUrl(), "/rest/folders/" + folderId + "/entries", FolderDetails.class);
        if (result == null)
            return null;

        return (FolderDetails) result;
    }
}
