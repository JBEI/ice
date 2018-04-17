package org.jbei.ice.lib.dto.access;

import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.web.RegistryPartner;

/**
 * Access permission for remote access. Includes more information than the regular access permission object
 *
 * @author Hector Plahar
 */
public class RemoteAccessPermission extends AccessPermission {

    private FolderDetails folderDetails;
    private RegistryPartner partner;
    private String secret;

    public FolderDetails getFolderDetails() {
        return folderDetails;
    }

    public void setFolderDetails(FolderDetails folderDetails) {
        this.folderDetails = folderDetails;
    }

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
