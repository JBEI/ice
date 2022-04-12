package org.jbei.ice.storage;

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

            case SEED:
                entry = setSeedFields(info.getSeedData(), new ArabidopsisSeed());
                break;

            case PROTEIN:
                entry = setProteinFields(info.getProteinData(), new Protein());
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

        if (info.getVisibility() != null)
            entry.setVisibility(info.getVisibility().getValue());
        return entry;
    }

    private static Entry setPlasmidFields(PlasmidData plasmidData, Entry entry) {
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

    private static Entry setStrainFields(StrainData strainData, Entry entry) {
        Strain strain = (Strain) entry;
        if (strainData == null)
            return entry;

        if (strainData.getHost() != null)
            strain.setHost(strainData.getHost());

        if (strainData.getGenotypePhenotype() != null)
            strain.setGenotypePhenotype(strainData.getGenotypePhenotype());

        return entry;
    }

    private static Entry setSeedFields(SeedData seedData, Entry entry) {
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

    private static Entry setProteinFields(ProteinData proteinData, Entry entry) {
        Protein protein = (Protein) entry;
        if (proteinData == null)
            return entry;

        if (proteinData.getOrganism() != null)
            protein.setOrganism(proteinData.getOrganism());

        if (proteinData.getFullName() != null)
            protein.setFullName(proteinData.getFullName());

        if (proteinData.getGeneName() != null)
            protein.setGeneName(proteinData.getGeneName());

        if (proteinData.getUploadedFrom() != null)
            protein.setUploadedFrom(proteinData.getUploadedFrom());

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

            case SEED:
                entry = setSeedFields(data.getSeedData(), entry);
                break;

            case PROTEIN:
                entry = setProteinFields(data.getProteinData(), entry);
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
        List<Parameter> parameters = getParameters(info.getParameters(), entry);
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

                if (currentItem.length() > 50)
                    currentItem = currentItem.substring(0, 50);
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
     * Updates the entry based on the field that is specified.
     *
     * @param entry  entry to be updated
     * @param values list of value to set. some fields like selections markers can handle multiple values.
     * @param field  to set
     */
    public static void infoToEntryForField(Entry entry, String[] values, EntryFieldLabel field) {
        if (entry == null || values.length == 0)
            return;

        String value = values[0];

        switch (field) {
            case PI:
                entry.setPrincipalInvestigator(value);
                if (values.length > 1)
                    entry.setPrincipalInvestigatorEmail(values[1]);
                break;

            case PI_EMAIL:
                entry.setPrincipalInvestigatorEmail(value);
                break;

            case CREATOR:
                entry.setCreator(value);
                if (values.length > 1)
                    entry.setCreatorEmail(values[1]);
                break;

            case CREATOR_EMAIL:
                entry.setCreatorEmail(value);
                break;

            case FUNDING_SOURCE:
                entry.setFundingSource(value);
                break;

            case IP:
                entry.setIntellectualProperty(value);
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
                entry.setBioSafetyLevel(level);
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
                for (String linkValue : values) {
                    Link link = new Link();
                    link.setLink(linkValue);
                    link.setEntry(entry);
                    links.add(link);
                }
                entry.setLinks(links);
                break;

            case STATUS:
                entry.setStatus(value);
                break;

            case SELECTION_MARKERS:
                HashSet<SelectionMarker> markers = new HashSet<>();
                for (String markerValue : values) {
                    markers.add(new SelectionMarker(markerValue, entry));
                }
                entry.setSelectionMarkers(markers);
                break;

            case HOST:
            case GENOTYPE_OR_PHENOTYPE:
                infoToStrainForField(entry, value, field);
                break;

            case BACKBONE:
            case ORIGIN_OF_REPLICATION:
            case CIRCULAR:
            case PROMOTERS:
            case REPLICATES_IN:
                infoToPlasmidForField(entry, value, field);
                break;

            case HOMOZYGOSITY:
            case ECOTYPE:
            case HARVEST_DATE:
            case GENERATION:
            case SENT_TO_ABRC:
            case PLANT_TYPE:
            case PARENTS:
                infoToSeedForField(entry, value, field);
                break;

            case ORGANISM:
            case FULL_NAME:
            case GENE_NAME:
            case UPLOADED_FROM:
                infoToProteinForField(entry, value, field);
                break;

            default:
                break;

        }
    }

    private static void infoToStrainForField(Entry entry, String value, EntryFieldLabel field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString()))
            return;

        Strain strain = (Strain) entry;

        switch (field) {
            case HOST:
                strain.setHost(value);
                return;

            case GENOTYPE_OR_PHENOTYPE:
                strain.setGenotypePhenotype(value);
                break;
        }
    }

    private static void infoToPlasmidForField(Entry entry, String value, EntryFieldLabel field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.PLASMID.toString()))
            return;

        Plasmid plasmid = (Plasmid) entry;

        switch (field) {
            case BACKBONE:
                plasmid.setBackbone(value);
                return;

            case PROMOTERS:
                plasmid.setPromoters(value);
                return;

            case REPLICATES_IN:
                plasmid.setReplicatesIn(value);
                return;

            case CIRCULAR:
                plasmid.setCircular("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "circular".equalsIgnoreCase(value));
                return;

            case ORIGIN_OF_REPLICATION:
                plasmid.setOriginOfReplication(value);
                return;

            default:
        }
    }

    private static void infoToSeedForField(Entry entry, String value, EntryFieldLabel field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.SEED.toString()))
            return;

        ArabidopsisSeed seed = (ArabidopsisSeed) entry;

        switch (field) {
            case HOMOZYGOSITY:
                seed.setHomozygosity(value);
                return;

            case ECOTYPE:
                seed.setEcotype(value);
                return;

            case HARVEST_DATE:
                if (value != null && !value.isEmpty()) {
                    try {
                        Date date = SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse(value);
                        seed.setHarvestDate(date);
                    } catch (ParseException ia) {
                        Logger.error(ia);
                    }
                }
                return;

            case GENERATION:
                seed.setGeneration(Generation.fromString(value));
                return;

            case SENT_TO_ABRC:
                seed.setSentToABRC("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                return;

            case PLANT_TYPE:
                seed.setPlantType(PlantType.fromString(value));
                return;

            case PARENTS:
                seed.setParents(value);
                return;

            default:
        }
    }

    private static void infoToProteinForField(Entry entry, String value, EntryFieldLabel field) {
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.PROTEIN.toString()))
            return;

        Protein protein = (Protein) entry;

        switch (field) {
            case ORGANISM:
                protein.setOrganism(value);
                return;

            case FULL_NAME:
                protein.setFullName(value);
                return;

            case GENE_NAME:
                protein.setGeneName(value);
                return;

            case UPLOADED_FROM:
                protein.setUploadedFrom(value);
                return;

            default:
        }
    }
}
