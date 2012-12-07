package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.shared.dto.entry.EntryInfo;

/**
 * Headers for part bulk upload
 *
 * @author Hector Plahar
 */
public class PartHeader extends BulkUploadHeaders {

    public PartHeader(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        headers.add(new CellColumnHeader(Header.PI, preferences, true));
        headers.add(new CellColumnHeader(Header.FUNDING_SOURCE, preferences));
        headers.add(new CellColumnHeader(Header.IP, preferences));
        headers.add(new CellColumnHeader(Header.BIOSAFETY, preferences, true, new BioSafetySheetCell()));
        headers.add(new CellColumnHeader(Header.NAME, preferences, true));
        headers.add(new CellColumnHeader(Header.ALIAS, preferences));
        headers.add(new CellColumnHeader(Header.KEYWORDS, preferences));
        headers.add(new CellColumnHeader(Header.SUMMARY, preferences, true));
        headers.add(new CellColumnHeader(Header.NOTES, preferences));
        headers.add(new CellColumnHeader(Header.REFERENCES, preferences));
        headers.add(new CellColumnHeader(Header.LINKS, preferences));
        headers.add(new CellColumnHeader(Header.STATUS, preferences, true, new StatusSheetCell()));
        headers.add(new CellColumnHeader(Header.SEQ_FILENAME, preferences, false,
                                         new FileInputCell(true, delegate, false)));
        headers.add(new CellColumnHeader(Header.ATT_FILENAME, preferences, false,
                                         new FileInputCell(false, delegate, false)));
        headers.add(new CellColumnHeader(Header.SELECTION_MARKERS, preferences, false,
                                         new MultiSuggestSheetCell(true)));
    }

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        return extractCommon(header, info);
    }
}
