package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.entry.model.Entry;

public interface INameValueObject {
    void setId(long id);

    long getId();

    String getName();

    void setName(String name);

    Entry getEntry();

    void setEntry(Entry entry);
}