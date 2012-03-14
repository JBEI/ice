package org.jbei.ice.client.collection.presenter;

import java.util.List;

import org.jbei.ice.shared.ColumnField;

public class EntryContext {
    private List<Long> list;
    private long current;
    private Type type;
    private ColumnField sortColumn;
    private boolean asc;

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

    public ColumnField getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(ColumnField sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public enum Type {
        SEARCH, COLLECTION, SAMPLES;
    }
}
