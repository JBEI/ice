package org.jbei.ice.lib.dto.access;

import org.jbei.ice.lib.dto.folder.FolderDetails;

/**
 * Access permission for remote access. Includes more information than the regular access permission object
 *
 * @author Hector Plahar
 */
public class RemoteAccessPermission extends AccessPermission {

    private FolderDetails folderDetails;

    public FolderDetails getFolderDetails() {
        return folderDetails;
    }

    public void setFolderDetails(FolderDetails folderDetails) {
        this.folderDetails = folderDetails;
    }
}
