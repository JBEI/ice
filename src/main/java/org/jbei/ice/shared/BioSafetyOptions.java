package org.jbei.ice.shared;

import java.util.ArrayList;

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

    public static Integer intValue(String value) {
        for (BioSafetyOptions option : BioSafetyOptions.values()) {
            if (option.displayName.equals(value) || option.getValue().equals(value)) {
                return Integer.valueOf(option.getValue());

            }
        }
        return null;
    }

    public static ArrayList<String> getDisplayList() {
        ArrayList<String> list = new ArrayList<String>();
        for (BioSafetyOptions option : BioSafetyOptions.values()) {
            list.add(option.displayName);
        }
        return list;
    }
}
