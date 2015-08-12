package org.jbei.ice.lib.dto.bulkupload;

/**
 * Complete list of storage location fields for samples
 *
 * @author Hector Plahar
 */
public enum SampleField {
    SHELF,
    BOX,
    WELL,
    TUBE,
    LABEL;

    public static SampleField fromString(String string) {
        for (SampleField field : SampleField.values()) {
            if (string.equalsIgnoreCase(field.name()))
                return field;
        }

        return null;
    }
}
