package org.jbei.ice.servlet;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.storage.model.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Factory object for converting data transfer objects to model objects
 *
 * @author Hector Plahar
 */
public class InfoToModelFactory {

    public static Entry infoToEntry(PartData info) {
        EntryType type = info.getType();
        Entry entry;

        switch (type) {
            case PLASMID:
                entry = setPlasmidFields(info.getPlasmidData(), new Plasmid());
                break;

            case STRAIN:
                entry = setStrainFields(info.getStrainData(), new Strain());
                break;

            case ARABIDOPSIS:
                entry = setSeedFields(info.getArabidopsisSeedData(), new ArabidopsisSeed());
                break;

            case PART:
            default:
                entry = new Part();
                break;
        }

        if (entry == null)
            throw new IllegalArgumentException("Could not create entry from info object");

        // common fields
        if (StringUtils.isEmpty(info.getRecordId()))
            entry.setRecordId(UUID.randomUUID().toString());
        else
            entry.setRecordId(info.getRecordId());

        entry.setVersionId(entry.getRecordId());
        if (info.getCreationTime() == 0)
            entry.setCreationTime(new Date());
        else
            entry.setCreationTime(new Date(info.getCreationTime()));

        entry.setModificationTime(entry.getCreationTime());
        entry = setCommon(entry, info);
        return entry;
    }

    protected static Entry setPlasmidFields(PlasmidData plasmidData, Entry entry) {
        if (plasmidData == null)
            return entry;

        Plasmid plasmid = (Plasmid) entry;

        if (plasmidData.getBackbone() != null)
            plasmid.setBackbone(plasmidData.getBackbone());

        if (plasmidData.getOriginOfReplication() != null)
            plasmid.setOriginOfReplication(plasmidData.getOriginOfReplication());

        if (plasmidData.getPromoters() != null)
            plasmid.setPromoters(plasmidData.getPromoters());

        if (plasmidData.getReplicatesIn() != null)
            plasmid.setReplicatesIn(plasmidData.getReplicatesIn());

        if (plasmidData.getCircular() != null)
            plasmid.setCircular(plasmidData.getCircular());

        return entry;
    }

    protected static Entry setStrainFields(StrainData strainData, Entry entry) {
        Strain strain = (Strain) entry;
        if (strainData == null)
            return entry;

        if (strainData.getHost() != null)
            strain.setHost(strainData.getHost());

        if (strainData.getGenotypePhenotype() != null)
            strain.setGenotypePhenotype(strainData.getGenotypePhenotype());

        return entry;
    }

    protected static Entry setSeedFields(ArabidopsisSeedData seedData, Entry entry) {
        ArabidopsisSeed seed = (ArabidopsisSeed) entry;
        if (seedData == null)
            return entry;

        if (seedData.getHomozygosity() != null)
            seed.setHomozygosity(seedData.getHomozygosity());

        if (StringUtils.isNotEmpty(seedData.getHarvestDate())) {
            DateFormat format = new SimpleDateFormat("MM/dd/YYYY");
            try {
                Date date = format.parse(seedData.getHarvestDate());
                seed.setHarvestDate(date);
            } catch (ParseException e) {
                Logger.error("Could not parse date " + seedData.getHarvestDate());
                return null;
            }
        }

        String ecoType = seedData.getEcotype() == null ? "" : seedData.getEcotype();
        seed.setEcotype(ecoType);
        String parents = seedData.getSeedParents() == null ? "" : seedData.getSeedParents();

        seed.setParents(parents);

        if (seedData.getGeneration() != null) {
            Generation generation = Generation.fromString(seedData.getGeneration().name());
            seed.setGeneration(generation);
        } else {
            seed.setGeneration(Generation.UNKNOWN);
        }

        if (seedData.getPlantType() != null) {
            PlantType plantType = PlantType.fromString(seedData.getPlantType().name());
            seed.setPlantType(plantType);
        } else {
            seed.setPlantType(PlantType.NULL);
        }
        seed.setSentToABRC(seedData.isSentToAbrc());
        return entry;
    }

    /**
     * sets the corresponding fields in data only if they are not null
     *
     * @param data  PartData object to converted to Entry
     * @param entry if null, a new entry is created otherwise entry is used
     * @return converted PartData object
     */
    public static Entry updateEntryField(PartData data, Entry entry) {
        EntryType type = data.getType();
        if (type == null)
            return entry;

        switch (type) {
            case PLASMID:
                entry = setPlasmidFields(data.getPlasmidData(), entry);
                break;

            case STRAIN:
                entry = setStrainFields(data.getStrainData(), entry);
                break;

            case PART:
                break;

            case ARABIDOPSIS:
                entry = setSeedFields(data.getArabidopsisSeedData(), entry);
                break;
        }

        entry = setCommon(entry, data);
        return entry;
    }

