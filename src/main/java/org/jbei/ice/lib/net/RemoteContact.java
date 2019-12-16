package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.AccessTokens;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Remote communications with other ice instances
 * Any method calls to this class will likely be slow since
 * it requires remote access
 *
 * @author Hector Plahar
 */
public class RemoteContact {

    private final RemotePartnerDAO dao;
    private final TokenHash tokenHash;

    public RemoteContact() {
        dao = DAOFactory.getRemotePartnerDAO();
        tokenHash = new TokenHash();
    }

    // exchange api key with remote partner
    // send to remote in order to trigger an api exchange. Note that the remote partner will
    public RegistryPartner contactPotentialPartner(RegistryPartner thisPartner, String remotePartnerUrl) {
        AccessTokens.setToken(remotePartnerUrl, thisPartner.getApiKey());
        IceRestClient client = new IceRestClient(remotePartnerUrl, "/rest/partners");
        RegistryPartner newPartner = client.post(thisPartner, RegistryPartner.class);
        AccessTokens.removeToken(remotePartnerUrl);
        return newPartner;
    }

    public RegistryPartner refreshPartnerKey(RegistryPartner partner, String url, String worToken) {
        IceRestClient client = new IceRestClient(url, worToken, "/rest/partners");
        return client.put(partner, RegistryPartner.class);
    }

    /**
     * Contacts the registry partner at the specified url, to ensure that the API key validates.
     *
     * @param myURL           the url of this ICE instance
     * @param registryPartner partner infor
     * @return true if the api key validated successfully with the specified url, false otherwise (including when
     * the instance cannot be contacted)
     */
    public boolean apiKeyValidates(String myURL, RegistryPartner registryPartner) {
        if (StringUtils.isEmpty(registryPartner.getApiKey()))
            return false;

        IceRestClient client = new IceRestClient(registryPartner.getUrl(), registryPartner.getApiKey(), "/rest/accesstokens/web");
        client.queryParam("url", myURL);
        RegistryPartner response = client.get(RegistryPartner.class);
        if (response == null) { // todo : should retry up to a certain number of times
            Logger.error("Could not validate request");
            return false;
        }
        return true;
    }

    public boolean handleRemoteRemoveRequest(String worToken, String url) {
        if (StringUtils.isEmpty(worToken) || StringUtils.isEmpty(url))
            return false;

        RemotePartner partner = dao.getByUrl(url);
        if (partner == null)
            return false;

        if (!partner.getAuthenticationToken().equals(tokenHash.encrypt(worToken, partner.getSalt()))) {
            Logger.error("Attempt to remove remote partner " + url + " with invalid worToken " + worToken);
            return false;
        }

        Logger.info("Deleting partner '" + url + "' at their request");
        dao.delete(partner); // todo : contact other instances (if this is a master node)
        return true;
    }

    PartData transferPart(String url, PartData data) {
        IceRestClient client = new IceRestClient(url, "/rest/parts/transfer");
        return client.put(data, PartData.class);
    }

    void transferSequence(String url, String recordId, EntryType entryType, String sequenceString) {
        IceRestClient client = new IceRestClient(url, "/rest/parts/transfer");
        client.postSequenceFile(recordId, entryType, sequenceString);
    }

    FolderDetails transferFolder(String url, FolderDetails folderDetails) {
        IceRestClient client = new IceRestClient(url, "/rest/folders");
        client.queryParam("isTransfer", true);
        return client.post(folderDetails, FolderDetails.class);
    }

    void addTransferredEntriesToFolder(String url, EntrySelection entrySelection) {
        IceRestClient client = new IceRestClient(url, "/rest/folders/entries");
        client.put(entrySelection, FolderDetails.class);
    }

    @SuppressWarnings("unchecked")
    List<RegistryPartner> getPartners(String url, String token) {
        IceRestClient client = new IceRestClient(url, token, "/rest/partners");
        return client.get(ArrayList.class);
    }

    public AccountTransfer getUser(String url, String email, String token) {
        IceRestClient client = new IceRestClient(url, token, "/rest/users/" + email);
        return client.get(AccountTransfer.class);
    }

    public AccessPermission shareFolder(String url, AccessPermission permission, String token) {
        IceRestClient client = new IceRestClient(url, token, "rest/permissions/remote");
        return client.post(permission, AccessPermission.class);
    }

