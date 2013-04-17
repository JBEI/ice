package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.AutoCompleteSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;

/**
 * Headers for strain with plasmid sheet
 *
 * @author Hector Plahar
 */
public class StrainWithPlasmidHeaders extends BulkUploadHeaders {

    public StrainWithPlasmidHeaders(EntryInfoDelegate delegate, HashMap<String, String> preferences) {
        headers.add(new CellColumnHeader(Header.PI, preferences, true));
        headers.add(new CellColumnHeader(Header.FUNDING_SOURCE, preferences));
        headers.add(new CellColumnHeader(Header.IP, preferences));
        headers.add(new CellColumnHeader(Header.BIOSAFETY, preferences, true, new BioSafetySheetCell()));
        headers.add(new CellColumnHeader(Header.STATUS, preferences, true, new StatusSheetCell()));

        //strain information
        headers.add(new CellColumnHeader(Header.STRAIN_NAME, preferences, true, "e.g. JBEI-0001"));
        headers.add(new CellColumnHeader(Header.STRAIN_ALIAS, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_LINKS, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_SELECTION_MARKERS, preferences, true,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS)));
        headers.add(new CellColumnHeader(Header.STRAIN_PARENTAL_STRAIN, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_GEN_PHEN, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_KEYWORDS, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_SUMMARY, preferences, true));
        headers.add(new CellColumnHeader(Header.STRAIN_NOTES, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_REFERENCES, preferences));
        headers.add(new CellColumnHeader(Header.STRAIN_SEQ_FILENAME, preferences, false,
                                         new FileInputCell(true, delegate, false)));
        headers.add(new CellColumnHeader(Header.STRAIN_ATT_FILENAME, preferences, false,
                                         new FileInputCell(false, delegate, false)));

        // plasmid information
        headers.add(new CellColumnHeader(Header.PLASMID_NAME, preferences, true, "e.g. pTSH117"));
        headers.add(new CellColumnHeader(Header.PLASMID_ALIAS, preferences));
        headers.add(new CellColumnHeader(Header.PLASMID_LINKS, preferences));
        headers.add(new CellColumnHeader(Header.PLASMID_SELECTION_MARKERS, preferences, true,
                                         new AutoCompleteSheetCell(AutoCompleteField.SELECTION_MARKERS)));
        headers.add(new CellColumnHeader(Header.CIRCULAR, preferences, false, new BooleanSheetCell()));
        headers.add(new CellColumnHeader(Header.PLASMID_BACKBONE, preferences));
        headers.add(new CellColumnHeader(Header.PLASMID_PROMOTERS, preferences, false,
                                         new AutoCompleteSheetCell(AutoCompleteField.PROMOTERS)));
        headers.add(new CellColumnHeader(Header.PLASMID_ORIGIN_OF_REPLICATION, preferences, false,
                                         new AutoCompleteSheetCell(AutoCompleteField.ORIGIN_OF_REPLICATION)));
        headers.add(new CellColumnHeader(Header.PLASMID_KEYWORDS, preferences));
        headers.add(new CellColumnHeader(Header.PLASMID_SUMMARY, preferences, true));
        headers.add(new CellColumnHeader(Header.PLASMID_NOTES, preferences));
        headers.add(new CellColumnHeader(Header.PLASMID_REFERENCES, preferences));
        headers.add(new CellColumnHeader(Header.PLASMID_SEQ_FILENAME, preferences, false,
                                         new FileInputCell(true, delegate, true)));
        headers.add(new CellColumnHeader(Header.PLASMID_ATT_FILENAME, preferences, false,
                                         new FileInputCell(false, delegate, true)));
    }

    public static boolean isPlasmidHeader(Header header) {
        return (header == Header.PLASMID_NAME
                || header == Header.PLASMID_ALIAS
                || header == Header.PLASMID_LINKS
                || header == Header.PLASMID_SELECTION_MARKERS
                || header == Header.CIRCULAR
                || header == Header.PLASMID_BACKBONE
                || header == Header.PLASMID_PROMOTERS
                || header == Header.PLASMID_ORIGIN_OF_REPLICATION
                || header == Header.PLASMID_KEYWORDS
                || header == Header.PLASMID_SUMMARY
                || header == Header.PLASMID_NOTES
                || header == Header.PLASMID_REFERENCES
                || header == Header.PLASMID_SEQ_FILENAME
                || header == Header.PLASMID_ATT_FILENAME);
    }

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;


        data = extractCommon(header, info.getInfo());
        if (data != null)
            return data;

        StrainInfo strain = (StrainInfo) info;
        PlasmidInfo plasmid = (PlasmidInfo) info.getInfo();

        String value = null;
        switch (header) {
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
