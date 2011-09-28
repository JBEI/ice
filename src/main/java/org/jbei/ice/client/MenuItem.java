package org.jbei.ice.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MenuItem implements IsSerializable {

    private String name;

    public MenuItem() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