    private static Entry setCommon(Entry entry, PartData info) {
        if (entry == null || info == null)
            return null;

        if (info.getName() != null)
            entry.setName(info.getName());

        if (info.getSelectionMarkers() != null) {
            Set<SelectionMarker> markers = getSelectionMarkers(info.getSelectionMarkers(), entry);
            entry.setSelectionMarkers(markers);
        }

        if (info.getReferences() != null)
            entry.setReferences(info.getReferences());

        if (StringUtils.isBlank(entry.getPartNumber()))
            entry.setPartNumber(info.getPartId());

        Date currentTime = new Date();
        if (entry.getCreationTime() == null)
            entry.setCreationTime(currentTime);

        entry.setModificationTime(currentTime);

        if (info.getOwnerEmail() != null) {
            entry.setOwnerEmail(info.getOwnerEmail());
        }

        if (info.getOwner() != null)
            entry.setOwner(info.getOwner());

        if (info.getCreatorEmail() != null) {
            entry.setCreatorEmail(info.getCreatorEmail());
        }

        if (info.getCreator() != null)
            entry.setCreator(info.getCreator());

        if (info.getStatus() == null) {
            if (StringUtils.isBlank(entry.getStatus()))
                entry.setStatus("");
        } else
            entry.setStatus(info.getStatus());

        if (info.getAlias() != null)
            entry.setAlias(info.getAlias());

        if (info.getBioSafetyLevel() == null) {
            if (entry.getBioSafetyLevel() == null)
                entry.setBioSafetyLevel(0);
        } else
            entry.setBioSafetyLevel(info.getBioSafetyLevel());

        if (info.getShortDescription() != null)
            entry.setShortDescription(info.getShortDescription());
        if (info.getLongDescription() != null)
            entry.setLongDescription(info.getLongDescription());
        if (info.getIntellectualProperty() != null)
            entry.setIntellectualProperty(info.getIntellectualProperty());

        Set<Link> links = getLinks(info.getLinks(), entry);
        entry.setLinks(links);

        Visibility visibility = info.getVisibility();
        if (visibility != null)
            entry.setVisibility(visibility.getValue());

        // checking for null instead of blank since it could be cleared
        if (info.getFundingSource() != null)
            entry.setFundingSource(info.getFundingSource());
        if (info.getPrincipalInvestigator() != null)
            entry.setPrincipalInvestigator(info.getPrincipalInvestigator());
        if (info.getPrincipalInvestigatorEmail() != null)
            entry.setPrincipalInvestigatorEmail(info.getPrincipalInvestigatorEmail());

        if (info.getKeywords() != null)
            entry.setKeywords(info.getKeywords());

        // parameters 
        List<Parameter> parameters = getParameters(info.getCustomFields(), entry);
        entry.setParameters(parameters);
        return entry;
    }

    private static List<Parameter> getParameters(ArrayList<CustomField> infos, Entry entry) {
        List<Parameter> parameters = new ArrayList<>();

        if (infos == null)
            return parameters;

        for (CustomField info : infos) {
            Parameter param = new Parameter();
            param.setEntry(entry);
            param.setKey(info.getName());
            param.setValue(info.getValue());
            parameters.add(param);
        }
        return parameters;
    }

    private static Set<SelectionMarker> getSelectionMarkers(ArrayList<String> markerStr, Entry entry) {
        Set<SelectionMarker> existingMarkers = entry.getSelectionMarkers();
        Set<SelectionMarker> markers = new HashSet<>();

        if (existingMarkers == null)
            existingMarkers = new HashSet<>();

        if (markerStr != null) {
            int itemLength = markerStr.size();

            for (int i = 0; i < itemLength; i++) {
                String currentItem = markerStr.get(i);
                SelectionMarker marker;

                if (existingMarkers.size() > i) {
                    marker = (SelectionMarker) existingMarkers.toArray()[i];
                } else {
                    marker = new SelectionMarker();
                    existingMarkers.add(marker);
                }

                marker.setName(currentItem);
                marker.setEntry(entry);
                markers.add(marker);
            }
        } else
            return null;

        return markers;
    }

    private static Set<Link> getLinks(ArrayList<String> linkList, Entry entry) {
        Set<Link> existingLinks = entry.getLinks();
        Set<Link> links = new HashSet<>();

        if (existingLinks == null)
            existingLinks = new HashSet<>();

        if (linkList == null)
            return existingLinks;

        for (int i = 0; i < linkList.size(); i++) {
            String currentItem = linkList.get(i);
            Link link;

            if (existingLinks.size() > i) {
                link = (Link) existingLinks.toArray()[i];
            } else {
                link = new Link();
                existingLinks.add(link);
            }
            link.setLink(currentItem);
            link.setEntry(entry);
            links.add(link);
        }

        return links;
    }

