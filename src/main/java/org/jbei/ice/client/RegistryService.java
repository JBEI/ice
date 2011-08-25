package org.jbei.ice.client;

import java.util.ArrayList;

import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.FilterTrans;
import org.jbei.ice.shared.Folder;
import org.jbei.ice.shared.ProfileInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ice")
public interface RegistryService extends RemoteService {

    /**
     * @param name
     * @param pass
     * @return valid session id if the login was sucessful
     */
    String login(String name, String pass);

    boolean sessionValid(String sid);

    boolean logout(String sessionId);

    //
    // Search
    //
    ArrayList<Long> retrieveSearchResults(ArrayList<FilterTrans> filters);

    ArrayList<EntryDataView> retrieveEntryViews(ArrayList<Long> entries);

    EntryDataView retrieveEntryView(long id);

    /**
     * Returns list of folders as seen on the collections page
     * collections menu
     */
    ArrayList<Folder> retrieveCollections(String sessionId);

    /**
     * returns a list of entry data views representing entries which are stored
     * in the folder specified in the param. can return null
     */
    ArrayList<EntryDataView> retrieveEntriesForFolder(String sessionId, Folder folder);

    ArrayList<EntryDataView> retrieveEntriesForMenu(String string, EntryMenu selection);

    //
    // profile page methods
    //
    ProfileInfo retrieveProfileInfo(String sessionId, String email);

}
