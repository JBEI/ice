package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.models.Entry;

public interface IPartNumberValueObject {
    long getId();

    void setId(long id);

    String getPartNumber();

    void setPartNumber(String partNumber);

    Entry getEntry();

    void setEntry(Entry entry);
}