package org.jbei.ice.lib.manuscript;

import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class Manuscript implements IDataTransferModel {

    private long id;
    private String title;
    private String authors;
    private FolderDetails folder;
    private String paragonUrl;
    private ManuscriptStatus status;
    private long creationTime;

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

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
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
}
