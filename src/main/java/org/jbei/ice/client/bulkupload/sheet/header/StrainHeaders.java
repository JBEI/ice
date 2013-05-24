package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.AutoCompleteSheetCell;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.bulkupload.EntryField;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.StrainInfo;

/**
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
    public SheetCellData extractValue(EntryField header, EntryInfo info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        StrainInfo strain = (StrainInfo) info;
        String value = null;
        switch (header) {
            case PARENTAL_STRAIN:
                value = strain.getHost();
                break;

            case GENOTYPE_OR_PHENOTYPE:
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
