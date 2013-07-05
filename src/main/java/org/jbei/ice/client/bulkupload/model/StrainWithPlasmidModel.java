package org.jbei.ice.client.bulkupload.model;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.lib.shared.dto.entry.StrainInfo;

public class StrainWithPlasmidModel extends SheetModel<StrainInfo> {

    @Override
    public StrainInfo setInfoField(SheetCellData datum, EntryInfo strain) {
        if (strain == null) {
            strain = new StrainInfo();
        }

        PlasmidInfo plasmid = (PlasmidInfo) strain.getInfo();
        if (plasmid == null) {
            plasmid = new PlasmidInfo();
            strain.setInfo(plasmid);
        }

        setPlasmidInfo(plasmid, datum);
        setStrainInfo((StrainInfo) strain, datum);

//        strain.setInfo(plasmid);
        return (StrainInfo) strain;
    }

    @Override
    public StrainInfo createInfo() {
        PlasmidInfo plasmid = new PlasmidInfo();
        StrainInfo strain = new StrainInfo();
        strain.setInfo(plasmid);
        return strain;
    }

    private void setStrainInfo(StrainInfo strain, SheetCellData datum) {
        if (datum == null)
            return;

        EntryField header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return;

        switch (header) {
            case PI:
                strain.setPrincipalInvestigator(value);
                break;

            case FUNDING_SOURCE:
                strain.setFundingSource(value);
                break;
            case IP:
                strain.setIntellectualProperty(value);
                break;

            case BIOSAFETY_LEVEL:
                Integer optionValue = BioSafetyOption.intValue(value);
                strain.setBioSafetyLevel(optionValue);
                break;

            case STATUS:
                strain.setStatus(value);
                break;

            case STRAIN_NAME:
                strain.setName(value);
                break;

            case STRAIN_ALIAS:
                strain.setAlias(value);
                break;

            case STRAIN_KEYWORDS:
                strain.setKeywords(value);
                break;

            case STRAIN_SUMMARY:
                strain.setShortDescription(value);
                break;

            case STRAIN_NOTES:
                strain.setLongDescription(value);
                break;

            case STRAIN_REFERENCES:
                strain.setReferences(value);
                break;

            case STRAIN_LINKS:
                strain.setLinks(value);
                break;

            case STRAIN_STATUS:
                strain.setStatus(value);
                break;

            case STRAIN_SEQ_FILENAME:
                ArrayList<SequenceAnalysisInfo> seq = strain.getSequenceAnalysis();
                if (seq == null) {
                    seq = new ArrayList<SequenceAnalysisInfo>();
                    strain.setSequenceAnalysis(seq);
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
                strain.setHasSequence(true);
                strain.setHasOriginalSequence(true);
                break;

            case STRAIN_ATT_FILENAME:
                ArrayList<AttachmentInfo> attachmentInfoList = strain.getAttachments();
                if (attachmentInfoList == null) {
                    attachmentInfoList = new ArrayList<AttachmentInfo>();
                    strain.setAttachments(attachmentInfoList);
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
                strain.setHasAttachment(true);
                break;

            case STRAIN_SELECTION_MARKERS:
                strain.setSelectionMarkers(value);
                break;

            case STRAIN_PARENTAL_STRAIN:
                strain.setHost(value);
                break;

            case STRAIN_GEN_PHEN:
                strain.setGenotypePhenotype(value);
                break;
        }
    }

    public void setPlasmidInfo(PlasmidInfo info, SheetCellData datum) {
        if (datum == null)
            return;

        EntryField header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return;

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

            case STATUS:
                info.setStatus(value);
                break;

            case PLASMID_NAME:
                info.setName(value);
                break;

            case PLASMID_ALIAS:
                info.setAlias(value);
                break;

            case PLASMID_KEYWORDS:
                info.setKeywords(value);
                break;

            case PLASMID_SUMMARY:
                info.setShortDescription(value);
                break;

            case PLASMID_NOTES:
                info.setLongDescription(value);
                break;

            case PLASMID_REFERENCES:
                info.setReferences(value);
                break;

            case PLASMID_LINKS:
                info.setLinks(value);
                break;

            case PLASMID_STATUS:
                info.setStatus(value);
                break;

            case PLASMID_SEQ_FILENAME:
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

            case PLASMID_ATT_FILENAME:
                ArrayList<AttachmentInfo> attInfo = info.getAttachments();
                if (attInfo == null) {
                    attInfo = new ArrayList<AttachmentInfo>();
                    info.setAttachments(attInfo);
                }

                AttachmentInfo attachmentInfo = attInfo.isEmpty() ? null : attInfo.get(0);

                String fileId = datum.getId();
                if (fileId == null || fileId.isEmpty()) {
                    if (attachmentInfo != null) {
                        attachmentInfo.setFileId("");
                        attachmentInfo.setFilename("");
                    }
                    break;
                }

                attInfo.clear();
                if (attachmentInfo == null) {
                    attachmentInfo = new AttachmentInfo();
                }

                attachmentInfo.setFilename(value);
                attachmentInfo.setFileId(datum.getId());
                attInfo.add(attachmentInfo);
                info.setHasAttachment(true);
                break;

            case PLASMID_SELECTION_MARKERS:
                info.setSelectionMarkers(value);
                break;

            case CIRCULAR:
                if (value.isEmpty() || (!"Yes".equalsIgnoreCase(value)
                        && !"True".equalsIgnoreCase(value)
                        && !"False".equalsIgnoreCase(value)
                        && !"No".equalsIgnoreCase(value))) {
                    info.setCircular(null);
                    break;
                }

                boolean circular = "Yes".equalsIgnoreCase(value) || "True".equalsIgnoreCase(value);
                info.setCircular(circular);
                break;

            case PLASMID_BACKBONE:
                info.setBackbone(value);
                break;

            case PLASMID_PROMOTERS:
                info.setPromoters(value);
                break;

            case PLASMID_ORIGIN_OF_REPLICATION:
                info.setOriginOfReplication(value);
                break;
        }
    }
}
