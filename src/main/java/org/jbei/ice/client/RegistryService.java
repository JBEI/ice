package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountResults;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.NewsItem;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;
import org.jbei.ice.shared.dto.message.MessageList;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.search.SearchQuery;
import org.jbei.ice.shared.dto.search.SearchResults;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ice")
public interface RegistryService extends RemoteService {

    AccountInfo login(String name, String pass) throws AuthenticationException;

    AccountInfo sessionValid(String sid);

    boolean logout(String sessionId);

    ArrayList<FolderDetails> retrieveCollections(String sessionId) throws AuthenticationException;

    FolderDetails retrieveEntriesForFolder(String sessionId, long folderId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException;

    FolderDetails retrieveUserEntries(String sid, String userId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException;

    EntryInfo retrieveEntryDetails(String sid, long id, String recordId, String url) throws AuthenticationException;

    FolderDetails createUserCollection(String sid, String name, String description,
            ArrayList<Long> contents) throws AuthenticationException;

    ArrayList<FolderDetails> moveToUserCollection(String sid, long source, ArrayList<Long> destination,
            ArrayList<Long> entryIds) throws AuthenticationException;

    AccountInfo retrieveProfileInfo(String sid, String userId) throws AuthenticationException;

    Long createEntry(String sid, EntryInfo info) throws AuthenticationException;

    ArrayList<FolderDetails> addEntriesToCollection(String sid, ArrayList<Long> destination,
            ArrayList<Long> entryIds) throws AuthenticationException;

    HashMap<SampleInfo, ArrayList<String>> retrieveStorageSchemes(String sessionId, EntryType type)
            throws AuthenticationException;

    ArrayList<NewsItem> retrieveNewsItems(String sessionId) throws AuthenticationException;

    NewsItem createNewsItem(String sessionId, NewsItem item) throws AuthenticationException;

    FolderDetails updateFolder(String sid, long folderId, FolderDetails update) throws AuthenticationException;

    FolderDetails removeFromUserCollection(String sessionId, long source, ArrayList<Long> ids)
            throws AuthenticationException;

    boolean updateEntry(String sid, EntryInfo info) throws AuthenticationException;

    ArrayList<BulkUploadInfo> retrieveUserSavedDrafts(String sid) throws AuthenticationException;

    BulkUploadInfo retrieveBulkImport(String sid, long id, int start, int limit) throws AuthenticationException;

    FolderDetails deleteFolder(String sessionId, long folderId) throws AuthenticationException;

    SampleStorage createSample(String sessionId, SampleStorage sampleStorage, long entryId)
            throws AuthenticationException;

    SuggestOracle.Response getPermissionSuggestions(Request req);

    SuggestOracle.Response getAutoCompleteSuggestion(AutoCompleteField field, Request request);

    boolean addPermission(String sessionId, PermissionInfo permission) throws AuthenticationException;

    boolean removePermission(String sessionId, PermissionInfo permissionInfo) throws AuthenticationException;

    boolean saveSequence(String sessionId, long entry, String sequenceUser) throws AuthenticationException;

    boolean sendFeedback(String email, String msg);

    String getConfigurationSetting(String name);

    AccountInfo retrieveAccount(String email);

    AccountInfo createNewAccount(AccountInfo info, String url);

    AccountInfo updateAccount(String sid, String email, AccountInfo info) throws AuthenticationException;

    boolean updateAccountPassword(String sid, String email, String password) throws AuthenticationException;

    boolean handleForgotPassword(String email, String url) throws AuthenticationException;

    AccountResults retrieveAllUserAccounts(String sid, int start, int limit) throws AuthenticationException;

    boolean removeSequence(String sid, long entryId) throws AuthenticationException;

    ArrayList<SequenceAnalysisInfo> retrieveEntryTraceSequences(String sid, long entryId)
            throws AuthenticationException;

    ArrayList<SequenceAnalysisInfo> deleteEntryTraceSequences(String sid, long entryId, ArrayList<String> seqId)
            throws AuthenticationException;

    ArrayList<BulkUploadInfo> retrieveDraftsPendingVerification(String sid) throws AuthenticationException;

    ArrayList<GroupInfo> retrieveGroups(String sid, GroupType type) throws AuthenticationException;

    boolean deleteEntryAttachment(String sid, String fileId) throws AuthenticationException;

    EntryInfo retrieveEntryTipDetails(String sessionId, String id, String url) throws AuthenticationException;

    BulkUploadInfo deleteSavedDraft(String sid, long draftId) throws AuthenticationException;

    ArrayList<FolderDetails> deleteEntry(String sessionId, EntryInfo info) throws AuthenticationException;

    boolean approvePendingBulkImport(String sessionId, long id) throws AuthenticationException;

    boolean submitBulkUploadDraft(String sid, long draftId) throws AuthenticationException;

    HashMap<String, String> retrieveSystemSettings(String sid) throws AuthenticationException;

    ArrayList<AccountInfo> retrieveGroupMembers(String sessionId, GroupInfo info) throws AuthenticationException;

    ArrayList<AccountInfo> setGroupMembers(String sessionId, GroupInfo info, ArrayList<AccountInfo> members)
            throws AuthenticationException;

    Boolean setConfigurationSetting(String sid, ConfigurationKey key, String value);

    GroupInfo createNewGroup(String sessionId, GroupInfo info) throws AuthenticationException;

    boolean revertedSubmittedBulkUpload(String sid, long uploadId) throws AuthenticationException;

    FolderDetails retrieveAllVisibleEntrys(String sid, FolderDetails details, ColumnField field, boolean asc,
            int start, int limit) throws AuthenticationException;

    ArrayList<AccountInfo> retrieveAvailableAccounts(String sessionId) throws AuthenticationException;

    HashMap<String, String> retrieveWebOfRegistrySettings(String sid) throws AuthenticationException;

    boolean addWebPartner(String sessionId, String webPartner);

    SearchResults performSearch(String sid, SearchQuery searchQuery, boolean isWeb) throws AuthenticationException;

    boolean setPreferenceSetting(String sid, PreferenceKey key, String value) throws AuthenticationException;

    HashMap<PreferenceKey, String> retrieveUserPreferences(String sid, ArrayList<PreferenceKey> keys)
            throws AuthenticationException;

    boolean isWebOfRegistriesEnabled();

    MessageList retrieveMessages(String sessionId, int start, int count) throws AuthenticationException;

    boolean setBulkUploadDraftName(String sid, long id, String draftName) throws AuthenticationException;

    GroupInfo updateGroup(String sessionId, GroupInfo info) throws AuthenticationException;

    GroupInfo deleteGroup(String sessionId, GroupInfo info) throws AuthenticationException;

    boolean removeAccountFromGroup(String sid, GroupInfo info, AccountInfo account) throws AuthenticationException;

    BulkUploadAutoUpdate autoUpdateBulkUpload(String sid, BulkUploadAutoUpdate wrapper, EntryAddType addType)
            throws AuthenticationException;

    Long updateBulkUploadPreference(String sid, long bulkUploadId, EntryAddType addType, PreferenceInfo info)
            throws AuthenticationException;

    void requestEntryTransfer(String sid, ArrayList<Long> ids, ArrayList<String> sites);

    boolean deleteSample(String sessionId, SampleInfo info) throws AuthenticationException;

    ArrayList<GroupInfo> retrieveUserGroups(String sessionId) throws AuthenticationException;

    Long updateBulkUploadPermissions(String sid, long bulkUploadId, EntryAddType addType,
            ArrayList<PermissionInfo> permissions) throws AuthenticationException;

    boolean promoteCollection(String sessionId, long id) throws AuthenticationException;

    boolean demoteCollection(String sessionId, long id) throws AuthenticationException;
}
