package org.jbei.ice.client.collection.presenter;

import java.util.List;

public class EntryContext {
    //    private IHasEntryId hasEntry;
    private List<Long> list;
    private long current;

    public EntryContext() {
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    //    public IHasEntryId getHasEntry() {
    //        return hasEntry;
    //    }
    //
    //    public void setHasEntry(IHasEntryId hasEntry) {
    //        this.hasEntry = hasEntry;
    //    }

    public List<Long> getList() {
        return list;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }
}