    /**
     * Updates the entry based on the field that is specified. Mainly created for use by the bulk import auto update
     *
     * @param entry   entry to be updated
     * @param plasmid should be set if updating strain with plasmid
     * @param value   value to be set
     * @param field   to set
     * @return updated entry array containing both entry and plasmid. if plasmid is null only entry is returned
     */
    public static Entry[] infoToEntryForField(Entry entry, Entry plasmid, String value, EntryField field) {
        switch (field) {
            case PI: {
                entry.setPrincipalInvestigator(value);
                if (plasmid != null)
                    plasmid.setPrincipalInvestigator(value);
                break;
            }

            case PI_EMAIL: {
                entry.setPrincipalInvestigatorEmail(value);
                break;
            }

            case FUNDING_SOURCE: {
                entry.setFundingSource(value);
                if (plasmid != null)
                    plasmid.setFundingSource(value);
                break;
            }

            case IP:
                entry.setIntellectualProperty(value);
                if (plasmid != null)
                    plasmid.setIntellectualProperty(value);
                break;

            case BIO_SAFETY_LEVEL:
                Integer level = BioSafetyOption.intValue(value);
                if (level == null) {
                    if (value.contains("1"))
                        level = 1;
                    else if (value.contains("2"))
                        level = 2;
                    else
                        break;
                }
                entry.setBioSafetyLevel(level);
                if (plasmid != null) {
                    plasmid.setBioSafetyLevel(level);
                }
                break;

            case NAME:
                entry.setName(value);
                break;

            case ALIAS:
                entry.setAlias(value);
                break;

            case KEYWORDS:
                entry.setKeywords(value);
                break;

            case SUMMARY:
                entry.setShortDescription(value);
                break;

            case NOTES:
                entry.setLongDescription(value);
                break;

            case REFERENCES:
                entry.setReferences(value);
                break;

            case LINKS:
                HashSet<Link> links = new HashSet<>();
                Link link = new Link();
                link.setLink(value);
                link.setEntry(entry);
                links.add(link);
                entry.setLinks(links);
                break;

            case STATUS:
                entry.setStatus(value);
                if (plasmid != null)
                    plasmid.setStatus(value);
                break;

            case SELECTION_MARKERS:
                HashSet<SelectionMarker> markers = new HashSet<>();
                SelectionMarker marker = new SelectionMarker(value, entry);
                markers.add(marker);
                entry.setSelectionMarkers(markers);
                break;

            case PARENTAL_STRAIN:
            case GENOTYPE_OR_PHENOTYPE:
                entry = infoToStrainForField(entry, value, field);
                break;

            case BACKBONE:
            case ORIGIN_OF_REPLICATION:
            case CIRCULAR:
            case PROMOTERS:
            case REPLICATES_IN:
                entry = infoToPlasmidForField(entry, value, field);
                break;

            case HOMOZYGOSITY:
            case ECOTYPE:
            case HARVEST_DATE:
            case GENERATION:
            case SENT_TO_ABRC:
            case PLANT_TYPE:
            case PARENTS:
                entry = infoToSeedForField(entry, value, field);
                break;
            default:
                break;
        }
        if (plasmid == null)
            return new Entry[]{entry};

        return new Entry[]{entry, plasmid};
    }

    private static Entry infoToStrainForField(Entry entry, String value, EntryField field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString()))
            return entry;

        Strain strain = (Strain) entry;

        switch (field) {
            case PARENTAL_STRAIN:
                strain.setHost(value);
                return strain;

            case GENOTYPE_OR_PHENOTYPE:
                strain.setGenotypePhenotype(value);
                return strain;

            default:
                return strain;
        }
    }

    private static Entry infoToPlasmidForField(Entry entry, String value, EntryField field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.PLASMID.toString()))
            return entry;

        Plasmid plasmid = (Plasmid) entry;

        switch (field) {
            case BACKBONE:
                plasmid.setBackbone(value);
                return plasmid;

            case PROMOTERS:
                plasmid.setPromoters(value);
                return plasmid;

            case REPLICATES_IN:
                plasmid.setReplicatesIn(value);
                return plasmid;

            case CIRCULAR:
                plasmid.setCircular("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "circular".equalsIgnoreCase(value));
                return plasmid;

            case ORIGIN_OF_REPLICATION:
                plasmid.setOriginOfReplication(value);
                return plasmid;

            default:
                return plasmid;
        }
    }

    private static Entry infoToSeedForField(Entry entry, String value, EntryField field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.ARABIDOPSIS.toString()))
            return entry;

        ArabidopsisSeed seed = (ArabidopsisSeed) entry;

        switch (field) {
            case HOMOZYGOSITY:
                seed.setHomozygosity(value);
                return seed;

            case ECOTYPE:
                seed.setEcotype(value);
                return seed;

            case HARVEST_DATE:
                if (value != null && !value.isEmpty()) {
                    try {
                        Date date = SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse(value);
                        seed.setHarvestDate(date);
                    } catch (ParseException ia) {
                        Logger.error(ia);
                    }
                }
                return seed;

            case GENERATION:
                seed.setGeneration(Generation.fromString(value));
                return seed;

            case SENT_TO_ABRC:
                seed.setSentToABRC("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                return seed;

            case PLANT_TYPE:
                seed.setPlantType(PlantType.fromString(value));
                return seed;

            case PARENTS:
                seed.setParents(value);
                return seed;

            default:
                return seed;
        }
    }
}
