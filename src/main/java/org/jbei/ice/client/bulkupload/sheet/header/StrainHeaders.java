package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;

/**
 * @author Hector Plahar
 */
public class StrainHeaders extends PartHeader {

    public StrainHeaders(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        super(delegate, preferences);

        // strain specific headers
        headers.add(new CellColumnHeader(Header.PARENTAL_STRAIN, preferences));
        headers.add(new CellColumnHeader(Header.GEN_PHEN, preferences));
        headers.add(new CellColumnHeader(Header.PLASMIDS, preferences, false, new MultiSuggestSheetCell(true)));
    }

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        StrainInfo strain = (StrainInfo) info;
        String value = null;
        switch (header) {
            case PARENTAL_STRAIN:
                value = strain.getHost();
                break;

            case GEN_PHEN:
                value = strain.getGenotypePhenotype();
                break;

            case PLASMIDS:
                value = strain.getPlasmids();
                break;
        }

        if (value == null)
            return null;

        data = new SheetCellData();
        data.setId(value);
        data.setValue(value);
        return data;
    }
}
