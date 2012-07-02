package org.jbei.ice.lib.entry;

/**
 * @author Hector Plahar
 */
public enum Visibility {

    NORMAL(9), DRAFT(0), PENDING(1);

    private Integer integer;

    Visibility(int intValue) {
        integer = new Integer(intValue);
    }

    public Integer getVisibilityInt() {
        return integer;
    }
}
