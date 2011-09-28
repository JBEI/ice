package org.jbei.ice.shared;

import org.jbei.ice.client.MenuItem;

public class Folder extends MenuItem {

    private long id;

    public Folder() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
