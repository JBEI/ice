package org.jbei.ice.lib.vo;

import org.jbei.ice.lib.models.Entry;

public interface IAttachmentValueObject {
    public abstract int getId();

    public abstract void setId(int id);

    public abstract void setDescription(String description);

    public abstract String getDescription();

    public abstract String getFileName();

    public abstract void setFileName(String fileName);

    public abstract String getFileId();

    public abstract void setFileId(String fileId);

    public abstract Entry getEntry();

    public abstract void setEntry(Entry entry);
}