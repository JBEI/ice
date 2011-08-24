package org.jbei.ice.client;

import java.io.Serializable;

public class MenuItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public MenuItem() {

    }

    public MenuItem(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;

    }
}
