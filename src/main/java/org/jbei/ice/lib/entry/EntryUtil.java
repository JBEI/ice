package org.jbei.ice.lib.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.model.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Utility class for operating on entries
 *
 * @author Hector Plahar
 */
public class EntryUtil {

    public static String entryFieldToValue(Entry entry, EntryFieldLabel field) {
        String value = getCommonFieldValues(entry, field);
        if (value != null)
            return value;

        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            return null;

        switch (type) {
            case STRAIN:
                value = getStrainFieldValues((Strain) entry, field);
                break;

            case PLASMID:
                value = getPlasmidFieldValues((Plasmid) entry, field);
                break;

            case SEED:
                value = getSeedFieldValues((ArabidopsisSeed) entry, field);
                break;

            case PROTEIN:
                value = getProteinFieldValues((Protein) entry, field);
                break;

            default:
                value = null;
        }

        if (value == null)
            return "";

        return value.trim();
    }

    /**
     * String representation of {@link Link}s.
     *
     * @return Comma separated list of links.
     */
    private static String getLinksAsString(Set<Link> links) {
        String result;
        ArrayList<String> linksStr = new ArrayList<>();
        for (Link link : links) {
            linksStr.add(link.getLink());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", linksStr);

        return result;
    }

    private static String getCommonFieldValues(Entry entry, EntryFieldLabel field) {
        switch (field) {
            case PI:
                return entry.getPrincipalInvestigator();

            case PI_EMAIL:
                return entry.getPrincipalInvestigatorEmail();

            case FUNDING_SOURCE:
                return entry.getFundingSource();

            case IP:
                return entry.getIntellectualProperty();

            case BIO_SAFETY_LEVEL:
                return entry.getBioSafetyLevel().toString();

            case NAME:
                return entry.getName();

            case ALIAS:
                return entry.getAlias();

            case KEYWORDS:
                return entry.getKeywords();

            case SUMMARY:
                return entry.getShortDescription();

            case NOTES:
                return entry.getLongDescription();

            case REFERENCES:
                return entry.getReferences();

            case LINKS:
                return getLinksAsString(entry.getLinks());

            case STATUS:
                return entry.getStatus();

            case SELECTION_MARKERS:
                return entry.getSelectionMarkersAsString();

            case CREATOR:
                return entry.getCreator();

            case CREATOR_EMAIL:
                return entry.getCreatorEmail();

            default:
                return null;
        }
    }

    private static String getStrainFieldValues(Strain strain, EntryFieldLabel field) {
        switch (field) {
            case HOST:
                return strain.getHost();

            case GENOTYPE_OR_PHENOTYPE:
                return strain.getGenotypePhenotype();

            default:
                return null;
        }
    }

    private static String getPlasmidFieldValues(Plasmid plasmid, EntryFieldLabel field) {
        switch (field) {
            case BACKBONE:
                return plasmid.getBackbone();

            case ORIGIN_OF_REPLICATION:
                return plasmid.getOriginOfReplication();

            case CIRCULAR:
                return plasmid.getCircular().toString();

            case PROMOTERS:
                return plasmid.getPromoters();

            case REPLICATES_IN:
                return plasmid.getReplicatesIn();
            default:
                return null;
        }
    }

    private static String getSeedFieldValues(ArabidopsisSeed seed, EntryFieldLabel field) {
        switch (field) {
            case HOMOZYGOSITY:
                return seed.getHomozygosity();

            case ECOTYPE:
                return seed.getEcotype();

            case HARVEST_DATE:
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                Date date = seed.getHarvestDate();
                if (date == null)
                    return "";
                return dateFormat.format(date);

            case GENERATION:
                return seed.getGeneration().toString();

            case SENT_TO_ABRC:
                return seed.isSentToABRC() ? "Yes" : "No";

            case PLANT_TYPE:
                return seed.getPlantType().toString();

            case PARENTS:
                return seed.getParents();

            default:
                return null;
        }
    }

    private static String getProteinFieldValues(Protein protein, EntryFieldLabel field) {
        switch (field) {
            case ORGANISM:
                return protein.getOrganism();

            case FULL_NAME:
                return protein.getFullName();

            case GENE_NAME:
                return protein.getGeneName();

            case UPLOADED_FROM:
                return protein.getUploadedFrom();

            default:
                return null;
        }
    }

    public static String getPartNumberPrefix() {
        return Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX) +
                Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DELIMITER);
    }

    public static ArrayList<String> getSelectionMarkersAsList(Set<SelectionMarker> markers) {
        ArrayList<String> selectionMarkers = new ArrayList<>();
        if (markers == null)
            return selectionMarkers;

        for (SelectionMarker marker : markers) {
            selectionMarkers.add(marker.getName());
        }
        return selectionMarkers;
    }

    /**
     * Validates the required fields in the Data Transfer Object
     *
     * @param partData DTO whose fields are being validated
     * @return list which contains fields (if any) that are invalid
     */
    public static List<EntryFieldLabel> validates(PartData partData) {
        List<EntryFieldLabel> invalidFields = new ArrayList<>();

        if (StringUtils.isEmpty(partData.getName()))
            invalidFields.add(EntryFieldLabel.NAME);

        if (partData.getBioSafetyLevel() == null)
            invalidFields.add(EntryFieldLabel.BIO_SAFETY_LEVEL);

        if (StringUtils.isEmpty(partData.getStatus()))
            invalidFields.add(EntryFieldLabel.STATUS);

        if (StringUtils.isEmpty(partData.getCreator()))
            invalidFields.add(EntryFieldLabel.CREATOR);

        if (StringUtils.isEmpty(partData.getCreatorEmail()))
            invalidFields.add(EntryFieldLabel.CREATOR_EMAIL);

        if (StringUtils.isEmpty(partData.getShortDescription()))
            invalidFields.add(EntryFieldLabel.SUMMARY);

        return invalidFields;
    }

    static PartData setPartDefaults(PartData partData) {
        if (partData.getType() == EntryType.PLASMID) {
            if (partData.getPlasmidData() == null) {
                PlasmidData plasmidData = new PlasmidData();
                plasmidData.setCircular(true);
                partData.setPlasmidData(plasmidData);
            } else
                partData.getPlasmidData().setCircular(true);
        }

        return partData;
    }

    private static StrainData setStrainDataFromField(StrainData strainData, String value, EntryFieldLabel field) {
        if (strainData == null)
            strainData = new StrainData();

        switch (field) {
            case HOST:
                strainData.setHost(value);
                break;

            case GENOTYPE_OR_PHENOTYPE:
                strainData.setGenotypePhenotype(value);
                break;
        }

        return strainData;
    }

    private static PlasmidData setPlasmidDataFromField(PlasmidData plasmidData, String value, EntryFieldLabel field) {
        if (plasmidData == null)
            plasmidData = new PlasmidData();

        switch (field) {
            case BACKBONE:
                plasmidData.setBackbone(value);
                break;

            case ORIGIN_OF_REPLICATION:
                plasmidData.setOriginOfReplication(value);
                break;

            case CIRCULAR:
                plasmidData.setCircular("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                break;

            case PROMOTERS:
                plasmidData.setPromoters(value);
                break;

            case REPLICATES_IN:
                plasmidData.setReplicatesIn(value);
                break;
        }

        return plasmidData;
    }

    private static SeedData setSeedDataFromField(SeedData seedData, String value, EntryFieldLabel field) {
        if (seedData == null)
            seedData = new SeedData();

        switch (field) {
            case HOMOZYGOSITY:
                seedData.setHomozygosity(value);
                break;

            case ECOTYPE:
                seedData.setEcotype(value);
                break;

            case HARVEST_DATE:
                if (value != null && !value.isEmpty()) {
                    seedData.setHarvestDate(value);
                }
                break;

            case GENERATION:
                if (!StringUtils.isEmpty(value))
                    seedData.setGeneration(Generation.fromString(value));
                break;

            case SENT_TO_ABRC:
                seedData.setSentToAbrc("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                break;

            case PLANT_TYPE:
                if (!StringUtils.isEmpty(value))
                    seedData.setPlantType(PlantType.fromString(value));
                break;

            case PARENTS:
                seedData.setSeedParents(value);
                break;
        }

        return seedData;
    }

    private static ProteinData setProteinDataFromField(ProteinData proteinData, String value, EntryFieldLabel field) {
        if (proteinData == null)
            proteinData = new ProteinData();

        switch (field) {
            case ORGANISM:
                proteinData.setOrganism(value);
                break;

            case FULL_NAME:
                proteinData.setFullName(value);
                break;

            case GENE_NAME:
                proteinData.setGeneName(value);
                break;

            case UPLOADED_FROM:
                proteinData.setUploadedFrom(value);
                break;
        }

        return proteinData;
    }

    /**
     * Updates the partData based on the field that is specified.
     * Mainly created for use by the bulk import auto update
     *
     * @param data  entry to be updated
     * @param value value to be set
     * @param field entry field whose value is to be set
     */
    public static void setPartDataFromField(PartData data, String value, EntryFieldLabel field) {
        switch (field) {
            case PI:
                data.setPrincipalInvestigator(value);
                break;

            case PI_EMAIL: {
                data.setPrincipalInvestigatorEmail(value);
                break;
            }

            case FUNDING_SOURCE: {
                data.setFundingSource(value);
                break;
            }

            case IP:
                data.setIntellectualProperty(value);
                break;

            case BIO_SAFETY_LEVEL:
                Integer level = BioSafetyOption.intValue(value);
                if (level == null) {
                    if (value.contains("1"))
                        level = 1;
                    else if (value.contains("2"))
                        level = 2;
                    else if ("restricted".equalsIgnoreCase(value))
                        level = -1;
                    else
                        break;
                }
                data.setBioSafetyLevel(level);
                break;

            case NAME:
                data.setName(value);
                break;

            case ALIAS:
                data.setAlias(value);
                break;

            case KEYWORDS:
                data.setKeywords(value);
                break;

            case SUMMARY:
                data.setShortDescription(value);
                break;

            case NOTES:
                data.setLongDescription(value);
                break;

            case REFERENCES:
                data.setReferences(value);
                break;

            case LINKS:
                ArrayList<String> links = new ArrayList<>();
                links.add(value);
                data.setLinks(links);
                break;

            case STATUS:
                data.setStatus(value);
                break;

            case SELECTION_MARKERS:
                ArrayList<String> selectionMarkers = new ArrayList<>();
                selectionMarkers.add(value);
                data.setSelectionMarkers(selectionMarkers);
                break;

            case HOST:
            case GENOTYPE_OR_PHENOTYPE:
                data.setStrainData(setStrainDataFromField(data.getStrainData(), value, field));
                break;

            case BACKBONE:
            case ORIGIN_OF_REPLICATION:
            case CIRCULAR:
            case PROMOTERS:
            case REPLICATES_IN:
                data.setPlasmidData(setPlasmidDataFromField(data.getPlasmidData(), value, field));
                break;

            case HOMOZYGOSITY:
            case ECOTYPE:
            case HARVEST_DATE:
            case GENERATION:
            case SENT_TO_ABRC:
            case PLANT_TYPE:
            case PARENTS:
                data.setSeedData(setSeedDataFromField(data.getSeedData(), value, field));
                break;

            case ORGANISM:
            case FULL_NAME:
            case GENE_NAME:
            case UPLOADED_FROM:
                data.setProteinData(setProteinDataFromField(data.getProteinData(), value, field));
                break;

            case CREATOR:
                data.setCreator(value);
                break;

            case CREATOR_EMAIL:
                data.setCreatorEmail(value);
                break;

            default:
                break;
        }
    }
}
