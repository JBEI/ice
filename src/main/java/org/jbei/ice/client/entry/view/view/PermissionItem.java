package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.collection.view.OptionSelect;

public class PermissionItem extends OptionSelect {

    private final boolean isGroup;
    private final boolean isWrite;

    public PermissionItem(long id, String name, boolean isGroup, boolean isWrite) {
        super(id, name);
        this.isGroup = isGroup;
        this.isWrite = isWrite;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isWrite() {
        return this.isWrite;
    }

    public boolean equals(PermissionItem item) {
        return getId() == item.getId() && (isGroup == item.isGroup) && (isWrite == item.isWrite);
    }
}
