package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum EntryMenu implements IsSerializable {

    MINE("My Entries"), ALL("All Entries"), RECENTLY_VIEWED("Recently Viewed"), SAMPLES("Samples"), WORKSPACE(
            "Workspace");

    private String display;

    EntryMenu(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }
}
