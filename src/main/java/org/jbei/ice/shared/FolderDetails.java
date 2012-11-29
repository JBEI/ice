package org.jbei.ice.shared;

import java.util.LinkedList;

import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Folder Transfer Object
 *
 * @author Hector Plahar
 */

public class FolderDetails implements IsSerializable {

    private long id;
    private String folderName;
    private long count = -1;
    private boolean systemFolder;
    private String description;
    private LinkedList<EntryInfo> entries = new LinkedList<EntryInfo>();

    public FolderDetails() {
    }

    public FolderDetails(long id, String name, boolean systemFolder) {
        this.id = id;
        this.folderName = name;
        this.systemFolder = systemFolder;
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

    public boolean isSystemFolder() {
        return this.systemFolder;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LinkedList<EntryInfo> getEntries() {
        return entries;
    }

    public void setEntries(LinkedList<EntryInfo> entries) {
        this.entries = entries;
    }
}
