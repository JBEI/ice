package org.jbei.ice.lib.value_objects;

import java.util.Date;

import org.jbei.ice.lib.models.Entry;

public interface ISampleValueObject {

    public abstract int getId();

    public abstract void setId(int id);

    public abstract void setUuid(String uuid);

    public abstract String getUuid();

    public abstract String getDepositor();

    public abstract void setDepositor(String depositor);

    public abstract String getLabel();

    public abstract void setLabel(String label);

    public abstract String getNotes();

    public abstract void setNotes(String notes);

    public abstract Entry getEntry();

    public abstract void setEntry(Entry entry);

    public abstract Date getCreationTime();

    public abstract void setCreationTime(Date creationTime);

    public abstract Date getModificationTime();

    public abstract void setModificationTime(Date modificationTime);

}