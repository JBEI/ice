package org.jbei.ice.lib.dto.permission;

public class PermissionSuggestion {

    private AccessPermission access;

    public PermissionSuggestion() {
    }

    public PermissionSuggestion(AccessPermission access) {
        this.access = access;
    }

    public AccessPermission getAccess() {
        return this.access;
    }

    public String getDisplayString() {
        return access.getDisplay();
    }

    public String getReplacementString() {
        return access.getDisplay();
    }
}
