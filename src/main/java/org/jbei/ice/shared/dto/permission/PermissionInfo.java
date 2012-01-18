package org.jbei.ice.shared.dto.permission;

public class PermissionInfo {

    private final PermissionType type;
    private final long id;
    private final String display;

    public PermissionInfo(PermissionType type, long id, String display) {
        this.type = type;
        this.id = id;
        this.display = display;
    }

    public PermissionType getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public enum PermissionType {
        READ_ACCOUNT, WRITE_ACCOUNT, READ_GROUP, WRITE_GROUP;
    }
}
