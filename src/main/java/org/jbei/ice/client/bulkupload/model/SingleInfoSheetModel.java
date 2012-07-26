package org.jbei.ice.client.bulkupload.model;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

/**
 * Extracts common elements of entries from sheet
 *
 * @param <T>
 * @author Hector Plahar
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
                // we are reusing sequence analysis info here. It is for trace files
                ArrayList<SequenceAnalysisInfo> seq = info.getSequenceAnalysis();
                if (seq == null) {
                    seq = new ArrayList<SequenceAnalysisInfo>();
                    info.setSequenceAnalysis(seq);
                }

                String seqFileId = datum.getId();
                if (seqFileId == null || seqFileId.isEmpty())
                    break;

                SequenceAnalysisInfo analysisInfo = seq.isEmpty() ? null : seq.get(0);
                seq.clear();

                if (analysisInfo == null)
                    analysisInfo = new SequenceAnalysisInfo();

                analysisInfo.setName(value);
                analysisInfo.setFileId(datum.getId());
                seq.add(analysisInfo);
                info.setHasSequence(true);
                break;

            case ATT_FILENAME:
                ArrayList<AttachmentInfo> attachmentInfoList = info.getAttachments();
                if (attachmentInfoList == null) {
                    attachmentInfoList = new ArrayList<AttachmentInfo>();
                    info.setAttachments(attachmentInfoList);
                }

                String fileId = datum.getId();
                if (fileId == null || fileId.isEmpty())
                    break;

                AttachmentInfo attachmentInfo = attachmentInfoList.isEmpty() ? null : attachmentInfoList.get(0);
                attachmentInfoList.clear();

                if (attachmentInfo == null) {
                    attachmentInfo = new AttachmentInfo();
                }

                attachmentInfo.setFilename(value);
                attachmentInfo.setFileId(datum.getId());
                attachmentInfoList.add(attachmentInfo);
                info.setHasAttachment(true);
                break;
        }

        return this.setField((T) info, datum);
    }
}
