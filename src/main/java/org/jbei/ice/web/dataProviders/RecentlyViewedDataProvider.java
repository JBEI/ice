package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.web.common.ViewException;

public class RecentlyViewedDataProvider extends WorkspaceDataProvider {

    private static final long serialVersionUID = 1L;

    public RecentlyViewedDataProvider(Account account) {
        super(account);

        setSort("dateVisited", false);
    }

    @Override
    public Iterator<Workspace> iterator(int first, int count) {
        workspaces.clear();
        ArrayList<Workspace> result;
        try {
            result = WorkspaceManager.getRecentlyViewedByAccount(getAccount(), first, count);
            workspaces.addAll(result);
        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        if (getSort() != null)
            sort();
        return workspaces.iterator();
    }

    protected void sort() {
        Collections.sort(workspaces, new WorkspaceComparator());
    }

    private class WorkspaceComparator implements Comparator<Workspace> {
        @Override
        public int compare(Workspace o1, Workspace o2) {
            int result = 0;

            if (getSort() == null)
                return result;

            String property = getSort().getProperty();

            PropertyModel<Comparable<Object>> model1 = new PropertyModel<Comparable<Object>>(o1,
                    property);
            PropertyModel<Comparable<Object>> model2 = new PropertyModel<Comparable<Object>>(o2,
                    property);

            result = model1.getObject().compareTo(model2.getObject());

            if (!getSort().isAscending())
                result *= -1;

            return result;
        }
    }

    @Override
    public int size() {
        try {
            return WorkspaceManager.getRecentlyViewedCount(getAccount());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }
}
