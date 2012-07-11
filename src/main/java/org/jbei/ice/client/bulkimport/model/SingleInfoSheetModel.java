package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

/**
 * Extracts common elements of entries from sheet
 * 
 * @author Hector Plahar
 * 
 * @param <T>
 */
public abstract class SingleInfoSheetModel<T extends EntryInfo> extends SheetModel<T> {

    public abstract T setField(T info, SheetCellData datum);

    public T setInfoField(SheetCellData datum, EntryInfo info) {

        if (info == null)
            info = createInfo();

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        switch (header) {
        case PI:
            info.setPrincipalInvestigator(value);
            break;

        case FUNDING_SOURCE:
            info.setFundingSource(value);
            break;

        case IP:
            info.setIntellectualProperty(value);
            break;

        case BIOSAFETY:
            Integer optionValue = BioSafetyOptions.intValue(value);
            if (optionValue != null)
                info.setBioSafetyLevel(optionValue);
            break;

        case NAME:
            info.setName(value);
            break;

        case ALIAS:
            info.setAlias(value);
            break;

        case KEYWORDS:
            info.setKeywords(value);
            break;

        case SUMMARY:
            info.setShortDescription(value);
            break;

        case NOTES:
            info.setLongDescription(value);
            info.setLongDescriptionType("text");
            break;

        case REFERENCES:
            info.setReferences(value);
            break;

        case LINKS:
            info.setLinks(value);
            break;

        case STATUS:
            info.setStatus(value);
            break;

        case SEQ_FILENAME:
            ArrayList<SequenceAnalysisInfo> seq = info.getSequenceAnalysis();
            if (seq == null) {
                seq = new ArrayList<SequenceAnalysisInfo>();
                info.setSequenceAnalysis(seq);
            }

            if (datum.getId().isEmpty())
                break;

            SequenceAnalysisInfo analysisInfo = new SequenceAnalysisInfo();
            analysisInfo.setName(value);
            analysisInfo.setFileId(datum.getId());
            seq.add(analysisInfo);
            info.setHasSequence(true);
            info.setSequenceAnalysis(seq);
            break;

        case ATT_FILENAME:
            ArrayList<AttachmentInfo> attInfo = info.getAttachments();
            if (attInfo == null) {
                attInfo = new ArrayList<AttachmentInfo>();
                info.setAttachments(attInfo);
            }

            if (datum.getId().isEmpty())
                break;

            AttachmentInfo att = new AttachmentInfo();
            att.setFilename(value);
            att.setFileId(datum.getId());
            attInfo.add(att);
            info.setHasAttachment(true);
            info.setAttachments(attInfo);
            break;
        }

        return (T) info;
    }
}
