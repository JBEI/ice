package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.models.Entry;

public interface ISelectionMarkerValueObject {
    int getId();

    void setId(int id);

    String getName();

    void setName(String name);

    Entry getEntry();

    void setEntry(Entry entry);
}