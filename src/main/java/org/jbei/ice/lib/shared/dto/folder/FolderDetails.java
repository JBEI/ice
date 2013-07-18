package org.jbei.ice.lib.shared.dto.folder;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;
import org.jbei.ice.lib.shared.dto.user.User;

/**
 * Folder Transfer Object
 *
 * @author Hector Plahar
 */

public class FolderDetails implements IDTOModel {

    private long id;
    private String folderName;
    private long count = -1;
    private String description;
    private LinkedList<PartData> entries = new LinkedList<PartData>();
    private FolderType type;
    private User owner;    // owner or person sharing this folder
    private ArrayList<PermissionInfo> permissions;

    public FolderDetails() {
    }

    public FolderDetails(long id, String name) {
        this.id = id;
        this.folderName = name;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.folderName;
    }

    public void setName(String name) {
        this.folderName = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LinkedList<PartData> getEntries() {
        return entries;
    }

    public void setEntries(LinkedList<PartData> entries) {
        this.entries = entries;
    }

    public FolderType getType() {
        return type;
    }

    public void setType(FolderType type) {
        this.type = type;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ArrayList<PermissionInfo> getPermissions() {
        return permissions;
    }

    public void setPermissions(ArrayList<PermissionInfo> permissions) {
        this.permissions = permissions;
    }
}
