package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Arabidopsis seed generation options
 *
 * @author Hector Plahar
 */
public enum Generation implements IDataTransferModel {

    UNKNOWN, F1, F2, F3, M0, M1, M2, T0, T1, T2, T3, T4, T5;

    public static Generation fromString(String value) {
        for (Generation option : Generation.values()) {
            if (value.equalsIgnoreCase(option.name()))
                return option;
        }
        return UNKNOWN;
    }
}