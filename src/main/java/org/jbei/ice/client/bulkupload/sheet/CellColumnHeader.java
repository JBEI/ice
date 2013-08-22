package org.jbei.ice.client.bulkupload.sheet;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.sheet.cell.InputSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

/**
 * Header for each sheet header
 *
 * @author Hector Plahar
 */
public class CellColumnHeader {

    private final EntryField headerType;
    private final String name;
    private final SheetCell cell;
    private final String description;
    private String defaultValue;
    private boolean locked;

    public CellColumnHeader(EntryField headerType, HashMap<String, String> preferences,
            boolean required, SheetCell cell, String description) {
        this.headerType = headerType;
        this.cell = cell;
        this.cell.setRequired(required);
        this.name = headerType.toString();
        this.description = description;

        String value = preferences == null ? null : preferences.get(headerType.toString().toUpperCase());
        if (value != null && !value.trim().isEmpty()) {
            this.setLocked(true);
            this.setDefaultValue(value);
        }
    }

    public CellColumnHeader(EntryField headerType, HashMap<String, String> preferences, boolean required,
            SheetCell cell) {
        this(headerType, preferences, required, cell, null);
    }

    public CellColumnHeader(EntryField type, HashMap<String, String> preferences, boolean required,
            String description) {
        this(type, preferences, required, new InputSheetCell(), description);
    }

    public CellColumnHeader(EntryField type, HashMap<String, String> preferences, SheetCell cell) {
        this(type, preferences, false, cell, null);
    }

    public CellColumnHeader(EntryField type, HashMap<String, String> preferences, boolean required) {
        this(type, preferences, required, new InputSheetCell(), null);
    }

    public CellColumnHeader(EntryField type, HashMap<String, String> preferences) {
        this(type, preferences, false, new InputSheetCell(), null);
    }

    public SheetCell getCell() {
        return cell;
    }

    public boolean isRequired() {
        return this.cell.isRequired();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public EntryField getHeaderType() {
        return headerType;
    }

    public boolean isCanLock() {
        return headerType.isCanLock();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return true if the default value of the header has been set indicating that this is locked
     *         Note that it differs from isCanLock() which simply indicates that this header supports locking
     */
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
