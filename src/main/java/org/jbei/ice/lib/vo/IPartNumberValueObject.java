package org.jbei.ice.lib.vo;

import org.jbei.ice.lib.models.Entry;

public interface IPartNumberValueObject {
    int getId();

    void setId(int id);

    String getPartNumber();

    void setPartNumber(String partNumber);

    Entry getEntry();

    void setEntry(Entry entry);
}