package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.AccessTokens;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
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
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remote communications with other ice instances
 * Any method calls to this class will likely be slow since
 * it requires remote access
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class RemoteContact {

    private final RemotePartnerDAO dao;
    private final TokenHash tokenHash;
    private final IceRestClient restClient;

    public RemoteContact() {
        dao = DAOFactory.getRemotePartnerDAO();
        tokenHash = new TokenHash();
        restClient = IceRestClient.getInstance();
    }

    // exchange api key with remote partner
    // send to remote in order to trigger an api exchange. Note that the remote partner will
    public RegistryPartner contactPotentialPartner(RegistryPartner thisPartner, String remotePartnerUrl) {
        AccessTokens.setToken(remotePartnerUrl, thisPartner.getApiKey());
        RegistryPartner newPartner = restClient.post(remotePartnerUrl, "/rest/partners",
                thisPartner, RegistryPartner.class, null);
        AccessTokens.removeToken(remotePartnerUrl);
        return newPartner;
    }

    public RegistryPartner refreshPartnerKey(RegistryPartner partner, String url, String worToken) {
        return restClient.putWor(url, "rest/partners", partner, RegistryPartner.class, null, worToken);
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

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("url", myURL);
        RegistryPartner response = restClient.getWor(registryPartner.getUrl(), "/rest/accesstokens/web",
                RegistryPartner.class, queryParams, registryPartner.getApiKey());
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

    public PartData transferPart(String url, PartData data) {
        return restClient.put(url, "/rest/parts/transfer", data, PartData.class);
    }

    public void transferSequence(String url, String recordId, EntryType entryType, String sequenceString) {
        restClient.postSequenceFile(url, recordId, entryType, sequenceString);
    }

    public FolderDetails transferFolder(String url, FolderDetails folderDetails) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("isTransfer", true);
        return restClient.post(url, "/rest/folders", folderDetails, FolderDetails.class, queryParams);
    }

    public void addTransferredEntriesToFolder(String url, EntrySelection entrySelection) {
        restClient.put(url, "/rest/folders/entries", entrySelection, FolderDetails.class);
    }

    public List<RegistryPartner> getPartners(String url, String token) {
        return restClient.getWor(url, "/rest/partners", ArrayList.class, null, token);
    }

    public AccountTransfer getUser(String url, String email, String token) {
        return restClient.getWor(url, "rest/users/" + email, AccountTransfer.class, null, token);
    }

    public AccessPermission shareFolder(String url, AccessPermission permission, String token) {
        return restClient.postWor(url, "rest/permissions/remote", permission, AccessPermission.class, null, token);
    }

    public FolderDetails getRemoteContents(String url, String userId, long folderId, String token, PageParameters pageParameters,
                                           String worToken) {
        Map<String, Object> queryParams = new HashMap<>();
        try {
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            queryParams.put("token", encodedToken);
            queryParams.put("userId", userId);
            queryParams.put("sort", pageParameters.getSortField().name());
            queryParams.put("asc", Boolean.toString(pageParameters.isAscending()));
            queryParams.put("offset", pageParameters.getOffset());
            queryParams.put("limit", pageParameters.getLimit());
            return restClient.getWor(url, "rest/folders/" + folderId + "/entries", FolderDetails.class, queryParams, worToken);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public FolderDetails getFolderEntries(String url, String resourcePath, Map<String, Object> queryParams, String apiKey) {
        return restClient.getWor(url, resourcePath, FolderDetails.class, queryParams, apiKey);
    }

    public List<AttachmentInfo> getAttachmentList(String url, long entryId, String apiKey) {
        String path = "rest/parts/" + entryId + "/attachments";
        return restClient.getWor(url, path, ArrayList.class, null, apiKey);
    }

    public void addTransferredEntriesToFolder(String url, String userId, EntrySelection entrySelection, long folderId,
                                              String token, String worToken) {
        try {
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("token", encodedToken);
            queryParams.put("userId", userId);
            queryParams.put("folderId", folderId);
            restClient.putWor(url, "rest/folders/entries", entrySelection, FolderDetails.class, queryParams, worToken);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public PartData getToolTipDetails(String url, String userId, long partId, String token, String worToken) {
        try {
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("token", encodedToken);
            queryParams.put("userId", userId);
            return restClient.getWor(url, "rest/parts/" + partId + "/tooltip", PartData.class, queryParams, worToken);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public PartData getPublicTooltipDetails(String url, long partId, String apiKey) {
        String path = "rest/parts/" + partId + "/tooltip";
        return restClient.getWor(url, path, PartData.class, null, apiKey);
    }

    public PartStatistics getPublicEntryStatistics(String url, long partId, String apiKey) {
        String path = "/rest/parts/" + partId + "/statistics";
        return restClient.getWor(url, path, PartStatistics.class, null, apiKey);
    }

    public FeaturedDNASequence getPublicEntrySequence(String url, long partId, String apiKey) {
        String path = "/rest/parts/" + partId + "/sequence";
        return restClient.getWor(url, path, FeaturedDNASequence.class, null, apiKey);
    }

    public FeaturedDNASequence getSequence(String url, String userId, String partId, long folderId, String token,
                                           String apiKey) {
        try {
            String path = "rest/parts/" + partId + "/sequence";
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("token", encodedToken);
            queryParams.put("userId", userId);
            queryParams.put("folderId", folderId);
            return restClient.getWor(url, path, FeaturedDNASequence.class, queryParams, apiKey);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public PartData getRemoteEntry(String url, String userId, long partId, long folderId, String token, String worToken) {
        try {
            String encodedToken = URLEncoder.encode(token, "UTF-8");
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("token", encodedToken);
            queryParams.put("userId", userId);
            queryParams.put("folderId", folderId);
            return restClient.getWor(url, "rest/parts/" + partId, PartData.class, queryParams, worToken);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public PartData getPublicEntry(String url, long entryId, String apiKey) {
        return restClient.getWor(url, "rest/parts/" + entryId, PartData.class, null, apiKey);
    }

    /**
     * Deletes this instance of ICE from the web of registries master list
     *
     * @return true, if the master reports correct execution of the request. false otherwise
     */
    public boolean deleteInstanceFromMaster(String url, String apiKey, String thisUrl) {
        return restClient.delete(apiKey, url, "rest/partners/" + thisUrl);
    }
}
