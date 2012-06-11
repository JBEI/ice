package org.jbei.ice.shared;

import java.math.BigInteger;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Folder Transfer Object
 * 
 * @author Hector Plahar
 */

public class FolderDetails implements IsSerializable {

    private long id;
    private String folderName;
    private BigInteger count;
    private boolean systemFolder;
    private String description;
    private ArrayList<Long> contents;

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

    public BigInteger getCount() {
        return count;
    }

    public void setCount(BigInteger count) {
        this.count = count;
    }

    public boolean isSystemFolder() {
        return this.systemFolder;
    }

    public void setIsSystemFolder(boolean isSystemFolder) {
        this.systemFolder = isSystemFolder;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Long> getContents() {
        return contents;
    }

    public void setContents(ArrayList<Long> contents) {
        this.contents = contents;
    }
}
