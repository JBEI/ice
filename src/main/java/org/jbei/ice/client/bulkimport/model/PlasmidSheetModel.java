package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

public class PlasmidSheetModel extends SingleInfoSheetModel {

    @Override
    public void createInfo(ArrayList<SheetFieldData[]> data, ArrayList<EntryInfo> primaryData,
            ArrayList<EntryInfo> secondaryData) {
        if (primaryData == null)
            primaryData = new ArrayList<EntryInfo>();
        else
            primaryData.clear();

        // for each row
        for (SheetFieldData[] datumArray : data) {

            // each each field
            PlasmidInfo info = new PlasmidInfo();
            for (SheetFieldData datum : datumArray)
                setField(info, datum);
            primaryData.add(info);
        }
    }

    public void setField(PlasmidInfo info, SheetFieldData datum) {
        if (datum == null)
            return;

        Header header = datum.getType();
        String value = datum.getValue();

        if (header == null)
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
            try {
                info.setBioSafetyLevel(Integer.valueOf(value));

            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Illegal Pairing of Biosafety and value of "
                        + value);
            }
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
            SequenceAnalysisInfo analysisInfo = new SequenceAnalysisInfo();
            analysisInfo.setName(value);
            analysisInfo.setFileId(datum.getId());
            seq.add(analysisInfo);
            info.setHasSequence(true);
            break;

        case ATT_FILENAME:
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
            break;

        case SELECTION_MARKERS:
            info.setSelectionMarkers(value);
            break;

        case CIRCULAR:
            boolean circular = Boolean.parseBoolean(value);
            info.setCircular(circular);
            break;

        case BACKBONE:
            info.setBackbone(value);
            break;

        case PROMOTERS:
            info.setPromoters(value);
            break;

        case ORIGIN_OF_REPLICATION:
            info.setOriginOfReplication(value);
            break;
        }
    }
}
