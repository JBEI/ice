package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.client.bulkupload.sheet.cell.InputSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;

/**
 * Header for each sheet header
 *
 * @author Hector Plahar
 */
public class CellColumnHeader {
    private final Header headerType;
    private final String name;
    private final SheetCell cell;
    private final String description;

    public CellColumnHeader(Header headerType, boolean required, SheetCell cell, String description) {
        this.headerType = headerType;
        this.cell = cell;
        this.cell.setRequired(required);
        this.name = headerType.toString();
        this.description = description;
    }

    public CellColumnHeader(Header type, boolean required, String description) {
        this(type, required, new InputSheetCell(), description);
    }

    public CellColumnHeader(Header type, SheetCell cell) {
        this(type, false, cell, null);
    }

    public CellColumnHeader(Header type, boolean required) {
        this(type, required, new InputSheetCell(), null);
    }

    public CellColumnHeader(Header type) {
        this(type, false, new InputSheetCell(), null);
    }

    public SheetCell getCell() {
        return cell;
    }

    public boolean isRequired() {
        return this.cell.isRequired();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Header getHeaderType() {
        return headerType;
    }
}
