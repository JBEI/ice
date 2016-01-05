package org.jbei.ice.lib.shared;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * BioSafety Option options
 *
 * @author Hector Plahar
 */
public enum BioSafetyOption implements IDataTransferModel {

    LEVEL_ONE("1"),
    LEVEL_TWO("2");

    private String value;

    BioSafetyOption(String value) {
        this.value = value;
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
            if (option.getIntValue() == integer)
                return true;
        }
        return false;
    }

    public static Integer intValue(String value) {
        for (BioSafetyOption option : BioSafetyOption.values()) {
            if (option.value.equalsIgnoreCase(value) || option.getValue().equals(value)) {
                return Integer.valueOf(option.getValue());
            }
        }
        return null;
    }
}
