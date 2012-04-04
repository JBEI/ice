package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.jbei.ice.client.entry.view.model.SampleStorage;
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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ice")
public interface RegistryService extends RemoteService {

    AccountInfo login(String name, String pass);

    AccountInfo sessionValid(String sid);

    boolean logout(String sessionId);

    //
    // Search
    //
    ArrayList<Long> retrieveSearchResults(String sid, ArrayList<SearchFilterInfo> filters);

    ArrayList<BlastResultInfo> blastSearch(String sid, String searchString, QueryOperator program);

    ArrayList<EntryInfo> retrieveEntryData(String sid, ArrayList<Long> entries, ColumnField field,
            boolean asc);

    String linkifyText(String value);

    /**
     * Returns list of folders as seen on the collections page
     * collections menu
     */
    ArrayList<FolderDetails> retrieveCollections(String sessionId);

    ArrayList<FolderDetails> retrieveUserCollections(String sessionId, String userId);

    FolderDetails retrieveEntriesForFolder(String sessionId, long folderId);

    long retrieveAvailableEntryCount(String sessionId);

    FolderDetails retrieveUserEntries(String sid, String userId);

    FolderDetails retrieveAllEntryIDs(String sid);

    ArrayList<Long> retrieveRecentlyViewed(String sid);

    ArrayList<Long> retrieveWorkspaceEntries(String sid);

    HashMap<AutoCompleteField, ArrayList<String>> retrieveAutoCompleteData(String sid);

    EntryInfo retrieveEntryDetails(String sid, long id);

    AccountInfo retrieveAccountInfo(String sid, String userId);

    AccountInfo retrieveAccountInfoForSession(String sid);

    ArrayList<StorageInfo> retrieveChildren(String sid, long id);

    ArrayList<StorageInfo> retrieveStorageRoot(String sid);

    LinkedList<Long> retrieveSamplesByDepositor(String sid, String email, ColumnField field,
            boolean asc);

    LinkedList<SampleInfo> retrieveSampleInfo(String sid, LinkedList<Long> sampleIds,
            ColumnField sortField, boolean asc);

    FolderDetails retrieveFolderDetails(String sid, long folderId);

    // collections

    FolderDetails createUserCollection(String sid, String name, String description,
            ArrayList<Long> contents);

    ArrayList<FolderDetails> moveToUserCollection(String sid, long source,
            ArrayList<Long> destination, ArrayList<Long> entryIds);

    ProfileInfo retrieveProfileInfo(String sid, String userId);

    ArrayList<Long> createEntry(String sid, HashSet<EntryInfo> info);

    ArrayList<FolderDetails> addEntriesToCollection(String sid, ArrayList<Long> destination,
            ArrayList<Long> entryIds);

    HashMap<SampleInfo, ArrayList<String>> retrieveStorageSchemes(String sessionId, EntryType type);

    ArrayList<PermissionInfo> retrievePermissionData(String sessionId, Long entryId);

    LinkedHashMap<Long, String> retrieveAllGroups(String sessionId);

    LinkedHashMap<Long, String> retrieveAllAccounts(String sessionId);

    ArrayList<NewsItem> retrieveNewsItems(String sessionId);

    NewsItem createNewsItem(String sessionId, NewsItem item);

    FolderDetails updateFolder(String sid, long folderId, FolderDetails update);

    FolderDetails removeFromUserCollection(String sessionId, long source, ArrayList<Long> ids);

    boolean updateEntry(String sid, EntryInfo info);

    boolean submitBulkImport(String sid, String email, ArrayList<EntryInfo> primary,
            ArrayList<EntryInfo> seconday);

    ArrayList<BulkImportDraftInfo> retrieveImportDraftData(String sid, String email);

    BulkImportDraftInfo saveBulkImportDraft(String sid, String email, String name,
            ArrayList<EntryInfo> primary, ArrayList<EntryInfo> secondary);

    BulkImportDraftInfo retrieveBulkImport(String sid, long id);

    FolderDetails deleteFolder(String sessionId, long folderId);

    SampleStorage createSample(String sessionId, SampleStorage sampleStorage, long entryId);

    boolean updatePermission(String sessionId, long id, ArrayList<PermissionInfo> permissions);

    BulkImportDraftInfo updateBulkImportDraft(String sessionId, long id, String email, String name,
            ArrayList<EntryInfo> primary, ArrayList<EntryInfo> secondary);
}
