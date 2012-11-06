package org.jbei.ice.client.bulkupload.sheet.header;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.StrainInfo;

/**
 * @author Hector Plahar
 */
public class StrainHeaders extends PartHeader {

    public StrainHeaders() {
        super();

        // strain specific headers
        headers.add(new CellColumnHeader(Header.SELECTION_MARKERS, false, new MultiSuggestSheetCell(
                AppController.autoCompleteData.get(AutoCompleteField.SELECTION_MARKERS), true), null));
        headers.add(new CellColumnHeader(Header.PARENTAL_STRAIN));
        headers.add(new CellColumnHeader(Header.GEN_PHEN));
        headers.add(new CellColumnHeader(
                Header.PLASMIDS,
                false,
                new MultiSuggestSheetCell(AppController.autoCompleteData.get(AutoCompleteField.PLASMID_NAME), true),
                null));
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
