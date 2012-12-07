package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Hector Plahar
 */
public class CollectionMenuPresenter {
    private final HashMap<Long, ArrayList<PermissionInfo>> permissions;

    public interface IView {
    }

    private final IView view;

    public CollectionMenuPresenter(IView view) {
        this.view = view;
        permissions = new HashMap<Long, ArrayList<PermissionInfo>>();
    }

    public String formatNumber(long l) {
        NumberFormat format = NumberFormat.getFormat("##,###");
        return format.format(l);
    }

    public void setPermissions(ArrayList<PermissionInfo> list) {
        permissions.clear();
        for (PermissionInfo info : list) {
            ArrayList<PermissionInfo> perms = permissions.get(info.getTypeId());
            if (perms == null) {
                perms = new ArrayList<PermissionInfo>();
            }
            perms.add(info);
            permissions.put(info.getTypeId(), perms);
        }
    }

    public int getPermissionCount(long id) {
        if (!permissions.containsKey(id))
            return 0;

        return permissions.get(id).size();
    }

    public ArrayList<PermissionInfo> getFolderPermissions(long id) {
        if (permissions.containsKey(id))
            return permissions.get(id);

        return new ArrayList<PermissionInfo>();
    }

    public void addPermission(PermissionInfo info) {
        // add if permission does not already exist
        ArrayList<PermissionInfo> perms = permissions.get(info.getTypeId());
        if (perms == null) {
            perms = new ArrayList<PermissionInfo>();
        }

        for (PermissionInfo p : perms) {
            if (p.equals(info))
                return;
        }

        perms.add(info);
        permissions.put(info.getTypeId(), perms);
    }

    public void removePermission(PermissionInfo info) {
        ArrayList<PermissionInfo> perms = permissions.get(info.getTypeId());
        if (perms == null) {
            return;
        }

        Iterator<PermissionInfo> iterator = perms.iterator();
        while (iterator.hasNext()) {
            PermissionInfo p = iterator.next();
            if (p.equals(info)) {
                iterator.remove();
                return;
            }
        }
    }
}
