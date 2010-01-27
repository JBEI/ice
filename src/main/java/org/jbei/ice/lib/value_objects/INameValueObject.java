package org.jbei.ice.lib.value_objects;

import org.jbei.ice.lib.models.Entry;

public interface INameValueObject {

    public abstract void setId(int id);

    public abstract int getId();

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Entry getEntry();

    public abstract void setEntry(Entry entry);

}