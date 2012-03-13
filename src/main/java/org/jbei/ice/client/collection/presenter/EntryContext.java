package org.jbei.ice.client.collection.presenter;

import java.util.List;

public class EntryContext {
    private List<Long> list;
    private long current;
    private Type type;

    public EntryContext(Type type) {
        this.setType(type);
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public List<Long> getList() {
        return list;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        SEARCH, COLLECTION, SAMPLES;
    }
}
