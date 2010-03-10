package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.lib.query.SortField;

public class WorkspaceDataProvider extends SortableDataProvider<Workspace> {

    private static final long serialVersionUID = 1L;
    private HashMap<String, String> sortableFieldsMap = new HashMap<String, String>();
    private Account account;

    protected ArrayList<Workspace> workspaces = new ArrayList<Workspace>();

    public WorkspaceDataProvider(Account account) {
        super();
        this.account = account;
        sortableFieldsMap.put("id", "id");
        sortableFieldsMap.put("starred", "starred");
        sortableFieldsMap.put("dateAdded", "dateAdded");
        sortableFieldsMap.put("dateVisited", "dateVisited");

        setSort("dateAdded", false);
    }

    @Override
    public Iterator<Workspace> iterator(int first, int count) {
        workspaces.clear();

        try {
            SortParam sp = getSort();
            String field = getSortableField(sp.getProperty());
            ArrayList<Workspace> result = WorkspaceManager.getByAccount(account, first, count,
                    new SortField[] { new SortField(field, sp.isAscending()) });

            workspaces.addAll(result);
        } catch (Exception e) {
            Logger.warn("WorkspaceDataProvider error: " + e.toString());
        }
        return workspaces.iterator();
    }

    @Override
    public IModel<Workspace> model(Workspace object) {
        return new Model<Workspace>(object);
    }

    @Override
    public int size() {
        return WorkspaceManager.getCountByAccount(account);
    }

    protected String getSortableField(String key) {
        String result = sortableFieldsMap.get(key);
        if (result == null) {
            result = "dateAdded";
        }
        return result;
    }

    public ArrayList<Entry> getEntries() {
        ArrayList<Entry> result = new ArrayList<Entry>();
        for (Workspace workspace : workspaces) {
            result.add(workspace.getEntry());
        }
        return result;
    }

}
