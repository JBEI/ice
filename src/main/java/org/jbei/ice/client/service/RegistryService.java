package org.jbei.ice.client.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.ExportAsOption;
import org.jbei.ice.lib.shared.dto.AccountResults;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.NewsItem;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.message.MessageList;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;
import org.jbei.ice.lib.shared.dto.search.IndexType;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResults;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;
import org.jbei.ice.lib.shared.dto.user.User;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;

/**
 * The client side stub for the RPC service.
 *
 * @author Hector Plahar
 */
@RemoteServiceRelativePath("ice")
public interface RegistryService extends RemoteService {

    User login(String name, String pass) throws AuthenticationException;

    User sessionValid(String sid);

    boolean logout(String sessionId);

    ArrayList<FolderDetails> retrieveCollections(String sessionId) throws AuthenticationException;

    FolderDetails retrieveEntriesForFolder(String sessionId, long folderId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException;

    FolderDetails retrieveUserEntries(String sid, String userId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException;

    PartData retrieveEntryDetails(String sid, long id, String url) throws AuthenticationException;

    FolderDetails createUserCollection(String sid, String name, String description,
            ArrayList<Long> contents) throws AuthenticationException;

    ArrayList<FolderDetails> moveToUserCollection(String sid, long source, ArrayList<Long> destination,
            ArrayList<Long> entryIds) throws AuthenticationException;

    User retrieveProfileInfo(String sid, String userId) throws AuthenticationException;

    Long createEntry(String sid, PartData info) throws AuthenticationException;

    ArrayList<FolderDetails> addEntriesToCollection(String sid, ArrayList<Long> destination,
            ArrayList<Long> entryIds) throws AuthenticationException;

    HashMap<PartSample, ArrayList<String>> retrieveStorageSchemes(String sessionId, EntryType type)
            throws AuthenticationException;

    ArrayList<NewsItem> retrieveNewsItems(String sessionId) throws AuthenticationException;

    NewsItem createNewsItem(String sessionId, NewsItem item) throws AuthenticationException;

    FolderDetails updateFolder(String sid, long folderId, FolderDetails update) throws AuthenticationException;

    FolderDetails removeFromUserCollection(String sessionId, long source, ArrayList<Long> ids)
            throws AuthenticationException;

    Long updateEntry(String sid, PartData info) throws AuthenticationException;

    ArrayList<BulkUploadInfo> retrieveUserSavedDrafts(String sid) throws AuthenticationException;

    BulkUploadInfo retrieveBulkImport(String sid, long id, int start, int limit) throws AuthenticationException;

    FolderDetails deleteFolder(String sessionId, long folderId) throws AuthenticationException;

    SampleStorage createSample(String sessionId, SampleStorage sampleStorage, long entryId)
            throws AuthenticationException;

    SuggestOracle.Response getPermissionSuggestions(String sessionId, Request req) throws AuthenticationException;

    SuggestOracle.Response getAutoCompleteSuggestion(AutoCompleteField field, Request request);

    boolean addPermission(String sessionId, AccessPermission accessPermission) throws AuthenticationException;

    boolean removePermission(String sessionId, AccessPermission accessPermission) throws AuthenticationException;

    PartData saveSequence(String sessionId, PartData data, String sequenceUser, boolean isFile)
            throws AuthenticationException;

    String getConfigurationSetting(String name);

    User retrieveAccount(String email);

    String createNewAccount(User info, boolean sendEmail);

    User updateAccount(String sid, String email, User info) throws AuthenticationException;

    boolean updateAccountPassword(String sid, String email, String password) throws AuthenticationException;

    boolean handleForgotPassword(String email, String url) throws AuthenticationException;

    AccountResults retrieveAllUserAccounts(String sid, int start, int limit) throws AuthenticationException;

    boolean removeSequence(String sid, long entryId) throws AuthenticationException;

    ArrayList<SequenceAnalysisInfo> retrieveEntryTraceSequences(String sid, long entryId)
            throws AuthenticationException;

    ArrayList<SequenceAnalysisInfo> deleteEntryTraceSequences(String sid, long entryId, ArrayList<String> seqId)
            throws AuthenticationException;

    ArrayList<BulkUploadInfo> retrieveDraftsPendingVerification(String sid) throws AuthenticationException;

    ArrayList<UserGroup> retrieveGroups(String sid, GroupType type) throws AuthenticationException;

    boolean deleteEntryAttachment(String sid, String fileId) throws AuthenticationException;

    PartData retrieveEntryTipDetails(String sessionId, long id, String url) throws AuthenticationException;

    BulkUploadInfo deleteSavedDraft(String sid, long draftId) throws AuthenticationException;

    ArrayList<FolderDetails> deleteEntry(String sessionId, PartData info) throws AuthenticationException;

    boolean approvePendingBulkImport(String sessionId, long id) throws AuthenticationException;

    boolean submitBulkUploadDraft(String sid, long draftId, ArrayList<UserGroup> groups) throws AuthenticationException;

    HashMap<String, String> retrieveSystemSettings(String sid) throws AuthenticationException;

    ArrayList<User> retrieveGroupMembers(String sessionId, UserGroup user) throws AuthenticationException;

    ArrayList<User> setGroupMembers(String sessionId, UserGroup user, ArrayList<User> members)
            throws AuthenticationException;

    Boolean setConfigurationSetting(String sid, ConfigurationKey key, String value);

    UserGroup createNewGroup(String sessionId, UserGroup user) throws AuthenticationException;

    boolean revertedSubmittedBulkUpload(String sid, long uploadId) throws AuthenticationException;

    FolderDetails retrieveAllVisibleEntrys(String sid, FolderDetails details, ColumnField field, boolean asc,
            int start, int limit) throws AuthenticationException;

    ArrayList<User> retrieveAvailableAccounts(String sessionId) throws AuthenticationException;

    WebOfRegistries retrieveWebOfRegistryPartners(String sid) throws AuthenticationException;

    boolean addWebPartner(String sessionId, String partnerUrl, String partnerName);

    SearchResults performSearch(String sid, SearchQuery searchQuery, boolean isWeb) throws AuthenticationException;

    boolean setPreferenceSetting(String sid, String key, String value) throws AuthenticationException;

    HashMap<PreferenceKey, String> retrieveUserPreferences(String sid, ArrayList<PreferenceKey> keys)
            throws AuthenticationException;

    HashMap<String, String> retrieveUserSearchPreferences(String sid) throws AuthenticationException;

    boolean isWebOfRegistriesEnabled();

    MessageList retrieveMessages(String sessionId, int start, int count) throws AuthenticationException;

    int markMessageRead(String sessionId, long id) throws AuthenticationException;

    boolean setBulkUploadDraftName(String sid, long id, String draftName) throws AuthenticationException;

    UserGroup updateGroup(String sessionId, UserGroup user) throws AuthenticationException;

    UserGroup deleteGroup(String sessionId, UserGroup user) throws AuthenticationException;

    boolean removeAccountFromGroup(String sid, UserGroup user, User account) throws AuthenticationException;

    BulkUploadAutoUpdate autoUpdateBulkUpload(String sid, BulkUploadAutoUpdate wrapper, EntryAddType addType)
            throws AuthenticationException;

    Long updateBulkUploadPreference(String sid, long bulkUploadId, EntryAddType addType, PreferenceInfo info)
            throws AuthenticationException;

    void requestEntryTransfer(String sid, ArrayList<Long> ids, ArrayList<String> sites) throws AuthenticationException;

    boolean deleteSample(String sessionId, PartSample part) throws AuthenticationException;

    ArrayList<UserGroup> retrieveUserGroups(String sessionId, boolean includePublicGroup)
            throws AuthenticationException;

    boolean promoteCollection(String sessionId, long id) throws AuthenticationException;

    boolean demoteCollection(String sessionId, long id) throws AuthenticationException;

    Boolean sendMessage(String sid, MessageInfo info) throws AuthenticationException;

    Boolean rebuildSearchIndex(String sessionId, IndexType type) throws AuthenticationException;

    UserComment sendComment(String sid, UserComment comment) throws AuthenticationException;

    Integer requestSample(String sid, long entryId, SampleRequestType type) throws AuthenticationException;

    UserComment alertToEntryProblem(String sid, long entryID, String details) throws AuthenticationException;

    ArrayList<RegistryPartner> setEnableWebOfRegistries(String sessionId, boolean value) throws AuthenticationException;

    ArrayList<AccessPermission> retrieveDefaultPermissions(String sid) throws AuthenticationException;

    ArrayList<PartData> retrieveTransferredParts(String sessionId) throws AuthenticationException;

    boolean processTransferredParts(String sid, ArrayList<Long> partIds, boolean accept) throws AuthenticationException;

    boolean enablePublicReadAccess(String sid, long entryId) throws AuthenticationException;

    boolean disablePublicReadAccess(String sid, long entryId) throws AuthenticationException;

    boolean setPropagatePermissionForFolder(String sid, long folderId, boolean prop)
            throws AuthenticationException;

    String exportParts(String sid, ArrayList<Long> partIds, ExportAsOption option) throws AuthenticationException;

    boolean enableOrDisableFolderPublicAccess(String sid, long folderId, boolean isEnable)
            throws AuthenticationException;

    RegistryPartner setRegistryPartnerStatus(String sid, RegistryPartner partner) throws AuthenticationException;

    BulkUploadInfo getBulkEditData(String sid, ArrayList<Long> partIds) throws AuthenticationException;

    ArrayList<SampleRequest> getSampleRequests(String sid, SampleRequestStatus status)
            throws AuthenticationException;
}
