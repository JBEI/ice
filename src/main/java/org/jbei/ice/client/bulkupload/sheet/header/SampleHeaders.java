package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;

/**
 * @author Hector Plahar
 */
public abstract class SampleHeaders {

    protected ArrayList<CellColumnHeader> headers = new ArrayList<CellColumnHeader>();

    public SampleHeaders(ArrayList<String> locationList, HashMap<String, String> preferences) {
        for (String location : locationList) {
            try {
                EntryField header = EntryField.valueOf("SAMPLE_" + location.replaceAll(" ", "_").toUpperCase());
                headers.add(new CellColumnHeader(header, preferences));
            } catch (IllegalArgumentException ila) {
                headers.clear();
                return;
            }
        }
    }

    public CellColumnHeader getHeaderForIndex(int index) {
        return headers.get(index);
    }

    public ArrayList<CellColumnHeader> getHeaders() {
        return headers;
    }

    public int getHeaderSize() {
        return this.headers.size();
    }

    public abstract SheetCellData extractValue(EntryField headerType, EntryInfo info);

    SheetCellData extractCommon(EntryField headerType, EntryInfo info) {

        if (!info.isHasSample())
            return null;

        switch (headerType) {
            case SAMPLE_NAME:
                SampleStorage sampleStorage = info.getOneSampleStorage();
                String value = sampleStorage.getSample().getLabel();
                return new SheetCellData(headerType, value, value);

            default:
                return null;
        }
    }

    public void reset() {

        for (CellColumnHeader header : headers) {
            SheetCell cell = header.getCell();
            if (cell == null)
                continue;
            cell.reset();
        }
    }
}
