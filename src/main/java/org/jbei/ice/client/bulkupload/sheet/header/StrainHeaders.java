package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.AutoCompleteSheetCell;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

/**
 * Headers for strain bulk upload in addition to the common part headers.
 *
 * @author Hector Plahar
 */
public class StrainHeaders extends PartHeader {

    public StrainHeaders(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        super(delegate, preferences, EntryType.STRAIN, EntryAddType.STRAIN);

        // strain specific headers
        headers.add(new CellColumnHeader(EntryField.PARENTAL_STRAIN, preferences));
        headers.add(new CellColumnHeader(EntryField.GENOTYPE_OR_PHENOTYPE, preferences));
        headers.add(new CellColumnHeader(EntryField.PLASMIDS, preferences, false, new AutoCompleteSheetCell(
                AutoCompleteField.PLASMID_NAME)));
        headers.add(new CellColumnHeader(EntryField.SELECTION_MARKERS, preferences, true,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS)));
    }

    @Override
    public SheetCellData extractValue(EntryField header, PartData info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        StrainData strain = (StrainData) info;
        String value = null;
        switch (header) {
            case PARENTAL_STRAIN:
                value = strain.getHost();
                break;

            case GENOTYPE_OR_PHENOTYPE:
                value = strain.getGenotypePhenotype();
                break;

            case PLASMIDS:
                value = "";
                for (int i = 0; i < strain.getLinkedParts().size(); i += 1) {
                    if (i > 0)
                        value += ", ";
                    value += strain.getLinkedParts().get(i).getPartId();
                }
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
