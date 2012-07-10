package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;

import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

class InfoValueExtractor {

    public static String extractCommon(Header header, EntryInfo info, int index) {
        switch (header) {
            case PI:
                return info.getPrincipalInvestigator();

            case FUNDING_SOURCE:
                return info.getFundingSource();

            case IP:
                return info.getIntellectualProperty();

            case BIOSAFETY:
                BioSafetyOptions option = BioSafetyOptions.enumValue(info.getBioSafetyLevel());
                if (option == null)
                    return "";
                return option.toString();

            case NAME:
                return info.getName();

            case ALIAS:
                return info.getAlias();

            case KEYWORDS:
                return info.getKeywords();

            case SUMMARY:
                return info.getShortDescription();

            case NOTES:
                return info.getLongDescription();

            case REFERENCES:
                return info.getReferences();

            case LINKS:
                return info.getLinks();

            case STATUS:
                return info.getStatus();

            case SELECTION_MARKERS:
                return info.getSelectionMarkers();

            // special case for files
            case ATT_FILENAME:
                ArrayList<AttachmentInfo> attInfos = info.getAttachments();
                if (attInfos == null || attInfos.isEmpty() || index >= attInfos.size())
                    return "";

                AttachmentInfo attInfo = attInfos.get(index);
                if (attInfo == null)
                    return "";

                return attInfo.getFilename();

            case SEQ_FILENAME:
                ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
                if (sequenceInfos == null || sequenceInfos.isEmpty() || index >= sequenceInfos.size())
                    return "";

                SequenceAnalysisInfo seqInfo = sequenceInfos.get(index);
                if (seqInfo == null)
                    return "";

                return seqInfo.getName();

            default:
                return null;
        }
    }
}
