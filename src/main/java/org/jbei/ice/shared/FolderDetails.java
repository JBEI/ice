package org.jbei.ice.shared;

import java.io.Serializable;

/**
 * Folder Transfer Object
 * 
 * @author Hector Plahar
 */

public class FolderDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String folderName;

    public FolderDetails() {
    }

    public FolderDetails(long id, String name) {
        this.id = id;
        this.folderName = name;

    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.folderName;
    }

}
