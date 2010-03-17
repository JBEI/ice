package org.jbei.ice.lib.vo;

import org.jbei.ice.lib.models.Entry;

public interface ILinkValueObject {

    public abstract int getId();

    public abstract void setId(int id);

    public abstract String getLink();

    public abstract void setLink(String link);

    public abstract String getUrl();

    public abstract void setUrl(String url);

    public abstract Entry getEntry();

    public abstract void setEntry(Entry entry);

}