package org.jbei.ice.lib.entry;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for operating on entries
 *
 * @author Hector Plahar
 */
public class EntryUtil {

    public static Entry createEntryFromType(EntryType type, String name, String email) {
        Entry entry;

        switch (type) {
            case PLASMID:
                entry = new Plasmid();
                break;

            case STRAIN:
                entry = new Strain();
                break;

            case ARABIDOPSIS:
                entry = new ArabidopsisSeed();
                break;

            default:
            case PART:
                entry = new Part();
                break;
        }

        entry.setOwner(name);
        entry.setOwnerEmail(email);
        entry.setCreator(name);
        entry.setCreatorEmail(email);
        return entry;
    }

    public static String entryFieldToValue(Entry entry, EntryField field) {
        String value = getCommonFieldValues(entry, field);
        if (value != null)
            return value;

        EntryType type = EntryType.nameToType(entry.getRecordType());

        switch (type) {
            case STRAIN:
                value = getStrainFieldValues((Strain) entry, field);
                break;

            case PLASMID:
                value = getPlasmidFieldValues((Plasmid) entry, field);
                break;

            case ARABIDOPSIS:
                value = getSeedFieldValues((ArabidopsisSeed) entry, field);
                break;

            default:
                value = null;
        }

        if (value == null)
            return "";

        return value.trim();
    }

    protected static String getCommonFieldValues(Entry entry, EntryField field) {
        switch (field) {
            case PI:
                return entry.getPrincipalInvestigator();

            case PI_EMAIL:
                return entry.getPrincipalInvestigatorEmail();

            case FUNDING_SOURCE:
                return entry.getFundingSource();

            case IP:
                return entry.getIntellectualProperty();

            case BIOSAFETY_LEVEL:
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
                return entry.getLinksAsString();

            case STATUS:
                return entry.getStatus();

            case SELECTION_MARKERS:
                return entry.getSelectionMarkersAsString();

            default:
                return null;
        }
    }

    protected static String getStrainFieldValues(Strain strain, EntryField field) {
        switch (field) {
            case PARENTAL_STRAIN:
                return strain.getHost();

            case GENOTYPE_OR_PHENOTYPE:
                return strain.getGenotypePhenotype();

            case PLASMIDS:
                return strain.getPlasmids();

            default:
                return null;
        }
    }

    protected static String getPlasmidFieldValues(Plasmid plasmid, EntryField field) {
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

    protected static String getSeedFieldValues(ArabidopsisSeed seed, EntryField field) {
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

    public static boolean validates(PartData partData) {
        if (partData.getType() == null)
            return false;

        switch (partData.getType()) {
            case PLASMID:
            case STRAIN:
            case ARABIDOPSIS:
                if (partData.getSelectionMarkers().isEmpty())
                    return false;

                // deliberately not breaking here to fall into part since all other part types extends from it

            case PART:
                if (StringUtils.isEmpty(partData.getName()))
                    return false;

                if (partData.getBioSafetyLevel() == null)
                    return false;

                if (StringUtils.isEmpty(partData.getStatus()))
                    return false;

                if (StringUtils.isEmpty(partData.getCreator()))
                    return false;

                if (StringUtils.isEmpty(partData.getCreatorEmail()))
                    return false;

                if (StringUtils.isEmpty(partData.getShortDescription()))
                    return false;

                break;
        }

        return true;
    }
}
