package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.web.common.ViewException;

import edu.emory.mathcs.backport.java.util.Collections;

public class WorkspaceDataProvider extends SortableDataProvider<Workspace> {
    private static final long serialVersionUID = 1L;

    private HashMap<String, String> sortableFieldsMap = new HashMap<String, String>();
    private Account account;

    protected ArrayList<Workspace> workspaces = new ArrayList<Workspace>();

    public WorkspaceDataProvider(Account account) {
        super();

        this.setAccount(account);
        
     // default sort
        setSort("dateVisited", false);
    }

    @Override
    public Iterator<Workspace> iterator(int first, int count) {
        workspaces.clear();

        try {
            // TODO: Tim; Move this to some controller and filter according to permission
            ArrayList<Workspace> results = WorkspaceManager.getByAccount(getAccount(), first, count);

            if( results != null && !results.isEmpty()) {
                workspaces.addAll(results);
                Collections.sort(workspaces, new Comparator<Workspace>() {
                    @Override
                    public int compare(Workspace o1, Workspace o2) {

                        int result = 0;
                        if(getSort() == null)
                            return result;
                        
                        String property = getSort().getProperty();
                        
                        PropertyModel<Comparable<Object>> model1 = new PropertyModel<Comparable<Object>>(o1, property);
                        PropertyModel<Comparable<Object>> model2 = new PropertyModel<Comparable<Object>>(o2, property);
                        
                        result = model1.getObject().compareTo(model2.getObject());
                        
                        if (!getSort().isAscending())
                            result *= -1;

                        return result;
                    }
                });
            }
            
        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        return workspaces.iterator();
    }

    @Override
    public IModel<Workspace> model(Workspace object) {
        return new Model<Workspace>(object);
    }

    @Override
    public int size() {
        try {
            return WorkspaceManager.getCountByAccount(getAccount());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
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

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
