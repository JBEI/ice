package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.bulkupload.EntryField;
import org.jbei.ice.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;

import java.util.ArrayList;

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

        EntryField header = datum.getTypeHeader();
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

            case BIOSAFETY_LEVEL:
                Integer optionValue = BioSafetyOption.intValue(value);
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

                SequenceAnalysisInfo analysisInfo = seq.isEmpty() ? null : seq.get(0);
                String seqFileId = datum.getId();
                if (seqFileId == null || seqFileId.isEmpty()) {
                    if (analysisInfo != null) {
                        analysisInfo.setFileId("");
                        analysisInfo.setName("");
                    }
                    break;
                }

                seq.clear();

                if (analysisInfo == null)
                    analysisInfo = new SequenceAnalysisInfo();

                analysisInfo.setName(value);
                analysisInfo.setFileId(datum.getId());
                seq.add(analysisInfo);
                info.setHasSequence(true);
                info.setHasOriginalSequence(true);
                break;

            case ATT_FILENAME:
                ArrayList<AttachmentInfo> attachmentInfoList = info.getAttachments();
                if (attachmentInfoList == null) {
                    attachmentInfoList = new ArrayList<AttachmentInfo>();
                    info.setAttachments(attachmentInfoList);
                }

                AttachmentInfo attachmentInfo = attachmentInfoList.isEmpty() ? null : attachmentInfoList.get(0);

                String fileId = datum.getId();
                if (fileId == null || fileId.isEmpty()) {
                    if (attachmentInfo != null) {
                        attachmentInfo.setFileId("");
                        attachmentInfo.setFilename("");
                    }
                    break;
                }

                attachmentInfoList.clear();
                if (attachmentInfo == null) {
                    attachmentInfo = new AttachmentInfo();
                }

                attachmentInfo.setFilename(value);
                attachmentInfo.setFileId(datum.getId());
                attachmentInfoList.add(attachmentInfo);
                info.setHasAttachment(true);
                break;

            // deal with samples
            case SAMPLE_NAME:
                info.setHasSample(!value.trim().isEmpty());
                if (!info.isHasSample())
                    break;

                SampleStorage sampleStorage = info.getOneSampleStorage();
                if (sampleStorage.getSample() == null)
                    sampleStorage.setSample(new SampleInfo());

                sampleStorage.getSample().setLabel(value);
                break;
        }

        return this.setField((T) info, datum);
    }
}
