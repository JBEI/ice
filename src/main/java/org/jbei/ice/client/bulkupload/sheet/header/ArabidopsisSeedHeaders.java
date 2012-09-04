package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.Date;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.DateInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.GenerationSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.PlantTypeSheetCell;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.core.client.GWT;

/**
 * @author Hector Plahar
 */
public class ArabidopsisSeedHeaders extends PartHeader {

    public ArabidopsisSeedHeaders() {
        super();

        headers.add(new CellColumnHeader(Header.HOMOZYGOSITY));
        headers.add(new CellColumnHeader(Header.HARVEST_DATE, false, new DateInputCell(), null));
        headers.add(new CellColumnHeader(Header.ECOTYPE));
        headers.add(new CellColumnHeader(Header.PARENTS));
        headers.add(new CellColumnHeader(Header.GENERATION, true, new GenerationSheetCell(), null));
        headers.add(new CellColumnHeader(Header.PLANT_TYPE, true, new PlantTypeSheetCell(), null));
        headers.add(new CellColumnHeader(Header.SELECTION_MARKERS, false, new MultiSuggestSheetCell(
                AppController.autoCompleteData.get(AutoCompleteField.SELECTION_MARKERS), true), null));
        headers.add(new CellColumnHeader(Header.SENT_TO_ABRC, false, new BooleanSheetCell(), null));
    }

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        ArabidopsisSeedInfo seed = (ArabidopsisSeedInfo) info;
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
                else
                    value = DateUtilities.formatDate(harvestDate);
                break;

            case PARENTS:
                value = seed.getParents();
                break;

            case GENERATION:
                ArabidopsisSeedInfo.Generation generation = seed.getGeneration();
                if (generation == null)
                    value = "";
                else
                    value = seed.getGeneration().name();
                break;

            case PLANT_TYPE:
                ArabidopsisSeedInfo.PlantType plantType = seed.getPlantType();
                if (plantType == null)
                    value = "";
                else
                    value = seed.getPlantType().toString();
                break;

            case SENT_TO_ABRC:
                if (seed.isSentToAbrc() == null)
                    value = "";
                else {
                    if (seed.isSentToAbrc().booleanValue())
                        value = "Yes";
                    else
                        value = "No";
                }
                break;

            // TODO : samples
            case SAMPLE_DRAWER:
                if (!info.isHasSample()) {
                    value = "";
                    break;
                }
                SampleStorage sampleStorage = info.getOneSampleStorage();
                break;

            case SAMPLE_BOX:
                if (!info.isHasSample()) {
                    value = "";
                    break;
                }
                GWT.log("Seed: Sample Box");
                break;

            case SAMPLE_TUBE:
                if (!info.isHasSample()) {
                    value = "";
                    break;
                }
                GWT.log("Seed: Sample Tube");
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
