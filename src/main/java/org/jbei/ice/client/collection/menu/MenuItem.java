package org.jbei.ice.client.collection.menu;

public class MenuItem {

    private final long id;
    private String name;
    private long count;
    private final boolean isSystem;

    public MenuItem(long id, String name, long count, boolean isSystem) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.isSystem = isSystem;
    }

    public boolean isSystem() {
        return this.isSystem;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
