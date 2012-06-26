package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.StrainInfo;

import java.util.ArrayList;

public class StrainWithPlasmidModel extends SheetModel<StrainInfo> {

    @Override
    public StrainInfo setInfoField(SheetFieldData datum, EntryInfo strain) {
        PlasmidInfo plasmid;

        if (strain == null) {
            plasmid = new PlasmidInfo();
            strain = new StrainInfo();
        } else {
            plasmid = (strain.getInfo() == null) ? (new PlasmidInfo()) : (PlasmidInfo) strain.getInfo();
        }

        setPlasmidInfo(plasmid, datum);
        setStrainInfo((StrainInfo) strain, datum);

        strain.setInfo(plasmid);
        return (StrainInfo) strain;
    }

    private void setStrainInfo(StrainInfo strain, SheetFieldData datum) {
        if (datum == null)
            return;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null || value.isEmpty())
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

            case BIOSAFETY:
                Integer optionValue = BioSafetyOptions.intValue(value);
                if (optionValue != null)
                    strain.setBioSafetyLevel(optionValue);
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
                strain.setLongDescriptionType("text");
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
                SequenceAnalysisInfo analysisInfo = new SequenceAnalysisInfo();
                analysisInfo.setName(value);
                analysisInfo.setFileId(datum.getId());
                seq.add(analysisInfo);
                strain.setHasSequence(true);
                strain.setSequenceAnalysis(seq);
                break;

            case STRAIN_ATT_FILENAME:
                ArrayList<AttachmentInfo> attInfo = strain.getAttachments();
                if (attInfo == null) {
                    attInfo = new ArrayList<AttachmentInfo>();
                    strain.setAttachments(attInfo);
                }

                AttachmentInfo att = new AttachmentInfo();
                att.setFilename(value);
                att.setFileId(datum.getId());
                attInfo.add(att);
                strain.setHasAttachment(true);
                strain.setAttachments(attInfo);
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

            case STRAIN_PLASMIDS:
                strain.setPlasmids(value);
                break;
        }
    }

    public void setPlasmidInfo(PlasmidInfo info, SheetFieldData datum) {
        if (datum == null)
            return;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null || value.isEmpty())
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

            case BIOSAFETY:
                Integer optionValue = BioSafetyOptions.intValue(value);
                if (optionValue != null)
                    info.setBioSafetyLevel(optionValue);
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
                info.setLongDescriptionType("text");
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
                SequenceAnalysisInfo analysisInfo = new SequenceAnalysisInfo();
                analysisInfo.setName(value);
                analysisInfo.setFileId(datum.getId());
                seq.add(analysisInfo);
                info.setHasSequence(true);
                info.setSequenceAnalysis(seq);
                break;

            case PLASMID_ATT_FILENAME:
                ArrayList<AttachmentInfo> attInfo = info.getAttachments();
                if (attInfo == null) {
                    attInfo = new ArrayList<AttachmentInfo>();
                    info.setAttachments(attInfo);
                }

                AttachmentInfo att = new AttachmentInfo();
                att.setFilename(value);
                att.setFileId(datum.getId());
                attInfo.add(att);
                info.setHasAttachment(true);
                info.setAttachments(attInfo);
                break;

            case PLASMID_SELECTION_MARKERS:
                info.setSelectionMarkers(value);
                break;

            case CIRCULAR:
                boolean circular = Boolean.parseBoolean(value);
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
