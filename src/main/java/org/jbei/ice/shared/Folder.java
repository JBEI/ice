package org.jbei.ice.shared;

import org.jbei.ice.client.MenuItem;

public class Folder extends MenuItem {

    private static final long serialVersionUID = 1L;

    private long id;

    public Folder() {
        super("");
    }

    public Folder(String name, int id) {
        super(name);
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

}
