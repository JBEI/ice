package org.jbei.ice.lib.shared;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * BioSafety Option options
 *
 * @author Hector Plahar
 */
public enum BioSafetyOption implements IDataTransferModel {

    LEVEL_ONE("Level 1", "1"),
    LEVEL_TWO("Level 2", "2");

    private String displayName;
    private String value;

    BioSafetyOption(String name, String value) {
        this.displayName = name;
        this.value = value;
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
        if (integer == null)
            return false;

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
}
