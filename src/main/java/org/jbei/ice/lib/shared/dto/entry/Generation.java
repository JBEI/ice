package org.jbei.ice.lib.shared.dto.entry;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Arabidopsis seed generation
 *
 * @author Hector Plahar
 */
public enum Generation implements IDTOModel {
    UNKNOWN, F1, F2, F3, M0, M1, M2, T0, T1, T2, T3, T4, T5;

    public static ArrayList<String> getDisplayList() {
        ArrayList<String> list = new ArrayList<String>();
        for (Generation option : Generation.values()) {
            list.add(option.name());
        }
        return list;
    }

    public static Generation fromString(String value) {
        for (Generation option : Generation.values()) {
            if (value.equalsIgnoreCase(option.name()))
                return option;
        }
        return UNKNOWN;
    }
}