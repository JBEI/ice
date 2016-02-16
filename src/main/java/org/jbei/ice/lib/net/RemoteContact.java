package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.AccessTokens;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

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
    // request validation of the api key at GET /rest/accesstokens/web
    public RegistryPartner contactPotentialPartner(RegistryPartner thisPartner, String remotePartnerUrl) {
        AccessTokens.setToken(remotePartnerUrl, thisPartner.getApiKey());
        RegistryPartner newPartner = restClient.post(remotePartnerUrl, "/rest/partners",
                thisPartner, RegistryPartner.class, null);
        AccessTokens.removeToken(remotePartnerUrl);
        return newPartner;
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

        if (!partner.getAuthenticationToken().equals(tokenHash.encryptPassword(worToken, partner.getSalt()))) {
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

    public AccountTransfer getUser(String url, String email) {
        return restClient.get(url, "/rest/users/" + email, AccountTransfer.class, null);
    }

    public AccessPermission shareFolder(String url, AccessPermission permission, String token) {
        return restClient.postWor(url, "rest/permissions/remote", permission, AccessPermission.class, null, token);
    }

    public FolderDetails getRemoteContents(String url, String userId, long folderId, String token) {
        // todo : paging params
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("token", token);
        queryParams.put("userId", userId);
        return restClient.get(url, "rest/folders/" + folderId + "/entries", FolderDetails.class, queryParams);
    }
}
