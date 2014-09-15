package org.jbei.ice.lib.dto.folder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;

/**
 * Folder Transfer Object
 *
 * @author Hector Plahar
 */

public class FolderDetails implements IDataTransferModel, Comparable<FolderDetails> {

    private long id;
    private String folderName;
    private long count;
    private String description;
    private boolean propagatePermission;
    private List<PartData> entries = new LinkedList<>();
    private FolderType type;
    private AccountTransfer owner;    // owner or person sharing this folder
    private ArrayList<AccessPermission> accessPermissions;
    private boolean publicReadAccess;
    private boolean canEdit;
    private long created;
    private FolderDetails parent;

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

    public List<PartData> getEntries() {
        return entries;
    }

    public void setEntries(List<PartData> entries) {
        this.entries = entries;
    }

    public FolderType getType() {
        return type;
    }

    public void setType(FolderType type) {
        if (type == null)
            this.type = FolderType.PRIVATE;
        else
            this.type = type;
    }

    public AccountTransfer getOwner() {
        return owner;
    }

    public void setOwner(AccountTransfer owner) {
        this.owner = owner;
    }

    public ArrayList<AccessPermission> getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(ArrayList<AccessPermission> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public boolean isPropagatePermission() {
        return propagatePermission;
    }

    public void setPropagatePermission(boolean propagatePermission) {
        this.propagatePermission = propagatePermission;
    }

    public boolean isPublicReadAccess() {
        return publicReadAccess;
    }

    public void setPublicReadAccess(boolean publicReadAccess) {
        this.publicReadAccess = publicReadAccess;
    }

    public int compareTo(FolderDetails details) {
        return Integer.compare((int) id, (int) details.getId());
    }

    public FolderDetails getParent() {
        return parent;
    }

    public void setParent(FolderDetails parent) {
        this.parent = parent;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
