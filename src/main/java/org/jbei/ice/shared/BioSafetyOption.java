package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

public enum BioSafetyOption implements IsSerializable {

    LEVEL_ONE("Level 1", "1"),
    LEVEL_TWO("Level 2", "2");

    private String displayName;
    private String value;

    BioSafetyOption(String name, String value) {
        this.displayName = name;
        this.value = value;
    }

    BioSafetyOption() {
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean isValidOption(Integer integer) {
        for (BioSafetyOption option : BioSafetyOption.values()) {
            if (integer.intValue() == intValue(option.toString()).intValue())
                return true;
        }
        return false;
    }

    public static BioSafetyOption enumValue(Integer i) {
        for (BioSafetyOption option : BioSafetyOption.values()) {
            if (Integer.valueOf(option.value).intValue() == i.intValue())
                return option;
        }
        return null;
    }

    public static Integer intValue(String value) {
        for (BioSafetyOption option : BioSafetyOption.values()) {
            if (option.displayName.equalsIgnoreCase(value) || option.getValue().equals(value)) {
                return Integer.valueOf(option.getValue());
            }
        }
        return null;
    }

    public static ArrayList<String> getDisplayList() {
        ArrayList<String> list = new ArrayList<String>();
        for (BioSafetyOption option : BioSafetyOption.values()) {
            list.add(option.displayName);
        }
        return list;
    }

    public static BioSafetyOption displayToEnum(String value) {
        for (BioSafetyOption option : BioSafetyOption.values()) {
            if (value.equalsIgnoreCase(option.getDisplayName()))
                return option;
        }
        return null;
    }
}
