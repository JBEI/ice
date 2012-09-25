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
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public interface RegistryServiceAsync {

    void login(String name, String pass, AsyncCallback<AccountInfo> callback);

    void sessionValid(String sid, AsyncCallback<AccountInfo> callback);

    /**
     * logs user out and clears all session information.
     * cookies are also reset
     */
    void logout(String sessionId, AsyncCallback<Boolean> callback);

    void retrieveSearchResults(String sid, ArrayList<SearchFilterInfo> filters,
            AsyncCallback<LinkedList<SearchResultInfo>> asyncCallback);

    void retrieveEntryData(String sid, ColumnField field, boolean ascending,
            LinkedList<Long> entries, AsyncCallback<LinkedList<EntryInfo>> callback);

    void retrieveEntryDetails(String sessionId, long id, AsyncCallback<EntryInfo> callback);

    void retrieveEntryTipDetails(String sessionId, long id, AsyncCallback<EntryInfo> callback)
            throws AuthenticationException;

    void sortEntryList(String sessionId, LinkedList<Long> ids, ColumnField field, boolean asc,
            AsyncCallback<LinkedList<Long>> callback) throws AuthenticationException;

    // permissions
    void getPermissionSuggestions(SuggestOracle.Request req,
            AsyncCallback<SuggestOracle.Response> callback);

    /**
     * retrieves the list of entries for the folder
     */
    void retrieveEntriesForFolder(String sessionId, long folderId,
            AsyncCallback<FolderDetails> callback);

    void retrieveUserEntries(String sid, String userId, AsyncCallback<FolderDetails> asyncCallback);

    void retrieveAllVisibleEntryIDs(String sid, AsyncCallback<FolderDetails> asyncCallback);

    void retrieveSamplesByDepositor(String sid, String email, ColumnField field, boolean asc,
            AsyncCallback<LinkedList<Long>> callback);

    void retrieveAutoCompleteData(String sid,
            AsyncCallback<HashMap<AutoCompleteField, ArrayList<String>>> callback);

    void blastSearch(String sid, String searchString, QueryOperator program,
            AsyncCallback<ArrayList<BlastResultInfo>> callback);

    void retrieveSampleInfo(String sid, LinkedList<Long> sampleIds, ColumnField sortField,
            boolean asc, AsyncCallback<LinkedList<SampleInfo>> callback);

    void retrieveUserSavedDrafts(String sid, AsyncCallback<ArrayList<BulkUploadInfo>> callback)
            throws AuthenticationException;

    void retrieveDraftsPendingVerification(String sid,
            AsyncCallback<ArrayList<BulkUploadInfo>> callback) throws AuthenticationException;

    void deleteSavedDraft(String sid, long draftId,
            AsyncCallback<BulkUploadInfo> callback) throws AuthenticationException;

    void createSample(String sessionId, SampleStorage sampleStorage, long entryId,
            AsyncCallback<SampleStorage> callback) throws AuthenticationException;

    void retrieveProfileInfo(String sid, String userId, AsyncCallback<AccountInfo> callback)
            throws AuthenticationException;

    /**
     * Collections
     */

    void retrieveCollections(String sessionId, AsyncCallback<ArrayList<FolderDetails>> callback);

    void updateFolder(String sid, long folderId, FolderDetails update,
            AsyncCallback<FolderDetails> callback) throws AuthenticationException;

    void createUserCollection(String sid, String name, String description,
            ArrayList<Long> arrayList, AsyncCallback<FolderDetails> callback)
            throws AuthenticationException;

    void moveToUserCollection(String sid, long source, ArrayList<Long> destination,
            ArrayList<Long> entryIds, AsyncCallback<ArrayList<FolderDetails>> callback)
            throws AuthenticationException;

    void addEntriesToCollection(String sid, ArrayList<Long> destination, ArrayList<Long> entryIds,
            AsyncCallback<ArrayList<FolderDetails>> callback) throws AuthenticationException;

    //
    // end collections
    //

    void updateEntry(String sid, EntryInfo info, AsyncCallback<Boolean> callback)
            throws AuthenticationException;

    void retrieveStorageSchemes(String sessionId, EntryType type,
            AsyncCallback<HashMap<SampleInfo, ArrayList<String>>> callback);

    void retrievePermissionData(String sessionId, Long entryId,
            AsyncCallback<ArrayList<PermissionInfo>> callback) throws AuthenticationException;

    // news

    void retrieveNewsItems(String sessionId, AsyncCallback<ArrayList<NewsItem>> callback)
            throws AuthenticationException;

    void createNewsItem(String sessionId, NewsItem item, AsyncCallback<NewsItem> callback)
            throws AuthenticationException;

    // bulk import and draft

    void retrieveBulkImport(String sid, long id, AsyncCallback<BulkUploadInfo> callback)
            throws AuthenticationException;

    void deleteFolder(String sessionId, long folderId, AsyncCallback<FolderDetails> callback);

    void addPermission(String sessionId, long entryId, PermissionInfo permission,
            AsyncCallback<Boolean> callback) throws AuthenticationException;

    void removePermission(String sessionId, long entryId, PermissionInfo permissionInfo,
            AsyncCallback<Boolean> callback) throws AuthenticationException;

    void saveSequence(String sessionId, long entry, String sequenceUser,
            AsyncCallback<Boolean> callback) throws AuthenticationException;

    void sendFeedback(String email, String msg, AsyncCallback<Boolean> callback);

    void getSetting(String name, AsyncCallback<String> callback);

    void retrieveAccount(String email, AsyncCallback<AccountInfo> callback);

    void createNewAccount(AccountInfo info, String url, AsyncCallback<AccountInfo> callback);

    void updateAccount(String sid, String email, AccountInfo info,
            AsyncCallback<AccountInfo> callback) throws AuthenticationException;

    void updateAccountPassword(String sid, String email, String password,
            AsyncCallback<Boolean> callback) throws AuthenticationException;

    void handleForgotPassword(String email, String url, AsyncCallback<Boolean> callback)
            throws AuthenticationException;

    void retrieveAllUserAccounts(String sid, AsyncCallback<ArrayList<AccountInfo>> callback)
            throws AuthenticationException;

    void removeSequence(String sid, long entryId, AsyncCallback<Boolean> callback)
            throws AuthenticationException;

    void retrieveEntryTraceSequences(String sid, long entryId,
            AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback) throws AuthenticationException;

    void deleteEntryTraceSequences(String sid, long entryId, ArrayList<String> seqId,
            AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback) throws AuthenticationException;

    void deleteEntryAttachment(String sid, String fileId, AsyncCallback<Boolean> callback)
            throws AuthenticationException;

    void retrieveAllGroups(String sessionId, AsyncCallback<ArrayList<GroupInfo>> callback)
            throws AuthenticationException;

    void deleteEntry(String sessionId, EntryInfo info,
            AsyncCallback<ArrayList<FolderDetails>> callback) throws AuthenticationException;

    void createEntry(String sid, EntryInfo info, AsyncCallback<Long> async)
            throws AuthenticationException;

    void createStrainWithPlasmid(String sid, HashSet<EntryInfo> infoSet,
            AsyncCallback<ArrayList<Long>> async) throws AuthenticationException;

    void removeFromUserCollection(String sessionId, long source, ArrayList<Long> ids,
            AsyncCallback<FolderDetails> async) throws AuthenticationException;

    void updateBulkImportDraft(String sessionId, long draftId,
            ArrayList<EntryInfo> list, String groupUUID, AsyncCallback<BulkUploadInfo> async);

    void saveBulkImportDraft(String sid, String name,
            EntryAddType importType, ArrayList<EntryInfo> entryList, String groupUUID,
            AsyncCallback<BulkUploadInfo> async);

    void submitBulkImport(String sid, EntryAddType importType,
            ArrayList<EntryInfo> entryList, String groupUUID, AsyncCallback<Boolean> async);

    void approvePendingBulkImport(String sessionId, long id, ArrayList<EntryInfo> entryList, String groupUUID,
            AsyncCallback<Boolean> async);

    void submitBulkImportDraft(String sid, long draftId,
            ArrayList<EntryInfo> entryList, String groupUUID, AsyncCallback<Boolean> async);
}
