package org.jbei.ice.lib.shared.dto.entry;

import org.jbei.ice.lib.shared.dto.IDTOModel;

public class AttachmentInfo implements IDTOModel {

    private long id;
    private String filename;
    private String description;
    private String fileId;

    public AttachmentInfo() {
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
}
