package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;

import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

/**
 * Parent class for extracting values from an EntryInfo. This is used in the cases
 * when an existing bulk import is being used to populate the sheet
 *
 * @author Hector Plahar
 */
abstract class InfoValueExtractor {

    /**
     * Extracts data for common field represented by header. This is expected to be called by all
     * child classes to retrieve data. If null is returned, then the header represents a child field
     * which should be handled by that sub-class.
     * <p/>
     * The header data structure is also set using the values (and ids if applicable) that are
     * extracted
     *
     * @param header current Header under consideration (also represents the column)
     * @param info   entry data
     * @param index  represents row
     * @return extracted common field or null
     */
    protected String extractCommon(Header header, EntryInfo info, int index) {

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
                BioSafetyOptions option = BioSafetyOptions.enumValue(info.getBioSafetyLevel());
                if (option != null)
                    value = option.toString();
                break;

            case NAME:
            case STRAIN_NAME:
            case PLASMID_NAME:
                value = info.getName();
                break;

            case ALIAS:
            case STRAIN_ALIAS:
            case PLASMID_ALIAS:
                value = info.getAlias();
                break;

            case KEYWORDS:
            case STRAIN_KEYWORDS:
            case PLASMID_KEYWORDS:
                value = info.getKeywords();
                break;

            case SUMMARY:
            case STRAIN_SUMMARY:
            case PLASMID_SUMMARY:
                value = info.getShortDescription();
                break;

            case NOTES:
            case STRAIN_NOTES:
            case PLASMID_NOTES:
                value = info.getLongDescription();
                break;

            case REFERENCES:
            case STRAIN_REFERENCES:
            case PLASMID_REFERENCES:
                value = info.getReferences();
                break;

            case LINKS:
            case STRAIN_LINKS:
            case PLASMID_LINKS:
                value = info.getLinks();
                break;

            case STATUS:
            case STRAIN_STATUS:
            case PLASMID_STATUS:
                value = info.getStatus();
                break;

            case SELECTION_MARKERS:
            case STRAIN_SELECTION_MARKERS:
            case PLASMID_SELECTION_MARKERS:
                value = info.getSelectionMarkers();
                break;

            case ATT_FILENAME:
            case STRAIN_ATT_FILENAME:
            case PLASMID_ATT_FILENAME:
                ArrayList<AttachmentInfo> attInfos = info.getAttachments();
                if (attInfos == null || attInfos.isEmpty())
                    break;

                // currently support upload of a single attachment only
                AttachmentInfo attInfo = attInfos.get(0);
                value = attInfo.getFilename();
                id = attInfo.getFileId();
                break;

            case SEQ_FILENAME:
            case STRAIN_SEQ_FILENAME:
            case PLASMID_SEQ_FILENAME:
                ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
                if (sequenceInfos == null || sequenceInfos.isEmpty())
                    break;

                // currently support upload of a single sequence only
                SequenceAnalysisInfo seqInfo = sequenceInfos.get(0);
                value = seqInfo.getName();
                id = seqInfo.getFileId();
                break;
        }

        if ((value == null || value.isEmpty()) && id == null)
            return null;

        SheetCell cell = header.getCell();
        cell.setWidgetValue(index, value, id);
        return value;
    }
}
