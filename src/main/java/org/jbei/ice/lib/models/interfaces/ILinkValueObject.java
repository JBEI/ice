package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.models.Entry;

public interface ILinkValueObject {
    long getId();

    void setId(long id);

    String getLink();

    void setLink(String link);

    String getUrl();

    void setUrl(String url);

    Entry getEntry();

    void setEntry(Entry entry);
}