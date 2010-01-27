package org.jbei.ice.lib.value_objects;

import org.jbei.ice.lib.models.Entry;

public interface ISelectionMarkerValueObject {

    public abstract int getId();

    public abstract void setId(int id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Entry getEntry();

    public abstract void setEntry(Entry entry);

}