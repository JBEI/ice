package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.StrainInfo;

/**
 * Factory class for extracting the value from an EntryInfo
 * based on type and the header. Used to loading a sheet with existing data
 * 
 * @author Hector Plahar
 */
public class InfoValueExtractorFactory {

    public static String extractValue(EntryAddType type, Header header, EntryInfo primary,
            EntryInfo secondary, int index, HashMap<Integer, String> attachmentRowFileIds,
            HashMap<Integer, String> sequenceRowFileIds) {
        switch (type) {
        case STRAIN:
            return getStrainValue(header, primary, index, attachmentRowFileIds, sequenceRowFileIds);

        case PLASMID:
            return getPlasmidValue(header, primary, index, attachmentRowFileIds, sequenceRowFileIds);

        case STRAIN_WITH_PLASMID:
            if (secondary == null) {
            } // there is big problem

            // TODO 
            //            return getStrainWithPlasmidValue(header, primary, index, attachmentRowFileIds,
            //                sequenceRowFileIds);
            break;

        case ARABIDOPSIS:
            return getSeedValue(header, primary, index, attachmentRowFileIds, sequenceRowFileIds);

        case PART:
            return getPartValue(header, primary, index, attachmentRowFileIds, sequenceRowFileIds);
        }

        return "";
    }

    private static String getSeedValue(Header header, EntryInfo primary, int index,
            HashMap<Integer, String> attachmentRowFileIds,
            HashMap<Integer, String> sequenceRowFileIds) {
        if (header == null)
            return "";

        ArabidopsisSeedInfo info = (ArabidopsisSeedInfo) primary;
        switch (header) {
        case PI:
            return info.getPrincipalInvestigator();

        case FUNDING_SOURCE:
            return info.getFundingSource();

        case IP:
            return info.getIntellectualProperty();

        case BIOSAFETY:
            Integer level = info.getBioSafetyLevel();
            if (level == null)
                return "";
            return info.getBioSafetyLevel().toString();

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

        case HOMOZYGOSITY:
            return info.getHomozygosity();

        case ECOTYPE:
            return info.getEcotype();

        case HARVEST_DATE:
            return info.getHarvestDate().toString();

        case PARENTS:
            return info.getParents();

        case GENERATION:
            return info.getGeneration().name();

        case PLANT_TYPE:
            return info.getPlantType().toString();

            // special case for files
        case ATT_FILENAME:
            ArrayList<AttachmentInfo> attInfos = info.getAttachments();
            if (attInfos == null || attInfos.isEmpty())
                return "";

            AttachmentInfo attInfo = attInfos.get(index);
            if (attInfo == null)
                return "";

            attachmentRowFileIds.put(index, attInfo.getFileId());
            return attInfo.getFilename();

        case SEQ_FILENAME:
            ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
            if (sequenceInfos == null || sequenceInfos.isEmpty())
                return "";

            SequenceAnalysisInfo seqInfo = sequenceInfos.get(index);
            if (seqInfo == null)
                return "";

            attachmentRowFileIds.put(index, seqInfo.getFileId());
            return seqInfo.getName();
        }

        return "";
    }

    private static String getPartValue(Header header, EntryInfo primary, int index,
            HashMap<Integer, String> attachmentRowFileIds,
            HashMap<Integer, String> sequenceRowFileIds) {
        if (header == null)
            return "";

        PartInfo info = (PartInfo) primary;
        switch (header) {
        case PI:
            return info.getPrincipalInvestigator();

        case FUNDING_SOURCE:
            return info.getFundingSource();

        case IP:
            return info.getIntellectualProperty();

        case BIOSAFETY:
            Integer level = info.getBioSafetyLevel();
            if (level == null)
                return "";
            return info.getBioSafetyLevel().toString();

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
            if (attInfos == null || attInfos.isEmpty())
                return "";

            AttachmentInfo attInfo = attInfos.get(index);
            if (attInfo == null)
                return "";

            attachmentRowFileIds.put(index, attInfo.getFileId());
            return attInfo.getFilename();

        case SEQ_FILENAME:
            ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
            if (sequenceInfos == null || sequenceInfos.isEmpty())
                return "";

            SequenceAnalysisInfo seqInfo = sequenceInfos.get(index);
            if (seqInfo == null)
                return "";

            attachmentRowFileIds.put(index, seqInfo.getFileId());
            return seqInfo.getName();
        }

        return "";
    }

