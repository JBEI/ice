package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AttachmentInfo implements IsSerializable {

    private long id;
    private String filename;
    private String description;

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
}
