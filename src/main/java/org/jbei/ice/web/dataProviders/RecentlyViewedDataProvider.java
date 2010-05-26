package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.web.common.ViewException;

public class RecentlyViewedDataProvider extends WorkspaceDataProvider {

    public RecentlyViewedDataProvider(Account account) {
        super(account);

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

        return workspaces.iterator();
    }

    @Override
    public int size() {
        try {
            return WorkspaceManager.getRecentlyViewedCount(getAccount());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }

    private static final long serialVersionUID = 1L;

}
