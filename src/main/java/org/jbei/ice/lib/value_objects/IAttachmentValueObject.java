package org.jbei.ice.lib.value_objects;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Base64String;

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

    public abstract void setData(Base64String data);

    public abstract Base64String getData();

}