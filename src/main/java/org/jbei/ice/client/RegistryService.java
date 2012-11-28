package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.*;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

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

    //
    // Search
    //
    SearchResults retrieveSearchResults(String sid, ArrayList<SearchFilterInfo> filters, int start, int limit)
            throws AuthenticationException;

    ArrayList<BlastResultInfo> blastSearch(String sid, String searchString, QueryOperator program)
            throws AuthenticationException;

    /**
     * Returns list of folders as seen on the collections page
     * collections menu
     */
    ArrayList<FolderDetails> retrieveCollections(String sessionId) throws AuthenticationException;

    FolderDetails retrieveEntriesForFolder(String sessionId, long folderId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException;

    FolderDetails retrieveUserEntries(String sid, String userId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException;

    EntryInfo retrieveEntryDetails(String sid, long id) throws AuthenticationException;

    LinkedList<SampleInfo> retrieveSampleInfo(String sid, LinkedList<Long> sampleIds,
            ColumnField sortField, boolean asc) throws AuthenticationException;

    // collections

    FolderDetails createUserCollection(String sid, String name, String description,
            ArrayList<Long> contents) throws AuthenticationException;

    ArrayList<FolderDetails> moveToUserCollection(String sid, long source,
            ArrayList<Long> destination, ArrayList<Long> entryIds)
            throws AuthenticationException;

    AccountInfo retrieveProfileInfo(String sid, String userId) throws AuthenticationException;

    Long createEntry(String sid, EntryInfo info) throws AuthenticationException;

    ArrayList<FolderDetails> addEntriesToCollection(String sid, ArrayList<Long> destination,
            ArrayList<Long> entryIds) throws AuthenticationException;

    HashMap<SampleInfo, ArrayList<String>> retrieveStorageSchemes(String sessionId, EntryType type)
            throws AuthenticationException;

    ArrayList<PermissionInfo> retrievePermissionData(String sessionId, Long entryId) throws AuthenticationException;

    ArrayList<NewsItem> retrieveNewsItems(String sessionId) throws AuthenticationException;

    NewsItem createNewsItem(String sessionId, NewsItem item) throws AuthenticationException;

    FolderDetails updateFolder(String sid, long folderId, FolderDetails update) throws AuthenticationException;

    FolderDetails removeFromUserCollection(String sessionId, long source, ArrayList<Long> ids)
            throws AuthenticationException;

    boolean updateEntry(String sid, EntryInfo info) throws AuthenticationException;

    ArrayList<BulkUploadInfo> retrieveUserSavedDrafts(String sid) throws AuthenticationException;

    BulkUploadInfo retrieveBulkImport(String sid, long id) throws AuthenticationException;

    FolderDetails deleteFolder(String sessionId, long folderId) throws AuthenticationException;

    SampleStorage createSample(String sessionId, SampleStorage sampleStorage, long entryId)
            throws AuthenticationException;

    SuggestOracle.Response getPermissionSuggestions(Request req);

    SuggestOracle.Response getAutoCompleteSuggestion(AutoCompleteField field, Request request);

    boolean addPermission(String sessionId, long entryId, PermissionInfo permission) throws AuthenticationException;

    boolean removePermission(String sessionId, long entryId, PermissionInfo permissionInfo)
            throws AuthenticationException;

    boolean saveSequence(String sessionId, long entry, String sequenceUser) throws AuthenticationException;

    boolean sendFeedback(String email, String msg);

    String getSetting(String name);

    AccountInfo retrieveAccount(String email);

    AccountInfo createNewAccount(AccountInfo info, String url);

    AccountInfo updateAccount(String sid, String email, AccountInfo info) throws AuthenticationException;

    boolean updateAccountPassword(String sid, String email, String password) throws AuthenticationException;

    boolean handleForgotPassword(String email, String url) throws AuthenticationException;

    ArrayList<AccountInfo> retrieveAllUserAccounts(String sid) throws AuthenticationException;

    boolean removeSequence(String sid, long entryId) throws AuthenticationException;

    ArrayList<SequenceAnalysisInfo> retrieveEntryTraceSequences(String sid, long entryId)
            throws AuthenticationException;

    ArrayList<SequenceAnalysisInfo> deleteEntryTraceSequences(String sid, long entryId,
            ArrayList<String> seqId) throws AuthenticationException;

    ArrayList<BulkUploadInfo> retrieveDraftsPendingVerification(String sid) throws AuthenticationException;

    GroupInfo retrieveGroup(String sessionId, String uuid) throws AuthenticationException;

    boolean deleteEntryAttachment(String sid, String fileId) throws AuthenticationException;

    EntryInfo retrieveEntryTipDetails(String sessionId, long id) throws AuthenticationException;

    BulkUploadInfo deleteSavedDraft(String sid, long draftId) throws AuthenticationException;

    ArrayList<FolderDetails> deleteEntry(String sessionId, EntryInfo info) throws AuthenticationException;

    ArrayList<Long> createStrainWithPlasmid(String sid, HashSet<EntryInfo> infoSet) throws
            AuthenticationException;

    BulkUploadInfo updateBulkImportDraft(String sessionId, long draftId,
            ArrayList<EntryInfo> list, String groupUUID) throws AuthenticationException;

    BulkUploadInfo saveBulkImportDraft(String sid, String name,
            EntryAddType importType, ArrayList<EntryInfo> entryList, String groupUUID) throws AuthenticationException;

    boolean submitBulkImport(String sid, EntryAddType importType,
            ArrayList<EntryInfo> entryList, String groupUUID) throws AuthenticationException;

    boolean approvePendingBulkImport(String sessionId, long id, ArrayList<EntryInfo> entryList, String groupUUID)
            throws AuthenticationException;

    boolean submitBulkImportDraft(String sid, long draftId,
            ArrayList<EntryInfo> entryList, String groupUUID) throws AuthenticationException;

    HashMap<String, String> retrieveSystemSettings(String sid) throws AuthenticationException;

    ArrayList<AccountInfo> retrieveGroupMembers(String sessionId, GroupInfo info)
            throws AuthenticationException;

    Boolean setConfigurationSetting(ConfigurationKey key, String value);

    GroupInfo createNewGroup(String sessionId, String label, String description, long parentId, GroupType type)
            throws AuthenticationException;

    boolean revertedSubmittedBulkUpload(String sid, long uploadId) throws AuthenticationException;

    FolderDetails retrieveAllVisibleEntryIDs(String sid, FolderDetails details, ColumnField field, boolean asc,
            int start, int limit) throws AuthenticationException;
}
