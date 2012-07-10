package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.StrainInfo;

/**
 * Factory class for extracting the value from an EntryInfo
 * based on type and the header. Used to load a sheet with existing data
 *
 * @author Hector Plahar
 */
public class InfoValueExtractorFactory {

    public static String extractValue(EntryAddType type, Header header, EntryInfo primary,
            EntryInfo secondary, int index) {

        if (header == null)
            return "";

        String value = InfoValueExtractor.extractCommon(header, primary, index);

        if (value != null)
            return value;

        switch (type) {
            case STRAIN:
                // strain specific
                StrainInfo info = (StrainInfo) primary;
                switch (header) {
                    case PARENTAL_STRAIN:
                        return info.getHost();
                    case GEN_PHEN:
                        return info.getGenotypePhenotype();
                    case PLASMIDS:
                        return info.getPlasmids();
                }
                break;

            case PLASMID:
                PlasmidInfo plasmid = (PlasmidInfo) primary;
                switch (header) {
                    // plasmid specific
                    case BACKBONE:
                        return plasmid.getBackbone();
                    case ORIGIN_OF_REPLICATION:
                        return plasmid.getOriginOfReplication();
                    case PROMOTERS:
                        return plasmid.getPromoters();
                    case CIRCULAR:
                        if (plasmid.getCircular() == null)
                            return "";
                        return Boolean.toString(plasmid.getCircular());
                }
                break;

            case STRAIN_WITH_PLASMID:
                if (secondary == null) {
                } // there is big problem

                StrainInfo strain = (StrainInfo) primary;
                PlasmidInfo plasmidInfo = (PlasmidInfo) secondary;

                switch (header) {
                    case STRAIN_NAME:
                        return strain.getName();

                    case STRAIN_ALIAS:
                        return strain.getAlias();

                    case STRAIN_KEYWORDS:
                        return strain.getKeywords();
                    case STRAIN_SUMMARY:
                        return strain.getShortDescription();

                    case STRAIN_NOTES:
                        return strain.getLongDescription();

                    case STRAIN_REFERENCES:
                        return strain.getReferences();

                    case STRAIN_LINKS:
                        return strain.getLinks();

                    case STRAIN_STATUS:
                        return strain.getStatus();

                    case STRAIN_SEQ_FILENAME:
                        ArrayList<SequenceAnalysisInfo> strainSequenceInfos = strain.getSequenceAnalysis();
                        if (strainSequenceInfos == null || strainSequenceInfos.isEmpty()
                                || index >= strainSequenceInfos.size())
                            return "";

                        SequenceAnalysisInfo seqInfo = strainSequenceInfos.get(index);
                        if (seqInfo == null)
                            return "";

                        return seqInfo.getName();

                    case STRAIN_ATT_FILENAME:
                        ArrayList<AttachmentInfo> strainAttachmentInfos = strain.getAttachments();
                        if (strainAttachmentInfos == null || strainAttachmentInfos.isEmpty()
                                || index >= strainAttachmentInfos.size())
                            return "";

                        AttachmentInfo attInfo = strainAttachmentInfos.get(index);
                        if (attInfo == null)
                            return "";

                        return attInfo.getFilename();
                    case STRAIN_SELECTION_MARKERS:
                        return strain.getSelectionMarkers();
                    case STRAIN_PARENTAL_STRAIN:
                        return strain.getHost();
                    case STRAIN_GEN_PHEN:
                        return strain.getGenotypePhenotype();
                    case STRAIN_PLASMIDS:
                        return strain.getPlasmids();

                    // plasmid
                    case PLASMID_NAME:
                        return plasmidInfo.getName();
                    case PLASMID_ALIAS:
                        return plasmidInfo.getAlias();
                    case PLASMID_KEYWORDS:
                        return plasmidInfo.getKeywords();
                    case PLASMID_SUMMARY:
                        return plasmidInfo.getShortDescription();
                    case PLASMID_NOTES:
                        return plasmidInfo.getLongDescription();
                    case PLASMID_REFERENCES:
                        return plasmidInfo.getReferences();
                    case PLASMID_LINKS:
                        return plasmidInfo.getLinks();
                    case PLASMID_STATUS:
                        return plasmidInfo.getStatus();
                    case PLASMID_SEQ_FILENAME:
                        ArrayList<SequenceAnalysisInfo> plasmidSequenceInfos = strain.getSequenceAnalysis();
                        if (plasmidSequenceInfos == null || plasmidSequenceInfos.isEmpty()
                                || index >= plasmidSequenceInfos.size())
                            return "";

                        SequenceAnalysisInfo plasmidSequence = plasmidSequenceInfos.get(index);
                        if (plasmidSequence == null)
                            return "";

                        return plasmidSequence.getName();

                    case PLASMID_ATT_FILENAME:
                        ArrayList<AttachmentInfo> plasmidAttachmentInfo = plasmidInfo.getAttachments();
                        if (plasmidAttachmentInfo == null || plasmidAttachmentInfo.isEmpty()
                                || index >= plasmidAttachmentInfo.size())
                            return "";

                        AttachmentInfo plasmidAttachment = plasmidAttachmentInfo.get(index);
                        if (plasmidAttachment == null)
                            return "";

                        return plasmidAttachment.getFilename();

                    case PLASMID_SELECTION_MARKERS:
                    case CIRCULAR:
                        if (plasmidInfo.getCircular() == null)
                            return "";
                        return Boolean.toString(plasmidInfo.getCircular());
                    case PLASMID_BACKBONE:
                        return plasmidInfo.getBackbone();
                    case PLASMID_PROMOTERS:
                        return plasmidInfo.getPromoters();
                    case PLASMID_ORIGIN_OF_REPLICATION:
                        return plasmidInfo.getOriginOfReplication();
                }
                break;

            case ARABIDOPSIS:
                ArabidopsisSeedInfo seed = (ArabidopsisSeedInfo) primary;
                switch (header) {

                    case HOMOZYGOSITY:
                        return seed.getHomozygosity();

                    case ECOTYPE:
                        return seed.getEcotype();

                    case HARVEST_DATE:
                        Date harvestDate = seed.getHarvestDate();
                        if (harvestDate == null)
                            return "";
                        return DateUtilities.formatDate(harvestDate);

                    case PARENTS:
                        return seed.getParents();

                    case GENERATION:
                        Generation generation = seed.getGeneration();
                        if (generation == null)
                            return "";
                        return seed.getGeneration().name();

                    case PLANT_TYPE:
                        PlantType plantType = seed.getPlantType();
                        if (plantType == null)
                            return "";
                        return seed.getPlantType().toString();
                }
                break;

            //          case PART: part appears to be a strict super-set of all

        }

        return "";
    }
}
