package org.jbei.ice.shared.dto.permission;

import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class PermissionSuggestion implements IsSerializable, Suggestion {

    private PermissionType type;
    private String display;
    private long id;

    public PermissionSuggestion() {
    }

    public PermissionSuggestion(PermissionType type, long id, String display) {
        this.type = type;
        this.id = id;
        this.display = display;
    }

    public PermissionType getType() {
        return type;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public String getDisplayString() {
        /*
         * <span style="display: block; position: absolute; text-align:left">[KEY 
        GOES HERE]</span><span style="display: block; width: 100%; position: 
        relative; text-align:right">[VALUE HERE]</span> 
         */
        return display;
    }

    @Override
    public String getReplacementString() {
        return display;
    }
}
