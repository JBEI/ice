package org.jbei.ice.client;

import java.util.ArrayList;

import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.FilterTrans;
import org.jbei.ice.shared.Folder;
import org.jbei.ice.shared.ProfileInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RegistryServiceAsync {

    void login(String name, String pass, AsyncCallback<String> callback);

    void sessionValid(String sid, AsyncCallback<Boolean> callback);

    void logout(String sessionId, AsyncCallback<Boolean> callback);

    void retrieveSearchResults(ArrayList<FilterTrans> filters,
            AsyncCallback<ArrayList<Long>> asyncCallback);

    void retrieveEntryViews(ArrayList<Long> entries,
            AsyncCallback<ArrayList<EntryDataView>> callback);

    void retrieveEntryView(long id, AsyncCallback<EntryDataView> callback);

    void retrieveCollections(String sessionId, AsyncCallback<ArrayList<Folder>> callback);

    /**
     * retrieves the list of entries for the folder
     */
    void retrieveEntriesForFolder(String sessionId, Folder folder,
            AsyncCallback<ArrayList<EntryDataView>> callback);

    void retrieveEntriesForMenu(String string, EntryMenu selection,
            AsyncCallback<ArrayList<EntryDataView>> asyncCallback);

    void retrieveProfileInfo(String sessionId, String email, AsyncCallback<ProfileInfo> callback);
}
