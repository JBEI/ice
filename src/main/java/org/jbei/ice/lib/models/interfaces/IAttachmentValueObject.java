package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.entry.model.Entry;

public interface IAttachmentValueObject {
    long getId();

    void setId(long id);

    void setDescription(String description);

    String getDescription();

    String getFileName();

    void setFileName(String fileName);

    String getFileId();

    void setFileId(String fileId);

    Entry getEntry();

    void setEntry(Entry entry);
}