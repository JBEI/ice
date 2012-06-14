package org.jbei.ice.client.profile;

public class CellEntry {

    private MenuType type;
    private long count;

    public CellEntry(MenuType type, long count) {
        this.type = type;
        this.count = count;
    }

    public MenuType getType() {
        return type;
    }

    public long getCount() {
        return count;
    }
}