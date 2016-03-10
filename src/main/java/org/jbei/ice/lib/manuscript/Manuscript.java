package org.jbei.ice.lib.manuscript;

import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class Manuscript implements IDataTransferModel {

    private long id;
    private String title;
    private String authorFirstName;
    private String authorLastName;
    private FolderDetails folder;
    private String paragonUrl;
    private ManuscriptStatus status;
    private long creationTime;
    private String zipFileName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    public FolderDetails getFolder() {
        return folder;
    }

    public void setFolder(FolderDetails folder) {
        this.folder = folder;
    }

    public String getParagonUrl() {
        return paragonUrl;
    }

    public void setParagonUrl(String paragonUrl) {
        this.paragonUrl = paragonUrl;
    }

    public ManuscriptStatus getStatus() {
        return status;
    }

    public void setStatus(ManuscriptStatus status) {
        this.status = status;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }
}
