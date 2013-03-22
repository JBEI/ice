package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

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

    /**
     * Extracts appropriate value from info using the header enum
     * sets the cell widget value using the index
     *
     * @param header
     * @param info
     * @return extracted value or null if none is found
     */
    public SheetCellData extractCommon(Header header, EntryInfo info) {
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

            case BIOSAFETY:
                BioSafetyOption option = BioSafetyOption.enumValue(info.getBioSafetyLevel());
                if (option != null)
                    value = option.toString();
                break;

            case NAME:
            case STRAIN_NAME:
                value = info.getName();
                break;

            case ALIAS:
            case STRAIN_ALIAS:
                value = info.getAlias();
                break;

            case KEYWORDS:
            case STRAIN_KEYWORDS:
                value = info.getKeywords();
                break;

            case SUMMARY:
            case STRAIN_SUMMARY:
                value = info.getShortDescription();
                break;

            case NOTES:
            case STRAIN_NOTES:
                value = info.getLongDescription();
                break;

            case REFERENCES:
            case STRAIN_REFERENCES:
                value = info.getReferences();
                break;

            case LINKS:
            case STRAIN_LINKS:
                value = info.getLinks();
                break;

            case STATUS:
            case STRAIN_STATUS:
                value = info.getStatus();
                break;

            case SELECTION_MARKERS:
            case STRAIN_SELECTION_MARKERS:
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

    public abstract SheetCellData extractValue(Header header, EntryInfo info);
}