    public FolderDetails getRemoteContents(String url, String userId, long folderId, String token,
                                           PageParameters pageParameters, String worToken) {
        IceRestClient client = new IceRestClient(url, worToken, "rest/folders/" + folderId + "/entries");
        try {
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            client.queryParam("token", encodedToken);
            client.queryParam("userId", userId);
            client.queryParam("sort", pageParameters.getSortField().name());
            client.queryParam("asc", Boolean.toString(pageParameters.isAscending()));
            client.queryParam("offset", pageParameters.getOffset());
            client.queryParam("limit", pageParameters.getLimit());
            return client.get(FolderDetails.class);
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
    }

    FolderDetails getFolderEntries(String url, Map<String, Object> queryParams, String apiKey) {
        IceRestClient client = new IceRestClient(url, apiKey, "rest/folders/public/entries");
        for (Map.Entry<String, Object> entry : queryParams.entrySet())
            client.queryParam(entry.getKey(), entry.getValue());
        client.queryParam("fields", "hasSequence", "status", "creationTime");
        return client.get(FolderDetails.class);
    }

    @SuppressWarnings("unchecked")
    List<AttachmentInfo> getAttachmentList(String url, long entryId, String apiKey) {
        String path = "rest/parts/" + entryId + "/attachments";
        IceRestClient client = new IceRestClient(url, apiKey, path);
        return client.get(ArrayList.class);
    }

    public void addTransferredEntriesToFolder(String url, String userId, EntrySelection entrySelection, long folderId,
                                              String token, String worToken) {
        try {
            IceRestClient client = new IceRestClient(url, worToken, "rest/folders/entries");
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            client.queryParam("token", encodedToken);
            client.queryParam("userId", userId);
            client.queryParam("folderId", folderId);
            client.put(entrySelection, FolderDetails.class);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    PartData getToolTipDetails(String url, String userId, long partId, String token, String worToken) {
        try {
            IceRestClient client = new IceRestClient(url, worToken, "rest/parts/" + partId + "/tooltip");
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            client.queryParam("token", encodedToken);
            client.queryParam("userId", userId);
            return client.get(PartData.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    PartData getPublicTooltipDetails(String url, long partId, String apiKey) {
        String path = "rest/parts/" + partId + "/tooltip";
        IceRestClient client = new IceRestClient(url, apiKey, path);
        return client.get(PartData.class);
    }

    PartStatistics getPublicEntryStatistics(String url, long partId, String apiKey) {
        String path = "/rest/parts/" + partId + "/statistics";
        IceRestClient client = new IceRestClient(url, apiKey, path);
        return client.get(PartStatistics.class);
    }

    public FeaturedDNASequence getPublicEntrySequence(String url, String partId, String apiKey) {
        try {
            String path = "/rest/parts/" + partId + "/sequence";
            IceRestClient client = new IceRestClient(url, apiKey, path);
            return client.get(FeaturedDNASequence.class);
        } catch (Exception e) {
            // this is fine since it could be searching multiple instances
            return null;
        }
    }

    public FeaturedDNASequence getSequence(String url, String userId, String partId, long folderId, String token,
                                           String apiKey) {
        try {
            String path = "rest/parts/" + partId + "/sequence";
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            IceRestClient client = new IceRestClient(url, apiKey, path);
            client.queryParam("token", encodedToken);
            client.queryParam("userId", userId);
            client.queryParam("folderId", folderId);
            return client.get(FeaturedDNASequence.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    PartData getRemoteEntry(String url, String userId, long partId, long folderId, String token, String worToken) {
        try {
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            IceRestClient client = new IceRestClient(url, worToken, "rest/parts/" + partId);
            client.queryParam("token", encodedToken);
            client.queryParam("userId", userId);
            client.queryParam("folderId", folderId);
            return client.get(PartData.class);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public PartData getPublicEntry(String url, String entryId, String apiKey) {
        try {
            IceRestClient client = new IceRestClient(url, apiKey, "rest/parts/" + entryId);
            return client.get(PartData.class);
        } catch (Exception e) {
            // this is fine since it could be searching all instances
            return null;
        }
    }

    /**
     * Deletes this instance of ICE from the web of registries master list
     *
     * @return true, if the master reports correct execution of the request. false otherwise
     */
    boolean deleteInstanceFromMaster(String thisUrl) {
        final String NODE_MASTER = Utils.getConfigValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER);
        RemotePartner masterPartner = DAOFactory.getRemotePartnerDAO().getByUrl(NODE_MASTER);
        if (masterPartner == null)
            return false;

        IceRestClient client = new IceRestClient(masterPartner.getUrl(), masterPartner.getApiKey(), "rest/partners/" + thisUrl);
        return client.delete();
    }
}
