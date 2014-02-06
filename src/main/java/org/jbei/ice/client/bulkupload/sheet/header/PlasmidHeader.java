package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.AutoCompleteSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;

/**
 * @author Hector Plahar
 */
public class PlasmidHeader extends PartHeader {

    public PlasmidHeader(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        super(delegate, preferences, EntryType.PLASMID, EntryAddType.PLASMID);

        // plasmid specific headers
        headers.add(new CellColumnHeader(EntryField.CIRCULAR, preferences, false, new BooleanSheetCell(), "Yes or No"));
        headers.add(new CellColumnHeader(EntryField.BACKBONE, preferences));
        headers.add(new CellColumnHeader(EntryField.PROMOTERS, preferences, false, new AutoCompleteSheetCell(
                AutoCompleteField.PROMOTERS), "Comma separated"));
        headers.add(new CellColumnHeader(EntryField.REPLICATES_IN, preferences, false, "Comma separated"));
        headers.add(new CellColumnHeader(EntryField.ORIGIN_OF_REPLICATION, preferences, false,
                                         new AutoCompleteSheetCell(AutoCompleteField.ORIGIN_OF_REPLICATION),
                                         "Comma separated"));
        headers.add(new CellColumnHeader(EntryField.SELECTION_MARKERS, preferences, true,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS),
                                         "Comma separated"));
    }

    @Override
    public SheetCellData extractValue(EntryField header, PartData info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        PlasmidData plasmid = (PlasmidData) info;
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

            case REPLICATES_IN:
                value = plasmid.getReplicatesIn();
                break;

            case CIRCULAR:
                if (plasmid.getCircular() == null)
                    value = "";
                else
                    value = plasmid.getCircular() ? "Yes" : "No";
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