package org.jbei.ice.shared.dto.permission;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class PermissionSuggestion implements IsSerializable, Suggestion {

    private PermissionInfo info;

    public PermissionSuggestion() {
    }

    public PermissionSuggestion(PermissionInfo info) {
        this.info = info;
    }

    public PermissionInfo getInfo() {
        return this.info;
    }

    @Override
    public String getDisplayString() {
        return info.getDisplay();
    }

    @Override
    public String getReplacementString() {
        return info.getDisplay();
    }
}
