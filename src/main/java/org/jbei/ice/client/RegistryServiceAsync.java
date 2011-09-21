package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BlastProgram;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.FilterTrans;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RegistryServiceAsync {

    void login(String name, String pass, AsyncCallback<AccountInfo> callback);

    /**
     * checks for session validity
     */
    void sessionValid(String sid, AsyncCallback<Boolean> callback);

    /**
     * logs user out and clears all session information.
     * cookies are also reset
     */
    void logout(String sessionId, AsyncCallback<Boolean> callback);

    void retrieveSearchResults(ArrayList<FilterTrans> filters,
            AsyncCallback<ArrayList<Long>> asyncCallback);

    void retrieveEntryData(String sid, ArrayList<Long> entries, ColumnField field, boolean asc,
            AsyncCallback<ArrayList<EntryData>> callback);

    void retrieveEntryView(long id, AsyncCallback<EntryData> callback);

    void retrieveEntryDetails(String sessionId, long id, AsyncCallback<EntryInfo> callback);

    void retrieveCollections(String sessionId, AsyncCallback<ArrayList<FolderDetails>> callback);

    /**
     * retrieves the list of entries for the folder
     */
    void retrieveEntriesForFolder(String sessionId, FolderDetails folder,
            AsyncCallback<ArrayList<Long>> callback);

    void retrieveAvailableEntryCount(String sessionId, AsyncCallback<Long> callback);

    void retrieveUserEntries(String sid, String userId, AsyncCallback<ArrayList<Long>> callback);

    void retrieveAllEntryIDs(String sid, AsyncCallback<ArrayList<Long>> callback);

    void retrieveRecentlyViewed(String sid, AsyncCallback<ArrayList<Long>> callback);

    void retrieveSamplesByDepositor(String sid, String email, ColumnField field, boolean asc,
            AsyncCallback<LinkedList<Long>> callback);

    void retrieveWorkspaceEntries(String sid, AsyncCallback<ArrayList<Long>> callback);

    void retrieveAutoCompleteData(String sid,
            AsyncCallback<HashMap<AutoCompleteField, ArrayList<String>>> callback);

    void retrieveAccountInfo(String sid, String userId, AsyncCallback<AccountInfo> callback);

    void blastSearch(String sid, String searchString, BlastProgram program,
            AsyncCallback<ArrayList<BlastResultInfo>> callback);

    void retrieveChildren(String sid, long id, AsyncCallback<ArrayList<StorageInfo>> callback);

    void retrieveSampleInfo(String sid, LinkedList<Long> sampleIds, boolean asc,
            AsyncCallback<LinkedList<SampleInfo>> callback);

    void retrieveAccountInfoForSession(String sid, AsyncCallback<AccountInfo> callback);
}
