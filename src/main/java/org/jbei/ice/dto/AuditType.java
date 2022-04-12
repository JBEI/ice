package org.jbei.ice.dto;

/**
 * @author Hector Plahar
 */
public enum AuditType {

    READ("+r"),
    DELETE("d"),
    PERMANENTLY_DELETE("+d"),
    RESTORE("-d"),
    EDIT("e");

    private final String abbrev;

    AuditType(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getAbbrev() {
        return abbrev;
    }
}
