package org.jbei.ice.lib.models.interfaces;

import java.util.Date;

import org.jbei.ice.lib.models.Entry;

public interface ISampleValueObject {
    int getId();

    void setId(int id);

    void setUuid(String uuid);

    String getUuid();

    String getDepositor();

    void setDepositor(String depositor);

    String getLabel();

    void setLabel(String label);

    String getNotes();

    void setNotes(String notes);

    Entry getEntry();

    void setEntry(Entry entry);

    Date getCreationTime();

    void setCreationTime(Date creationTime);

    Date getModificationTime();

    void setModificationTime(Date modificationTime);
}