package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.Date;
import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.AutoCompleteSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.DateInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.GenerationSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.PlantTypeSheetCell;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.Generation;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlantType;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Arabidopsis seed header wrapper
 *
 * @author Hector Plahar
 */
public class ArabidopsisSeedHeaders extends PartHeader {

    public ArabidopsisSeedHeaders(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        super(delegate, preferences, EntryType.ARABIDOPSIS, EntryAddType.ARABIDOPSIS);
        headers.add(new CellColumnHeader(EntryField.SELECTION_MARKERS, preferences, false,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS)));
        headers.add(new CellColumnHeader(EntryField.HOMOZYGOSITY, preferences));
        headers.add(new CellColumnHeader(EntryField.HARVEST_DATE, preferences, false, new DateInputCell()));
        headers.add(new CellColumnHeader(EntryField.ECOTYPE, preferences));
        headers.add(new CellColumnHeader(EntryField.PARENTS, preferences));
        headers.add(new CellColumnHeader(EntryField.GENERATION, preferences, true, new GenerationSheetCell()));
        headers.add(new CellColumnHeader(EntryField.PLANT_TYPE, preferences, true, new PlantTypeSheetCell()));
        headers.add(new CellColumnHeader(EntryField.SENT_TO_ABRC, preferences, false, new BooleanSheetCell()));
    }

    @Override
    public SheetCellData extractValue(EntryField header, PartData info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        ArabidopsisSeedData seed = (ArabidopsisSeedData) info;
        String value = null;

        switch (header) {
            case HOMOZYGOSITY:
                value = seed.getHomozygosity();
                break;

            case ECOTYPE:
                value = seed.getEcotype();
                break;

            case HARVEST_DATE:
                Date harvestDate = seed.getHarvestDate();
                if (harvestDate == null)
                    value = "";
                else {
                    DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy");
                    value = format.format(harvestDate);
                }
                break;

            case PARENTS:
                value = seed.getParents();
                break;

            case GENERATION:
                Generation generation = seed.getGeneration();
                if (generation == null)
                    value = "";
                else
                    value = seed.getGeneration().name();
                break;

            case PLANT_TYPE:
                PlantType plantType = seed.getPlantType();
                if (plantType == null)
                    value = "";
                else
                    value = seed.getPlantType().toString();
                break;

            case SENT_TO_ABRC:
                if (seed.isSentToAbrc() == null)
                    value = "";
                else {
                    if (seed.isSentToAbrc())
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
