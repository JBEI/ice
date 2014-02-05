package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * Headers for part bulk upload
 *
 * @author Hector Plahar
 */
public class PartHeader extends BulkUploadHeaders {

    public PartHeader(EntryInfoDelegate delegate, HashMap<String, String> preferences, EntryType type,
            EntryAddType addType) {
        headers.add(new CellColumnHeader(EntryField.PI, preferences, true));
        headers.add(new CellColumnHeader(EntryField.FUNDING_SOURCE, preferences));
        headers.add(new CellColumnHeader(EntryField.IP, preferences));
        headers.add(new CellColumnHeader(EntryField.BIOSAFETY_LEVEL, preferences, true, new BioSafetySheetCell()));
        headers.add(new CellColumnHeader(EntryField.NAME, preferences, true, "Part Name e.g.pTSH117"));
        headers.add(new CellColumnHeader(EntryField.ALIAS, preferences, false, "Part Alias"));
        headers.add(new CellColumnHeader(EntryField.KEYWORDS, preferences, false));
        headers.add(new CellColumnHeader(EntryField.SUMMARY, preferences, true, "Short description of the part"));
        headers.add(new CellColumnHeader(EntryField.NOTES, preferences, false,
                                         "More details about the part, if available"));
        headers.add(new CellColumnHeader(EntryField.REFERENCES, preferences));
        headers.add(new CellColumnHeader(EntryField.LINKS, preferences, false, "Comma separated"));
        headers.add(new CellColumnHeader(EntryField.STATUS, preferences, true, new StatusSheetCell()));
        headers.add(new CellColumnHeader(EntryField.SEQ_FILENAME, preferences, false,
                                         new FileInputCell(true, false, delegate, addType, type),
                                         "Click on the cell to upload file"));
        headers.add(new CellColumnHeader(EntryField.ATT_FILENAME, preferences, false,
                                         new FileInputCell(false, false, delegate, addType, type),
                                         "Click on the cell to upload file"));
        headers.add(new CellColumnHeader(EntryField.SEQ_TRACE_FILES, preferences, false,
                                         new FileInputCell(false, true, delegate, addType, type)));
    }

    @Override
    public SheetCellData extractValue(EntryField header, PartData info) {
        return extractCommon(header, info);
    }
}
