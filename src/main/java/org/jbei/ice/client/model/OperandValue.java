package org.jbei.ice.client.model;

/**
 * Wrapper around enum options for insertion into a listbox
 *
 * @author Hector Plahar
 */

public class OperandValue {

    private final String display;
    private final String value;

    public OperandValue(String display, String value) {
        this.display = display;
        this.value = value;
    }

    public String getDisplay() {
        return this.display;
    }

    public String getValue() {
        return this.value;
    }

}
