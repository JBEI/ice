package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Hector Plahar
 */
public class PartDataUtil {

    public static String entryFieldToValue(PartData data, EntryField field) {

        String value = getCommonFieldValues(data, field);
        if (value != null)
            return value;

        switch (data.getType()) {
            case STRAIN:
                value = getStrainFieldValues(data.getStrainData(), field);
                break;

            case PLASMID:
                value = getPlasmidFieldValues(data.getPlasmidData(), field);
                break;

            case ARABIDOPSIS:
                value = getSeedFieldValues(data.getArabidopsisSeedData(), field);
                break;

            default:
                value = null;
        }

        if (value == null)
            return "";

        return value.trim();
    }

    /**
     * Concatenate a Collection of Strings using the given delimiter.
     */
    public static String join(Collection<?> s) {
        if (s == null)
            return "";
        StringBuilder buffer = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item != null) {
                buffer.append(item);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }

        }
        return buffer.toString();
    }

    protected static String getCommonFieldValues(PartData data, EntryField field) {
        switch (field) {
            case PI:
                return data.getPrincipalInvestigator();

            case PI_EMAIL:
                return data.getPrincipalInvestigatorEmail();

            case FUNDING_SOURCE:
                return data.getFundingSource();

            case IP:
                return data.getIntellectualProperty();

            case BIO_SAFETY_LEVEL:
                return data.getBioSafetyLevel().toString();

            case NAME:
                return data.getName();

            case ALIAS:
                return data.getAlias();

            case KEYWORDS:
                return data.getKeywords();

            case SUMMARY:
                return data.getShortDescription();

            case NOTES:
                return data.getLongDescription();

            case REFERENCES:
                return data.getReferences();

            case LINKS:
                return join(data.getLinks());

            case STATUS:
                return data.getStatus();

            case SELECTION_MARKERS:
                return join(data.getSelectionMarkers());

            case CREATOR:
                return data.getCreator();

            case CREATOR_EMAIL:
                return data.getCreatorEmail();

            default:
                return null;
        }
    }

    protected static String getStrainFieldValues(StrainData strainData, EntryField field) {
        if (strainData == null)
            return "";

        switch (field) {
            case PARENTAL_STRAIN:
                return strainData.getHost();

            case GENOTYPE_OR_PHENOTYPE:
                return strainData.getGenotypePhenotype();

            default:
                return null;
        }
    }

    protected static String getPlasmidFieldValues(PlasmidData plasmidData, EntryField field) {
        if (plasmidData == null)
            return "";

        switch (field) {
            case BACKBONE:
                return plasmidData.getBackbone();

            case ORIGIN_OF_REPLICATION:
                return plasmidData.getOriginOfReplication();

            case CIRCULAR:
                return plasmidData.getCircular().toString();

            case PROMOTERS:
                return plasmidData.getPromoters();

            case REPLICATES_IN:
                return plasmidData.getReplicatesIn();
            default:
                return null;
        }
    }

    protected static String getSeedFieldValues(ArabidopsisSeedData seed, EntryField field) {
        if (seed == null)
            return "";

        switch (field) {
            case HOMOZYGOSITY:
                return seed.getHomozygosity();

            case ECOTYPE:
                return seed.getEcotype();

            case HARVEST_DATE:
                return seed.getHarvestDate();

            case GENERATION:
                return seed.getGeneration().toString();

            case SENT_TO_ABRC:
                return seed.isSentToAbrc() ? "Yes" : "No";

            case PLANT_TYPE:
                return seed.getPlantType().toString();

            case PARENTS:
                return seed.getSeedParents();

            default:
                return null;
        }
    }
}
