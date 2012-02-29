package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.StrainInfo;

public class StrainSheetModel extends SingleInfoSheetModel {

    private HashMap<Integer, StrainInfo> list;

    public StrainSheetModel() {
        list = new HashMap<Integer, StrainInfo>();
    }

    @Override
    public ArrayList<EntryInfo> createInfo(HashMap<Integer, String[]> data) {
        ArrayList<EntryInfo> entries = new ArrayList<EntryInfo>();

        for (String[] datum : data.values()) {
            StrainInfo info = new StrainInfo();
            for (Header header : Header.values()) {
                setField(header, info, datum[header.ordinal()]);
            }
            entries.add(info);
        }

        return entries;
    }

    public void rowToInfo(int index, Header header, String value) {

        StrainInfo info;
        if (list.containsKey(index))
            info = list.get(index);
        else {
            info = new StrainInfo();
            list.put(index, info);
        }
    }

    public void setField(Header header, StrainInfo info, String value) {
        if (value == null || value.trim().isEmpty())
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
            // TODO : long description type of text
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
            seq.add(analysisInfo);
            break;

        case ATT_FILENAME:
            ArrayList<AttachmentInfo> attInfo = info.getAttachments();
            if (attInfo == null) {
                attInfo = new ArrayList<AttachmentInfo>();
                info.setAttachments(attInfo);
            }

            AttachmentInfo att = new AttachmentInfo();
            att.setFilename(value);
            attInfo.add(att);
            break;

        case SELECTION_MARKERS:
            info.setSelectionMarkers(value);
            break;

        case PARENTAL_STRAIN:
            info.setHost(value);
            break;

        case GEN_PHEN:
            info.setGenotypePhenotype(value);
            break;

        case PLASMIDS:
            info.setPlasmids(value);
            break;
        }
    }

}
