package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BlastProgram;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ProfileInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;
import org.jbei.ice.shared.dto.StorageInfo;

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
    ArrayList<Long> retrieveSearchResults(ArrayList<SearchFilterInfo> filters);

    ArrayList<EntryData> retrieveEntryData(String sid, ArrayList<Long> entries, ColumnField field,
            boolean asc);

    EntryData retrieveEntryView(long id);

    /**
     * Returns list of folders as seen on the collections page
     * collections menu
     */
    ArrayList<FolderDetails> retrieveCollections(String sessionId);

    ArrayList<Long> retrieveEntriesForFolder(String sessionId, long folderId);

    long retrieveAvailableEntryCount(String sessionId);

    ArrayList<Long> retrieveUserEntries(String sid, String userId);

    ArrayList<Long> retrieveAllEntryIDs(String sid);

    ArrayList<Long> retrieveRecentlyViewed(String sid);

    ArrayList<Long> retrieveWorkspaceEntries(String sid);

    HashMap<AutoCompleteField, ArrayList<String>> retrieveAutoCompleteData(String sid);

    EntryInfo retrieveEntryDetails(String sid, long id);

    AccountInfo retrieveAccountInfo(String sid, String userId);

    AccountInfo retrieveAccountInfoForSession(String sid);

    ArrayList<BlastResultInfo> blastSearch(String sid, String searchString, BlastProgram program);

    ArrayList<StorageInfo> retrieveChildren(String sid, long id);

    ArrayList<StorageInfo> retrieveStorageRoot(String sid);

    LinkedList<Long> retrieveSamplesByDepositor(String sid, String email, ColumnField field,
            boolean asc);

    LinkedList<SampleInfo> retrieveSampleInfo(String sid, LinkedList<Long> sampleIds, boolean asc);

    FolderDetails retrieveFolderDetails(String sid, long folderId);

    // collections

    FolderDetails createUserCollection(String sid, String name, String description);

    boolean moveToUserCollection(String sid, ArrayList<Long> source, ArrayList<Long> destination,
            ArrayList<Long> entryIds);

    ProfileInfo retrieveProfileInfo(String sid, String userId);

    ArrayList<BulkImportDraftInfo> retrieveImportDraftData(String sid, String email);

    long createEntry(String sid, EntryInfo info);
}
