package org.jbei.ice.lib.query;

public class SortField {
    private String name;
    private boolean isAscending;

    public SortField(String name, boolean isAscending) {
        this.name = name;
        this.isAscending = isAscending;
    }

    public String getName() {
        return name;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public String toString() {
        return name + " " + (this.isAscending ? "ASC" : "DESC");
    }
}
