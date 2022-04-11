package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class AttachmentInfo implements IDataTransferModel {

    private long id;
    private String filename;
    private String description;
    private String fileId;
    private boolean canEdit;

    public AttachmentInfo() {
    }

    public AttachmentInfo(String fileName) {
        this.filename = fileName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return this.fileId;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
