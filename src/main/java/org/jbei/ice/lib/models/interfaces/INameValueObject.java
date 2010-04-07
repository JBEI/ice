package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.models.Entry;

public interface INameValueObject {
    void setId(int id);

    int getId();

    String getName();

    void setName(String name);

    Entry getEntry();

    void setEntry(Entry entry);
}