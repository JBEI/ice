package org.jbei.ice.shared.dto.permission;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PermissionInfo implements IsSerializable {

    private PermissionType type;
    private long id;
    private String display;

    public PermissionInfo() {
    }

    public PermissionInfo(PermissionType type, long id, String display) {
        this.type = type;
        this.id = id;
        this.display = display;
    }

    public PermissionType getType() {
        return type;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public enum PermissionType implements IsSerializable {
        READ_ACCOUNT, WRITE_ACCOUNT, READ_GROUP, WRITE_GROUP;
    }
}
