package org.jbei.ice.lib.vo;

import org.jbei.ice.lib.models.Entry;

public interface IAttachmentValueObject {
    int getId();

    void setId(int id);

    void setDescription(String description);

    String getDescription();

    String getFileName();

    void setFileName(String fileName);

    String getFileId();

    void setFileId(String fileId);

    Entry getEntry();

    void setEntry(Entry entry);
}