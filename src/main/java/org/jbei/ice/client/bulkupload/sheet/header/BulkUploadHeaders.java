package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

/**
 * @author Hector Plahar
 */
public abstract class BulkUploadHeaders {
    protected ArrayList<CellColumnHeader> headers = new ArrayList<CellColumnHeader>();

    public ArrayList<CellColumnHeader> getHeaders() {
        return headers;
    }

    public int getHeaderSize() {
        return this.headers.size();
    }

    public CellColumnHeader getHeaderForIndex(int index) {
        return headers.get(index);
    }

    public void reset() {
        for (CellColumnHeader header : headers) {
            SheetCell cell = header.getCell();
            if (cell == null)
                continue;
            cell.reset();
        }
    }

    /**
     * Extracts appropriate value from info using the header enum
     * sets the cell widget value using the index
     *
     * @param header header type used to indicate the type of data cells in column support
     * @param info   part data
     * @return extracted value or null if none is found
     */
    public SheetCellData extractCommon(EntryField header, PartData info) {
        String value = null;
        String id = null;

        switch (header) {
            case PI:
                value = info.getPrincipalInvestigator();
                break;

            case FUNDING_SOURCE:
                value = info.getFundingSource();
                break;

            case IP:
                value = info.getIntellectualProperty();
                break;

            case BIOSAFETY_LEVEL:
                BioSafetyOption option = BioSafetyOption.enumValue(info.getBioSafetyLevel());
                if (option != null)
                    value = option.toString();
                break;

            case NAME:
                value = info.getName();
                break;

            case ALIAS:
                value = info.getAlias();
                break;

            case KEYWORDS:
                value = info.getKeywords();
                break;

            case SUMMARY:
                value = info.getShortDescription();
                break;

            case NOTES:
                value = info.getLongDescription();
                break;

            case REFERENCES:
                value = info.getReferences();
                break;

            case LINKS:
                value = info.getLinks();
                break;

            case STATUS:
                value = info.getStatus();
                break;

            case SELECTION_MARKERS:
                value = info.getSelectionMarkers();
                break;

            case ATT_FILENAME:
            case STRAIN_ATT_FILENAME:
                ArrayList<AttachmentInfo> attInfos = info.getAttachments();
                if (attInfos == null || attInfos.isEmpty()) {
                    value = "";
                    break;
                }

                // currently support upload of a single attachment only
                AttachmentInfo attInfo = attInfos.get(0);
                value = attInfo.getFilename();
                id = attInfo.getFileId();
                break;

            case PLASMID_ATT_FILENAME:
                ArrayList<AttachmentInfo> plasmidAttachmentList = info.getInfo().getAttachments();
                if (plasmidAttachmentList == null || plasmidAttachmentList.isEmpty()) {
                    value = "";
                    break;
                }

                // currently support upload of a single attachment only
                AttachmentInfo plasmidAttachmentInfo = plasmidAttachmentList.get(0);
                value = plasmidAttachmentInfo.getFilename();
                id = plasmidAttachmentInfo.getFileId();
                break;

            case SEQ_FILENAME:
            case STRAIN_SEQ_FILENAME:
                ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
                if (sequenceInfos == null || sequenceInfos.isEmpty()) {
                    value = "";
                    if (info.isHasSequence())
                        value = "has sequence";
                    break;
                }

                // currently support upload of a single sequence only
                SequenceAnalysisInfo seqInfo = sequenceInfos.get(0);
                value = seqInfo.getName();
                id = seqInfo.getFileId();
                break;

            case PLASMID_SEQ_FILENAME:
                ArrayList<SequenceAnalysisInfo> plasmidSequenceList = info.getInfo().getSequenceAnalysis();
                if (plasmidSequenceList == null || plasmidSequenceList.isEmpty()) {
                    value = "";
                    if (info.getInfo().isHasSequence())
                        value = "has sequence";
                    break;
                }

                // currently support upload of a single sequence only
                SequenceAnalysisInfo plasmidSequenceInfo = plasmidSequenceList.get(0);
                value = plasmidSequenceInfo.getName();
                id = plasmidSequenceInfo.getFileId();
                break;
        }

        SheetCellData data = null;
        if (value != null) {
            if (id == null)
                id = value;
            data = new SheetCellData(header, id, value);
        }

        return data;
    }

    public abstract SheetCellData extractValue(EntryField header, PartData info);
}
