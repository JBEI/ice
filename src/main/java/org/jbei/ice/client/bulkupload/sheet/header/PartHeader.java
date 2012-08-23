package org.jbei.ice.client.bulkupload.sheet.header;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * Headers for part bulk upload
 *
 * @author Hector Plahar
 */
public class PartHeader extends BulkUploadHeaders {

    public PartHeader() {
        headers.add(new CellColumnHeader(Header.PI, true));
        headers.add(new CellColumnHeader(Header.FUNDING_SOURCE));
        headers.add(new CellColumnHeader(Header.IP));
        headers.add(new CellColumnHeader(Header.BIOSAFETY, true, new BioSafetySheetCell(), null));
        headers.add(new CellColumnHeader(Header.NAME, true));
        headers.add(new CellColumnHeader(Header.ALIAS));
        headers.add(new CellColumnHeader(Header.KEYWORDS));
        headers.add(new CellColumnHeader(Header.SUMMARY, true));
        headers.add(new CellColumnHeader(Header.NOTES));
        headers.add(new CellColumnHeader(Header.REFERENCES));
        headers.add(new CellColumnHeader(Header.LINKS));
        headers.add(new CellColumnHeader(Header.STATUS, true, new StatusSheetCell(), null));
        headers.add(new CellColumnHeader(Header.SEQ_FILENAME, false, new FileInputCell(true), null));
        headers.add(new CellColumnHeader(Header.ATT_FILENAME, false, new FileInputCell(false), null));
    }

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        return extractCommon(header, info);
    }
}
