package org.jbei.ice.client.profile;

public class CellEntry {

    private MenuType type;
    private int count;

    public CellEntry(MenuType type, int count) {
        this.type = type;
        this.count = count;
    }

    public MenuType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }
}