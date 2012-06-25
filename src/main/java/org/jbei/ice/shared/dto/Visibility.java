package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Hector Plahar
 */
public enum Visibility implements IsSerializable {

    DRAFT(0), PENDING(1), OK(9);

    private final int value;

    Visibility() {
        this.value = 9;
    }

    Visibility(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static Visibility valueToEnum(Integer value) {
        // this is for legacy reasons. Visibility was abandoned for a while
        if (value == null)
            return OK;

        switch (value) {
            case 0:
                return DRAFT;

            case 1:
                return PENDING;

            case 9:
            default:
                return OK;
        }
    }
}
