package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;

/**
 * @author Hector Plahar
 */
public class PlasmidHeader extends PartHeader {

    public PlasmidHeader(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        super(delegate, preferences);

        // plasmid specific headers
        ArrayList<String> data = new ArrayList<String>();
        data.add("Yes");
        data.add("No");
        headers.add(new CellColumnHeader(Header.CIRCULAR, preferences, false, new BooleanSheetCell()));
        headers.add(new CellColumnHeader(Header.BACKBONE, preferences));
        headers.add(new CellColumnHeader(Header.PROMOTERS, preferences, false, new MultiSuggestSheetCell(true)));
        headers.add(new CellColumnHeader(Header.ORIGIN_OF_REPLICATION, preferences, false,
                                         new MultiSuggestSheetCell(true)));
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
                else {
                    if (plasmid.getCircular().booleanValue())
                        value = "Yes";
                    else
                        value = "No";
                }
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