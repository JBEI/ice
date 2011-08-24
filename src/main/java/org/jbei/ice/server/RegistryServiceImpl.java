package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.Random;

import org.jbei.ice.client.EntryMenu;
import org.jbei.ice.client.RegistryService;
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
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "THIS_IS_A_FAKE_SESSION_dfsafsfas2345435mkldnmg";
    }

    @Override
    public boolean sessionValid(String sid) {
        return new Random().nextBoolean();
    }

    @Override
    public boolean logout(String sessionId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ArrayList<EntryDataView> getSearchResults(ArrayList<FilterTrans> filters) {
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
