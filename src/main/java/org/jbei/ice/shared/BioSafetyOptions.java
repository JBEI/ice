package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum BioSafetyOptions implements IsSerializable {

    LEVEL_ONE("Level 1", "1"), LEVEL_TWO("Level 2", "2");

    private String displayName;
    private String value;

    private BioSafetyOptions(String name, String value) {
        this.displayName = name;
        this.value = value;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getValue() {
        return this.value;
    }
}
