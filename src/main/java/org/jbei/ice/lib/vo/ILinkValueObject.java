package org.jbei.ice.lib.vo;

import org.jbei.ice.lib.models.Entry;

public interface ILinkValueObject {
    int getId();

    void setId(int id);

    String getLink();

    void setLink(String link);

    String getUrl();

    void setUrl(String url);

    Entry getEntry();

    void setEntry(Entry entry);
}