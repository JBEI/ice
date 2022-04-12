package org.jbei.ice.folder;

import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a grouping, in this case of folders / collections
 *
 * @author Hector Plahar
 */
public abstract class AbstractFolder implements IDataTransferModel, Comparable<AbstractFolder> {

    private long id;
    private long creationTime;
    private List<AccessPermission> accessPermissions;
    private List<PartData> entries;

    public AbstractFolder() {
        entries = new LinkedList<>();
    }

    public AbstractFolder(long id) {
        this.id = id;
        this.entries = new LinkedList<>();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public List<AccessPermission> getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(List<AccessPermission> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public List<PartData> getEntries() {
        return entries;
    }

    public void setEntries(List<PartData> entries) {
        this.entries = entries;
    }

    public int compareTo(AbstractFolder details) {
        return Long.compare(id, details.getId());
    }
}
