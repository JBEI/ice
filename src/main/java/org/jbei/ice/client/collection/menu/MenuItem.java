package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;

import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.folder.FolderShareType;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

public class MenuItem extends OptionSelect {

    private long count;
    private ArrayList<PermissionInfo> permissions;
    private AccountInfo owner;
    private FolderShareType shareType;

    public MenuItem(long id, String name, long count) {
        super(id, name);
        this.count = count;
    }

    public boolean hasSubMenu() {
        return getId() > 0;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setPermissions(ArrayList<PermissionInfo> permissions) {
        this.permissions = permissions;
    }

    public ArrayList<PermissionInfo> getPermissions() {
        return permissions;
    }

    public AccountInfo getOwner() {
        return owner;
    }

    public void setOwner(AccountInfo owner) {
        this.owner = owner;
    }

    public FolderShareType getShareType() {
        return shareType;
    }

    public void setShareType(FolderShareType shareType) {
        this.shareType = shareType;
    }
}
