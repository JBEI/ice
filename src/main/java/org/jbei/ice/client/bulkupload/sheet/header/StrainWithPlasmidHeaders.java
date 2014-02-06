package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.AutoCompleteSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

/**
 * Headers for strain with plasmid bulk upload sheet
 *
 * @author Hector Plahar
 */
public class StrainWithPlasmidHeaders extends BulkUploadHeaders {

    public StrainWithPlasmidHeaders(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        headers.add(new CellColumnHeader(EntryField.PI, preferences, true));
        headers.add(new CellColumnHeader(EntryField.FUNDING_SOURCE, preferences));
        headers.add(new CellColumnHeader(EntryField.IP, preferences));
        headers.add(new CellColumnHeader(EntryField.BIOSAFETY_LEVEL, preferences, true, new BioSafetySheetCell()));
        headers.add(new CellColumnHeader(EntryField.STATUS, preferences, true, new StatusSheetCell()));

        //strain information
        headers.add(new CellColumnHeader(EntryField.STRAIN_NAME, preferences, true, "e.g. JBEI-0001"));
        headers.add(new CellColumnHeader(EntryField.STRAIN_ALIAS, preferences));
        headers.add(new CellColumnHeader(EntryField.STRAIN_LINKS, preferences, false, "Comma separated list"));
        headers.add(new CellColumnHeader(EntryField.STRAIN_SELECTION_MARKERS, preferences, true,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS)));
        headers.add(new CellColumnHeader(EntryField.STRAIN_PARENTAL_STRAIN, preferences));
        headers.add(new CellColumnHeader(EntryField.STRAIN_GEN_PHEN, preferences));
        headers.add(new CellColumnHeader(EntryField.STRAIN_KEYWORDS, preferences));
        headers.add(new CellColumnHeader(EntryField.STRAIN_SUMMARY, preferences, true,
                                         "Short description of the strain"));
        headers.add(new CellColumnHeader(EntryField.STRAIN_NOTES, preferences, false,
                                         "More details about the strain, if available"));
        headers.add(new CellColumnHeader(EntryField.STRAIN_REFERENCES, preferences));
        headers.add(new CellColumnHeader(EntryField.STRAIN_SEQ_FILENAME, preferences, false,
                                         new FileInputCell(true, false, delegate, EntryAddType.STRAIN_WITH_PLASMID,
                                                           EntryType.STRAIN), "Click on the cell to upload file"));
        headers.add(new CellColumnHeader(EntryField.STRAIN_ATT_FILENAME, preferences, false,
                                         new FileInputCell(false, false, delegate, EntryAddType.STRAIN_WITH_PLASMID,
                                                           EntryType.STRAIN), "Click on the cell to upload file"));
        // plasmid information
        headers.add(new CellColumnHeader(EntryField.PLASMID_NAME, preferences, true, "e.g. pTSH117"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_ALIAS, preferences, false, "Part Alias"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_LINKS, preferences, false, "Comma separated list"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_SELECTION_MARKERS, preferences, true,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS),
                                         "Comma separated list"));
        headers.add(new CellColumnHeader(EntryField.CIRCULAR, preferences, false, new BooleanSheetCell(), "Yes or No"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_BACKBONE, preferences));
        headers.add(new CellColumnHeader(EntryField.PLASMID_PROMOTERS, preferences, false,
                                         new AutoCompleteSheetCell(AutoCompleteField.PROMOTERS),
                                         "Comma separated list"));
        headers.add(new CellColumnHeader(EntryField.REPLICATES_IN, preferences, false, "Comma separated list"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_ORIGIN_OF_REPLICATION, preferences, false,
                                         new AutoCompleteSheetCell(AutoCompleteField.ORIGIN_OF_REPLICATION),
                                         "Comma separated list"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_KEYWORDS, preferences));
        headers.add(new CellColumnHeader(EntryField.PLASMID_SUMMARY, preferences, true,
                                         "Short description of the plasmid"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_NOTES, preferences, false,
                                         "More details about the plasmid, if known"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_REFERENCES, preferences));
        headers.add(new CellColumnHeader(EntryField.PLASMID_SEQ_FILENAME, preferences, false,
                                         new FileInputCell(true, false, delegate, EntryAddType.STRAIN_WITH_PLASMID,
                                                           EntryType.PLASMID), "Click on the cell to upload file"));
        headers.add(new CellColumnHeader(EntryField.PLASMID_ATT_FILENAME, preferences, false,
                                         new FileInputCell(false, false, delegate, EntryAddType.STRAIN_WITH_PLASMID,
                                                           EntryType.PLASMID), "Click on the cell to upload file"));
    }

    public static boolean isPlasmidHeader(EntryField header) {
        return (header == EntryField.PLASMID_NAME
                || header == EntryField.PLASMID_ALIAS
                || header == EntryField.PLASMID_LINKS
                || header == EntryField.PLASMID_SELECTION_MARKERS
                || header == EntryField.CIRCULAR
                || header == EntryField.PLASMID_BACKBONE
                || header == EntryField.PLASMID_PROMOTERS
                || header == EntryField.PLASMID_REPLICATES_IN
                || header == EntryField.PLASMID_ORIGIN_OF_REPLICATION
                || header == EntryField.PLASMID_KEYWORDS
                || header == EntryField.PLASMID_SUMMARY
                || header == EntryField.PLASMID_NOTES
                || header == EntryField.PLASMID_REFERENCES
                || header == EntryField.PLASMID_SEQ_FILENAME
                || header == EntryField.PLASMID_ATT_FILENAME);
    }

    @Override
    public SheetCellData extractValue(EntryField header, PartData info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        data = extractCommon(header, info.getInfo());
        if (data != null)
            return data;

        StrainData strain = (StrainData) info;
        PlasmidData plasmid = (PlasmidData) info.getInfo();

        String value = null;
        switch (header) {
            case STRAIN_KEYWORDS:
                value = strain.getKeywords();
                break;

            case STRAIN_SUMMARY:
                value = strain.getShortDescription();
                break;

            case STRAIN_NOTES:
                value = strain.getLongDescription();
                break;

            case STRAIN_REFERENCES:
                value = strain.getReferences();
                break;

            case STRAIN_LINKS:
                value = strain.getLinks();
                break;

            case STRAIN_STATUS:
                value = strain.getStatus();
                break;

            case STRAIN_SELECTION_MARKERS:
                value = strain.getSelectionMarkers();
                break;

            case STRAIN_NAME:
                value = strain.getName();
                break;

            case STRAIN_ALIAS:
                value = strain.getAlias();
                break;

            case STRAIN_PARENTAL_STRAIN:
                value = strain.getHost();
                break;

            case STRAIN_GEN_PHEN:
                value = strain.getGenotypePhenotype();
                break;

            // plasmid specific
            case PLASMID_BACKBONE:
                value = plasmid.getBackbone();
                break;

            case PLASMID_ORIGIN_OF_REPLICATION:
                value = plasmid.getOriginOfReplication();
                break;

            case PLASMID_PROMOTERS:
                value = plasmid.getPromoters();
                break;

            case PLASMID_REPLICATES_IN:
                value = plasmid.getReplicatesIn();
                break;

            case CIRCULAR:
                if (plasmid.getCircular() == null)
                    value = "";
                else
                    value = plasmid.getCircular() ? "Yes" : "No";
                break;

            case PLASMID_NAME:
                value = plasmid.getName();
                break;

            case PLASMID_ALIAS:
                value = plasmid.getAlias();
                break;

            case PLASMID_KEYWORDS:
                value = plasmid.getKeywords();
                break;

            case PLASMID_SUMMARY:
                value = plasmid.getShortDescription();
                break;

            case PLASMID_NOTES:
                value = plasmid.getLongDescription();
                break;

            case PLASMID_REFERENCES:
                value = plasmid.getReferences();
                break;

            case PLASMID_LINKS:
                value = plasmid.getLinks();
                break;

            case PLASMID_STATUS:
                value = plasmid.getStatus();
                break;

            case PLASMID_SELECTION_MARKERS:
                value = plasmid.getSelectionMarkers();
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
