package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Arabidopsis seed plant type
 *
 * @author Hector Plahar
 */
public enum PlantType implements IDataTransferModel {

    EMS("EMS"),
    OVER_EXPRESSION("Over Expression"),
    RNAI("RNAi"),
    REPORTER("Reporter"),
    T_DNA("T-DNA"),
    OTHER("Other"),
    NULL("");

    private String display;

    PlantType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }

    public static PlantType fromString(String value) {
        for (PlantType option : PlantType.values()) {
            if (value.equalsIgnoreCase(option.toString()) || value.equalsIgnoreCase(option.name()))
                return option;
        }
        return NULL;
    }
}

