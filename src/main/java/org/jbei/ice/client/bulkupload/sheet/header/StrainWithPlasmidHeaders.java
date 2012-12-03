package org.jbei.ice.client.bulkupload.sheet.header;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;

/**
 * Headers for strain with plasmid sheet
 *
 * @author Hector Plahar
 */
public class StrainWithPlasmidHeaders extends BulkUploadHeaders {

    public StrainWithPlasmidHeaders() {
        headers.add(new CellColumnHeader(Header.PI, true));
        headers.add(new CellColumnHeader(Header.FUNDING_SOURCE));
        headers.add(new CellColumnHeader(Header.IP));
        headers.add(new CellColumnHeader(Header.BIOSAFETY, true, new BioSafetySheetCell(), null));
        headers.add(new CellColumnHeader(Header.STATUS, true, new StatusSheetCell(), null));

        //strain information
        headers.add(new CellColumnHeader(Header.STRAIN_NAME, true, "e.g. JBEI-0001"));
        headers.add(new CellColumnHeader(Header.STRAIN_ALIAS));
        headers.add(new CellColumnHeader(Header.STRAIN_LINKS));
        headers.add(new CellColumnHeader(Header.STRAIN_SELECTION_MARKERS, true, new MultiSuggestSheetCell(true), null));
        headers.add(new CellColumnHeader(Header.STRAIN_PARENTAL_STRAIN));
        headers.add(new CellColumnHeader(Header.STRAIN_GEN_PHEN));
        headers.add(new CellColumnHeader(Header.STRAIN_KEYWORDS));
        headers.add(new CellColumnHeader(Header.STRAIN_SUMMARY, true));
        headers.add(new CellColumnHeader(Header.STRAIN_NOTES));
        headers.add(new CellColumnHeader(Header.STRAIN_REFERENCES));
        headers.add(new CellColumnHeader(Header.STRAIN_SEQ_FILENAME, false, new FileInputCell(true), null));
        headers.add(new CellColumnHeader(Header.STRAIN_ATT_FILENAME, false, new FileInputCell(false), null));

        // plasmid information
        headers.add(new CellColumnHeader(Header.PLASMID_NAME, true, "e.g. pTSH117"));
        headers.add(new CellColumnHeader(Header.PLASMID_ALIAS));
        headers.add(new CellColumnHeader(Header.PLASMID_LINKS));
        headers.add(new CellColumnHeader(Header.PLASMID_SELECTION_MARKERS, true,
                                         new MultiSuggestSheetCell(true), null));
        headers.add(new CellColumnHeader(Header.CIRCULAR, false, new BooleanSheetCell(), null));
        headers.add(new CellColumnHeader(Header.PLASMID_BACKBONE));
        headers.add(new CellColumnHeader(Header.PLASMID_PROMOTERS, false, new MultiSuggestSheetCell(true), null));
        headers.add(new CellColumnHeader(Header.PLASMID_ORIGIN_OF_REPLICATION, false,
                                         new MultiSuggestSheetCell(true), null));
        headers.add(new CellColumnHeader(Header.PLASMID_KEYWORDS));
        headers.add(new CellColumnHeader(Header.PLASMID_SUMMARY, true));
        headers.add(new CellColumnHeader(Header.PLASMID_NOTES));
        headers.add(new CellColumnHeader(Header.PLASMID_REFERENCES));
        headers.add(new CellColumnHeader(Header.PLASMID_SEQ_FILENAME, false, new FileInputCell(true), null));
        headers.add(new CellColumnHeader(Header.PLASMID_ATT_FILENAME, false, new FileInputCell(false), null));
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
                value = plasmid.getReferences();
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
