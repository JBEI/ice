package org.jbei.ice.client.bulkupload.sheet.header;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;

/**
 * @author Hector Plahar
 */
public class PlasmidHeader extends PartHeader {

    public PlasmidHeader() {
        super();

        // plasmid specific headers
        headers.add(new CellColumnHeader(Header.SELECTION_MARKERS, false, new MultiSuggestSheetCell(
                AppController.autoCompleteData.get(AutoCompleteField.SELECTION_MARKERS)), null));
        headers.add(new CellColumnHeader(Header.CIRCULAR));
        headers.add(new CellColumnHeader(Header.BACKBONE));
        headers.add(new CellColumnHeader(Header.PROMOTERS, false, new MultiSuggestSheetCell(
                AppController.autoCompleteData.get(
                        AutoCompleteField.PROMOTERS)), null));
        headers.add(new CellColumnHeader(Header.ORIGIN_OF_REPLICATION, false, new MultiSuggestSheetCell(
                AppController.autoCompleteData.get(AutoCompleteField.ORIGIN_OF_REPLICATION)), null));
    }

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        PlasmidInfo plasmid = (PlasmidInfo) info;
        String value = null;

        switch (header) {
            // plasmid specific
            case BACKBONE:
                value = plasmid.getBackbone();
                break;

            case ORIGIN_OF_REPLICATION:
                value = plasmid.getOriginOfReplication();
                break;

            case PROMOTERS:
                value = plasmid.getPromoters();
                break;

            case CIRCULAR:
                if (plasmid.getCircular() == null)
                    value = "";
                else
                    value = Boolean.toString(plasmid.getCircular());
                break;
        }

        if (value == null)
            return null;

        data = new SheetCellData();
        data.setValue(value);
        data.setId(value);
        return data;
    }
}