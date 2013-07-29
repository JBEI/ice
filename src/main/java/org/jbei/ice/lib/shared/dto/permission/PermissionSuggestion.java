package org.jbei.ice.lib.shared.dto.permission;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class PermissionSuggestion implements IsSerializable, Suggestion {

    private AccessPermission access;

    public PermissionSuggestion() {
    }

    public PermissionSuggestion(AccessPermission access) {
        this.access = access;
    }

    public AccessPermission getAccess() {
        return this.access;
    }

    @Override
    public String getDisplayString() {
        return access.getDisplay();
    }

    @Override
    public String getReplacementString() {
        return access.getDisplay();
    }
}
