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

            case PARENTAL_STRAIN:
                return ((Strain) entry).getHost();

            case GENOTYPE_OR_PHENOTYPE:
                return ((Strain) entry).getGenotypePhenotype();

            case PLASMIDS:
                return ((Strain) entry).getPlasmids();

            case BACKBONE:
                return ((Plasmid) entry).getBackbone();

            case ORIGIN_OF_REPLICATION:
                return ((Plasmid) entry).getOriginOfReplication();

            case CIRCULAR:
                return ((Plasmid) entry).getCircular().toString();

            case PROMOTERS:
                return ((Plasmid) entry).getPromoters();

            case REPLICATES_IN:
                return ((Plasmid) entry).getReplicatesIn();

            case HOMOZYGOSITY:
                return ((ArabidopsisSeed) entry).getHomozygosity();

            case ECOTYPE:
                return ((ArabidopsisSeed) entry).getEcotype();

            case HARVEST_DATE:
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                Date date = ((ArabidopsisSeed) entry).getHarvestDate();
                if (date == null)
                    return "";
                return dateFormat.format(date);

            case GENERATION:
                return ((ArabidopsisSeed) entry).getGeneration().toString();

            case SENT_TO_ABRC:
                return ((ArabidopsisSeed) entry).isSentToABRC() ? "Yes" : "No";

            case PLANT_TYPE:
                return ((ArabidopsisSeed) entry).getPlantType().toString();

            case PARENTS:
                return ((ArabidopsisSeed) entry).getParents();
        }
        return "";
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
