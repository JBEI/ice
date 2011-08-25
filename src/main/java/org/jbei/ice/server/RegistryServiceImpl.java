package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.jbei.ice.client.EntryMenu;
import org.jbei.ice.client.RegistryService;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.QueryManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.FilterTrans;
import org.jbei.ice.shared.Folder;
import org.jbei.ice.shared.ProfileInfo;
import org.jbei.ice.shared.SeedTipView;
import org.jbei.ice.shared.StrainTipView;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RegistryServiceImpl extends RemoteServiceServlet implements RegistryService {

    private static final long serialVersionUID = 1L;

    @Override
    public String login(String name, String pass) {

        String sessionId = null;

        try {
            SessionData sessionData = AccountController.authenticate(name, pass);
            sessionId = sessionData.getSessionKey();
            log("User by login '" + name + "' successfully logged in");
            return sessionId;
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by user: " + name);
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (Exception e) {
            Logger.error(e);
        }

        return null;
    }

    @Override
    public boolean sessionValid(String sid) {
        return new Random().nextBoolean();
    }

    @Override
    public boolean logout(String sessionId) {
        try {
            AccountController.deauthenticate(sessionId);
            log("User by sessionId '" + sessionId + "' successfully logged out");
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public ArrayList<Long> retrieveSearchResults(ArrayList<FilterTrans> filters) {
        ArrayList<QueryFilter> queryFilters = new ArrayList<QueryFilter>();
        for (FilterTrans filter : filters) {
            QueryFilter queryFilter = new QueryFilter(filter);
            queryFilters.add(queryFilter);
        }

        try {
            Set<Long> filterResults = QueryManager.runFilters(queryFilters);
            ArrayList<Long> results = new ArrayList<Long>(filterResults);
            return results;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<EntryDataView> retrieveEntryViews(ArrayList<Long> entryIds) {

        // TODO Use Controller
        try {
            ArrayList<EntryDataView> results = new ArrayList<EntryDataView>();
            ArrayList<Entry> entries = EntryManager.getEntriesByIdSet(entryIds);

            for (Entry entry : entries) {

                EntryDataView view = EntryViewFactory.createTipView(entry);
                if (view == null)
                    continue;

                results.add(view);
            }

            return results;
        } catch (ManagerException e) {
            Logger.error("Error retrieving entry id set", e);
            return null;
        }
    }

    @Override
    public EntryDataView retrieveEntryView(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<Folder> retrieveCollections(String sessionId) {

        Folder f1 = new Folder("Rachel's Collection", 0);
        Folder f2 = new Folder("Keio Collection", 1);
        Folder f3 = new Folder("GT Collection", 2);

        ArrayList<Folder> folders = new ArrayList<Folder>();
        folders.add(f1);
        folders.add(f2);
        folders.add(f3);
        return folders;
    }

    @Override
    public ArrayList<EntryDataView> retrieveEntriesForFolder(String sessionId, Folder folder) {
        ArrayList<EntryDataView> list = new ArrayList<EntryDataView>();
        // TODO :
        long id = folder.getId();
        if (id == 1) {
            EntryDataView view = new SeedTipView();
            view.setAlias("foo");
            view.setBioSafetyLevel("2");
            view.setType("Seed");
            list.add(view);
        } else {
            StrainTipView view = new StrainTipView();
            view.setCreator("Hector Plahar");
            view.setName("tesT");
            view.setType("Strain");
            list.add(view);
        }
        return list;
    }

    @Override
    public ArrayList<EntryDataView> retrieveEntriesForMenu(String string, EntryMenu selection) {
        return fakeData();
    }

    private ArrayList<EntryDataView> fakeData() {
        ArrayList<EntryDataView> list = new ArrayList<EntryDataView>();

        Random r = new Random();
        int count = r.nextInt(30);

        while (count > 0) {
            count -= 1;
            EntryDataView view = new StrainTipView();
            view.setPartId("" + count);
            view.setName("Name: " + count);
            list.add(view);
        }

        return list;
    }

    @Override
    public ProfileInfo retrieveProfileInfo(String sessionId, String email) {
        ProfileInfo info = new ProfileInfo();
        info.setEmail(email);
        info.setFirstName("Hector");
        info.setLastName("Plahar");
        info.setInstitution("JBEI");
        info.setJoinDate(System.currentTimeMillis());
        return info;
    }
}
