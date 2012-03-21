package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.NewsItem;
import org.jbei.ice.shared.dto.ProfileInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RegistryServiceAsync {

    void login(String name, String pass, AsyncCallback<AccountInfo> callback);

    void sessionValid(String sid, AsyncCallback<AccountInfo> callback);

    /**
     * logs user out and clears all session information.
     * cookies are also reset
     */
    void logout(String sessionId, AsyncCallback<Boolean> callback);

    void retrieveSearchResults(String sid, ArrayList<SearchFilterInfo> filters,
            AsyncCallback<ArrayList<Long>> asyncCallback);

    void retrieveEntryData(String sid, ArrayList<Long> entries, ColumnField field, boolean asc,
            AsyncCallback<ArrayList<EntryInfo>> callback);

    void retrieveEntryView(long id, AsyncCallback<EntryInfo> callback);

    void retrieveEntryDetails(String sessionId, long id, AsyncCallback<EntryInfo> callback);

    /**
     * retrieves the list of entries for the folder
     */
    void retrieveEntriesForFolder(String sessionId, long folderId,
            AsyncCallback<FolderDetails> callback);

    void retrieveAvailableEntryCount(String sessionId, AsyncCallback<Long> callback);

    void retrieveUserEntries(String sid, String userId, AsyncCallback<FolderDetails> asyncCallback);

    void retrieveAllEntryIDs(String sid, AsyncCallback<FolderDetails> asyncCallback);

    void retrieveRecentlyViewed(String sid, AsyncCallback<ArrayList<Long>> callback);

    void retrieveSamplesByDepositor(String sid, String email, ColumnField field, boolean asc,
            AsyncCallback<LinkedList<Long>> callback);

    void retrieveWorkspaceEntries(String sid, AsyncCallback<ArrayList<Long>> callback);

    void retrieveAutoCompleteData(String sid,
            AsyncCallback<HashMap<AutoCompleteField, ArrayList<String>>> callback);

    void retrieveAccountInfo(String sid, String userId, AsyncCallback<AccountInfo> callback);

    void blastSearch(String sid, String searchString, QueryOperator program,
            AsyncCallback<ArrayList<BlastResultInfo>> callback);

    void retrieveChildren(String sid, long id, AsyncCallback<ArrayList<StorageInfo>> callback);

    void retrieveSampleInfo(String sid, LinkedList<Long> sampleIds, boolean asc,
            AsyncCallback<LinkedList<SampleInfo>> callback);

    void retrieveAccountInfoForSession(String sid, AsyncCallback<AccountInfo> callback);

    void retrieveFolderDetails(String sid, long folderId, AsyncCallback<FolderDetails> callback);

    void retrieveImportDraftData(String sid, String email,
            AsyncCallback<ArrayList<BulkImportDraftInfo>> callback);

    /**
     * Profile
     */
    void retrieveProfileInfo(String sid, String userId, AsyncCallback<ProfileInfo> callback);

    /**
     * Collections
     */

    void retrieveCollections(String sessionId, AsyncCallback<ArrayList<FolderDetails>> callback);

    void updateFolder(String sid, long folderId, FolderDetails update,
            AsyncCallback<FolderDetails> callback);

    void createUserCollection(String sid, String name, String description,
            ArrayList<Long> contents, AsyncCallback<FolderDetails> callback);

    void retrieveUserCollections(String sessionId, String userId,
            AsyncCallback<ArrayList<FolderDetails>> callback);

    void moveToUserCollection(String sid, long source, ArrayList<Long> destination,
            ArrayList<Long> entryIds, AsyncCallback<ArrayList<FolderDetails>> callback);

    void addEntriesToCollection(String sid, ArrayList<Long> destination, ArrayList<Long> entryIds,
            AsyncCallback<ArrayList<FolderDetails>> callback);

    //
    // end collections
    //

    void retrieveStorageRoot(String sid, AsyncCallback<ArrayList<StorageInfo>> callback);

    void createEntry(String sid, HashSet<EntryInfo> info, AsyncCallback<ArrayList<Long>> callback);

    void updateEntry(String sid, EntryInfo info, AsyncCallback<Boolean> callback);

    void retrieveStorageSchemes(String sessionId, EntryType type,
            AsyncCallback<HashMap<String, ArrayList<String>>> callback);

    void retrievePermissionData(String sessionId, Long entryId,
            AsyncCallback<ArrayList<PermissionInfo>> callback);

    void retrieveAllGroups(String sessionId, AsyncCallback<LinkedHashMap<Long, String>> callback);

    void retrieveAllAccounts(String sessionId, AsyncCallback<LinkedHashMap<Long, String>> callback);

    // news

    void retrieveNewsItems(String sessionId, AsyncCallback<ArrayList<NewsItem>> callback);

    void createNewsItem(String sessionId, NewsItem item, AsyncCallback<NewsItem> callback);

    // bulk import and draft

    void saveBulkImportDraft(String sid, String email, String name, ArrayList<EntryInfo> primary,
            ArrayList<EntryInfo> secondary, AsyncCallback<BulkImportDraftInfo> callback);

    void submitBulkImport(String sid, String email, ArrayList<EntryInfo> primary,
            ArrayList<EntryInfo> seconday, AsyncCallback<Boolean> callback);

    void removeFromUserCollection(String sessionId, long source, ArrayList<Long> ids,
            AsyncCallback<FolderDetails> asyncCallback);

    void retrieveBulkImport(String sid, long id, AsyncCallback<BulkImportDraftInfo> callback);

    void deleteFolder(String sessionId, long folderId, AsyncCallback<FolderDetails> callback);
}