    private static String getPlasmidValue(Header header, EntryInfo primary, int index,
            HashMap<Integer, String> attachmentRowFileIds,
            HashMap<Integer, String> sequenceRowFileIds) {
        if (header == null)
            return "";

        PlasmidInfo info = (PlasmidInfo) primary;
        switch (header) {
        case PI:
            return info.getPrincipalInvestigator();

        case FUNDING_SOURCE:
            return info.getFundingSource();

        case IP:
            return info.getIntellectualProperty();

        case BIOSAFETY:
            Integer level = info.getBioSafetyLevel();
            if (level == null)
                return "";
            return info.getBioSafetyLevel().toString();

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

        case BACKBONE:
            return info.getBackbone();

        case ORIGIN_OF_REPLICATION:
            return info.getOriginOfReplication();
        case PROMOTERS:
            return info.getPromoters();

        case CIRCULAR:
            return Boolean.toString(info.getCircular());

            // special case for files
        case ATT_FILENAME:
            ArrayList<AttachmentInfo> attInfos = info.getAttachments();
            if (attInfos == null || attInfos.isEmpty())
                return "";

            AttachmentInfo attInfo = attInfos.get(index);
            if (attInfo == null)
                return "";

            attachmentRowFileIds.put(index, attInfo.getFileId());
            return attInfo.getFilename();

        case SEQ_FILENAME:
            ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
            if (sequenceInfos == null || sequenceInfos.isEmpty())
                return "";

            SequenceAnalysisInfo seqInfo = sequenceInfos.get(index);
            if (seqInfo == null)
                return "";

            attachmentRowFileIds.put(index, seqInfo.getFileId());
            return seqInfo.getName();
        }

        return "";
    }

    private static String getStrainValue(Header header, EntryInfo primary, int index,
            HashMap<Integer, String> attachmentRowFileIds,
            HashMap<Integer, String> sequenceRowFileIds) {

        if (header == null)
            return "";

        StrainInfo info = (StrainInfo) primary;
        switch (header) {
        case PI:
            return info.getPrincipalInvestigator();

        case FUNDING_SOURCE:
            return info.getFundingSource();

        case IP:
            return info.getIntellectualProperty();

        case BIOSAFETY:
            Integer level = info.getBioSafetyLevel();
            if (level == null)
                return "";
            return info.getBioSafetyLevel().toString();

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

        case PARENTAL_STRAIN:
            return info.getHost();

        case GEN_PHEN:
            return info.getGenotypePhenotype();

        case PLASMIDS:
            return info.getPlasmids();

            // special case for files
        case ATT_FILENAME:
            ArrayList<AttachmentInfo> attInfos = info.getAttachments();
            if (attInfos == null || attInfos.isEmpty())
                return "";

            AttachmentInfo attInfo = attInfos.get(index);
            if (attInfo == null)
                return "";

            attachmentRowFileIds.put(index, attInfo.getFileId());
            return attInfo.getFilename();

        case SEQ_FILENAME:
            ArrayList<SequenceAnalysisInfo> sequenceInfos = info.getSequenceAnalysis();
            if (sequenceInfos == null || sequenceInfos.isEmpty())
                return "";

            SequenceAnalysisInfo seqInfo = sequenceInfos.get(index);
            if (seqInfo == null)
                return "";

            attachmentRowFileIds.put(index, seqInfo.getFileId());
            return seqInfo.getName();
        }

        return "";
    }
}
