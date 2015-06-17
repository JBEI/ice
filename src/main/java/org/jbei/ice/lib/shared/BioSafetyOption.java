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

    @Override
    public String toString() {
        return this.displayName;
    }

    public String getValue() {
        return this.value;
    }

    public int getIntValue() {
        return Integer.valueOf(this.value);
    }

    public static boolean isValidOption(Integer integer) {
        if (integer == null)
            return false;

        for (BioSafetyOption option : BioSafetyOption.values()) {
            Integer intValue = intValue(option.toString());
            if (intValue != null && integer.intValue() == intValue)
                return true;
        }
        return false;
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
