package org.jbei.ice.lib.dto.folder;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;

/**
 * Folder Transfer Object
 *
 * @author Hector Plahar
 */

public class FolderDetails implements IDataTransferModel {

    private long id;
    private String folderName;
    private long count = -1;
    private String description;
    private boolean propagatePermission;
    private LinkedList<PartData> entries = new LinkedList<PartData>();
    private FolderType type;
    private AccountTransfer owner;    // owner or person sharing this folder
    private ArrayList<AccessPermission> accessPermissions;
    private boolean publicReadAccess;

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
}
