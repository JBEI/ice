package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.collection.view.OptionSelect;

public class MenuItem extends OptionSelect {

    private long count;
    private final boolean isSystem;

    public MenuItem(long id, String name, long count, boolean isSystem) {
        super(id, name);
        this.count = count;
        this.isSystem = isSystem;
    }

    public boolean isSystem() {
        return this.isSystem;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
