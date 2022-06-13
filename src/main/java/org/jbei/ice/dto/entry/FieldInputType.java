package org.jbei.ice.dto.entry;

/**
 * Provides an indication of the expected input type for specific fields. e.g. regular text or boolean etc
 *
 * @author Hector Plahar
 */
public enum FieldInputType {
    BOOLEAN,
    TEXT,
    MULTI_TEXT,
    LONG_TEXT,
    TEXTAREA,
    DATE,
    FILE,
    SELECT,
    USER_WITH_EMAIL     // todo : better naming
}
