package org.jbei.ice.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Parameter;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.CustomField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;
import org.jbei.ice.lib.shared.dto.entry.Visibility;

/**
 * Factory object for converting data transfer objects to model objects
 *
 * @author Hector Plahar
 */
public class InfoToModelFactory {

    public static Entry infoToEntry(PartData info) {
        return infoToEntry(info, null);
    }

    /**
     * @param info  PartData object to converted to Entry
     * @param entry if null, a new entry is created otherwise entry is used
     * @return converted PartData object
     */
    public static Entry infoToEntry(PartData info, Entry entry) {
        EntryType type = info.getType();

        switch (type) {
            case PLASMID:
                Plasmid plasmid;
                if (entry == null) {
                    plasmid = new Plasmid();
                    entry = plasmid;
                } else
                    plasmid = (Plasmid) entry;

                PlasmidData plasmidData = (PlasmidData) info;
                plasmid.setBackbone(plasmidData.getBackbone());
                plasmid.setOriginOfReplication(plasmidData.getOriginOfReplication());
                plasmid.setPromoters(plasmidData.getPromoters());
                plasmid.setReplicatesIn(plasmidData.getReplicatesIn());
                plasmid.setCircular(plasmidData.getCircular());
                break;

            case STRAIN:
                Strain strain;
                if (entry == null) {
                    strain = new Strain();
                    entry = strain;
                } else
                    strain = (Strain) entry;

                StrainData strainData = (StrainData) info;
                strain.setHost(strainData.getHost());
                strain.setGenotypePhenotype(strainData.getGenotypePhenotype());
                break;

            case PART:
                if (entry == null) {
                    entry = new Part();
                }
                break;

            case ARABIDOPSIS:
                ArabidopsisSeed seed;
                if (entry == null) {
                    seed = new ArabidopsisSeed();
                    entry = seed;
                } else
                    seed = (ArabidopsisSeed) entry;

                ArabidopsisSeedData seedData = (ArabidopsisSeedData) info;
                String homozygosity = seedData.getHomozygosity() == null ? "" : seedData.getHomozygosity();
                seed.setHomozygosity(homozygosity);
                seed.setHarvestDate(seedData.getHarvestDate());
                String ecoType = seedData.getEcotype() == null ? "" : seedData.getEcotype();
                seed.setEcotype(ecoType);
                String parents = seedData.getParents() == null ? "" : seedData.getParents();
                seed.setParents(parents);

                if (seedData.getGeneration() != null) {
                    ArabidopsisSeed.Generation generation = ArabidopsisSeed.Generation.valueOf(
                            seedData.getGeneration().name());
                    seed.setGeneration(generation);
                } else {
                    seed.setGeneration(ArabidopsisSeed.Generation.NULL);
                }

                if (seedData.getPlantType() != null) {
                    ArabidopsisSeed.PlantType plantType = ArabidopsisSeed.PlantType.valueOf(
                            seedData.getPlantType().name());
                    seed.setPlantType(plantType);
                } else {
                    seed.setPlantType(ArabidopsisSeed.PlantType.NULL);
                }
                seed.setSentToABRC(seedData.isSentToAbrc());
                break;

            default:
                return null;
        }

        entry = setCommon(entry, info);
        return entry;
    }

    private static Entry setCommon(Entry entry, PartData info) {
        if (entry == null || info == null)
            return null;

        entry.setName(info.getName());
        Set<SelectionMarker> markers = getSelectionMarkers(info.getSelectionMarkers(), entry);
        entry.setSelectionMarkers(markers);
        entry.setReferences(info.getReferences());
        entry.setRecordId(info.getRecordId());
        entry.setPartNumber(info.getPartId());

        if (info.getOwnerEmail() != null) {
            entry.setOwner(info.getOwner());
            entry.setOwnerEmail(info.getOwnerEmail());
        }

        if (info.getCreatorEmail() != null) {
            entry.setCreator(info.getCreator());
            entry.setCreatorEmail(info.getCreatorEmail());
        }

        entry.setStatus(info.getStatus() == null ? "" : info.getStatus());
        entry.setAlias(info.getAlias());
        entry.setBioSafetyLevel(info.getBioSafetyLevel() == null ? Integer.valueOf(0) : info.getBioSafetyLevel());
        entry.setShortDescription(info.getShortDescription());
        entry.setLongDescription(info.getLongDescription());
        entry.setIntellectualProperty(info.getIntellectualProperty());

        Set<Link> links = getLinks(info.getLinks(), entry);
        entry.setLinks(links);

        Visibility visibility = info.getVisibility();
        if (visibility != null)
            entry.setVisibility(visibility.getValue());

        getFundingSources(info.getFundingSource(), info.getPrincipalInvestigator(), entry);
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

    private static Set<EntryFundingSource> getFundingSources(String fundingSourcesStr, String pI, Entry entry) {
        Set<EntryFundingSource> fundingSources = entry.getEntryFundingSources();
        if (fundingSources == null) {
            fundingSources = new HashSet<>();
            entry.setEntryFundingSources(fundingSources);
        }

        if (fundingSourcesStr != null) {
            String[] itemsAsString = fundingSourcesStr.split("\\s*,+\\s*");

            for (int i = 0; i < itemsAsString.length; i++) {
                String currentItem = itemsAsString[i];
                EntryFundingSource entryFundingSource;
                FundingSource fundingSource;

                if (fundingSources.size() > i) {
                    entryFundingSource = (EntryFundingSource) fundingSources.toArray()[i];
                    fundingSource = entryFundingSource.getFundingSource();
                } else {
                    fundingSource = new FundingSource();
                    entryFundingSource = new EntryFundingSource();
                    fundingSources.add(entryFundingSource);
                    entryFundingSource.setFundingSource(fundingSource);
                }

                fundingSource.setFundingSource(currentItem);
                if (pI == null)
                    pI = "";
                fundingSource.setPrincipalInvestigator(pI);
                entryFundingSource.setEntry(entry);
            }
        } else if (pI != null) {
            EntryFundingSource entryFundingSource;
            FundingSource fundingSource;

            if (!fundingSources.isEmpty()) {
                entryFundingSource = (EntryFundingSource) fundingSources.toArray()[0];
                fundingSource = entryFundingSource.getFundingSource();
            } else {
                fundingSource = new FundingSource();
                entryFundingSource = new EntryFundingSource();
                fundingSources.add(entryFundingSource);
                entryFundingSource.setFundingSource(fundingSource);
            }

            fundingSourcesStr = "";
            fundingSource.setFundingSource(fundingSourcesStr);
            fundingSource.setPrincipalInvestigator(pI);
            entryFundingSource.setEntry(entry);
        }

        return fundingSources;
    }

    private static Set<SelectionMarker> getSelectionMarkers(String markerStr, Entry entry) {
        Set<SelectionMarker> existingMarkers = entry.getSelectionMarkers();
        Set<SelectionMarker> markers = new HashSet<>();

        if (existingMarkers == null)
            existingMarkers = new HashSet<>();

        if (markerStr != null) {
            String[] itemsAsString = markerStr.split("\\s*,+\\s*");
            int itemLength = itemsAsString.length;

            for (int i = 0; i < itemLength; i++) {
                String currentItem = itemsAsString[i];
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
        }

        return markers;
    }

    private static Set<Link> getLinks(String linkString, Entry entry) {
        Set<Link> existingLinks = entry.getLinks();
        Set<Link> links = new HashSet<>();

        if (existingLinks == null)
            existingLinks = new HashSet<>();

        if (linkString != null) {
            String[] itemsAsString = linkString.split("\\s*,+\\s*");

            for (int i = 0; i < itemsAsString.length; i++) {
                String currentItem = itemsAsString[i];
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
                Set<EntryFundingSource> fundingSources = entry.getEntryFundingSources();
                EntryFundingSource entryFundingSource;
                FundingSource fundingSource;

                if (fundingSources == null)
                    fundingSources = new HashSet<>();

                if (fundingSources.isEmpty()) {
                    fundingSource = new FundingSource();
                    fundingSource.setFundingSource("");
                    fundingSource.setPrincipalInvestigator(value);
                    entryFundingSource = new EntryFundingSource();
                    fundingSources.add(entryFundingSource);
                    entryFundingSource.setFundingSource(fundingSource);
                } else {
                    entryFundingSource = (EntryFundingSource) fundingSources.toArray()[0];
                    fundingSource = entryFundingSource.getFundingSource();
                    fundingSource.setPrincipalInvestigator(value);
                }

                entry.setEntryFundingSources(fundingSources);
                entryFundingSource.setEntry(entry);
                if (plasmid != null) {
                    plasmid.setEntryFundingSources(fundingSources);
                    entryFundingSource.setEntry(plasmid);
                }
                break;
            }

            case FUNDING_SOURCE: {
                Set<EntryFundingSource> fundingSources = entry.getEntryFundingSources();
                EntryFundingSource entryFundingSource;
                FundingSource fundingSource;

                if (fundingSources == null)
                    fundingSources = new HashSet<>();

                if (fundingSources.isEmpty()) {
                    fundingSource = new FundingSource();
                    fundingSource.setFundingSource(value);
                    fundingSource.setPrincipalInvestigator("");
                    entryFundingSource = new EntryFundingSource();
                    fundingSources.add(entryFundingSource);
                    entryFundingSource.setFundingSource(fundingSource);
                } else {
                    entryFundingSource = (EntryFundingSource) fundingSources.toArray()[0];
                    fundingSource = entryFundingSource.getFundingSource();
                    fundingSource.setFundingSource(value);
                }

                entry.setEntryFundingSources(fundingSources);
                entryFundingSource.setEntry(entry);
                if (plasmid != null) {
                    plasmid.setEntryFundingSources(fundingSources);
                    entryFundingSource.setEntry(plasmid);
                }
                break;
            }

            case IP:
                entry.setIntellectualProperty(value);
                if (plasmid != null)
                    plasmid.setIntellectualProperty(value);
                break;

            case BIOSAFETY_LEVEL:
                Integer level = BioSafetyOption.intValue(value);
                entry.setBioSafetyLevel(level);
                if (plasmid != null) {
                    plasmid.setBioSafetyLevel(level);
                }
                break;

            case NAME:
            case STRAIN_NAME:
                entry.setName(value);
                break;

            case PLASMID_NAME:
                plasmid.setName(value);
                break;

            case ALIAS:
            case STRAIN_ALIAS:
                entry.setAlias(value);
                break;

            case PLASMID_ALIAS:
                plasmid.setAlias(value);
                break;

            case KEYWORDS:
            case STRAIN_KEYWORDS:
                entry.setKeywords(value);
                break;

            case PLASMID_KEYWORDS:
                plasmid.setKeywords(value);
                break;

            case SUMMARY:
            case STRAIN_SUMMARY:
                entry.setShortDescription(value);
                break;

            case PLASMID_SUMMARY:
                plasmid.setShortDescription(value);
                break;

            case NOTES:
            case STRAIN_NOTES:
                entry.setLongDescription(value);
                break;

            case PLASMID_NOTES:
                plasmid.setLongDescription(value);
                break;

            case REFERENCES:
            case STRAIN_REFERENCES:
                entry.setReferences(value);
                break;

            case PLASMID_REFERENCES:
                plasmid.setReferences(value);
                break;

            case LINKS:
            case STRAIN_LINKS:
                HashSet<Link> links = new HashSet<>();
                Link link = new Link();
                link.setLink(value);
                link.setEntry(entry);
                links.add(link);
                entry.setLinks(links);
                break;

            case PLASMID_LINKS:
                links = new HashSet<>();
                link = new Link();
                link.setLink(value);
                link.setEntry(plasmid);
                links.add(link);
                plasmid.setLinks(links);
                break;

            case STATUS:
                entry.setStatus(value);
                if (plasmid != null)
                    plasmid.setStatus(value);
                break;

            case PLASMID_STATUS:
                plasmid.setStatus(value);
                break;

            case SELECTION_MARKERS:
            case STRAIN_SELECTION_MARKERS:
                HashSet<SelectionMarker> markers = new HashSet<>();
                SelectionMarker marker = new SelectionMarker(value, entry);
                markers.add(marker);
                entry.setSelectionMarkers(markers);
                break;

            case PLASMID_SELECTION_MARKERS:
                markers = new HashSet<>();
                marker = new SelectionMarker(value, plasmid);
                markers.add(marker);
                plasmid.setSelectionMarkers(markers);
                break;

            case PARENTAL_STRAIN:
            case GENOTYPE_OR_PHENOTYPE:
            case PLASMIDS:
                entry = infoToStrainForField(entry, value, field);
                break;

            case BACKBONE:
            case PLASMID_BACKBONE:
            case PROMOTERS:
            case PLASMID_PROMOTERS:
            case REPLICATES_IN:
            case PLASMID_REPLICATES_IN:
            case CIRCULAR:
            case ORIGIN_OF_REPLICATION:
            case PLASMID_ORIGIN_OF_REPLICATION:
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

            case PLASMIDS:
                strain.setPlasmids(value);
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
            case PLASMID_BACKBONE:
                plasmid.setBackbone(value);
                return plasmid;

            case PROMOTERS:
            case PLASMID_PROMOTERS:
                plasmid.setPromoters(value);
                return plasmid;

            case REPLICATES_IN:
            case PLASMID_REPLICATES_IN:
                plasmid.setReplicatesIn(value);
                return plasmid;

            case CIRCULAR:
                plasmid.setCircular("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                return plasmid;

            case ORIGIN_OF_REPLICATION:
            case PLASMID_ORIGIN_OF_REPLICATION:
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
                try {
                    Date date = SimpleDateFormat.getDateInstance().parse(value);
                    seed.setHarvestDate(date);
                } catch (ParseException ia) {
                    Logger.error(ia);
                }
                return seed;

            case GENERATION:
                seed.setGeneration(ArabidopsisSeed.Generation.valueOf(value));
                return seed;

            case SENT_TO_ABRC:
                seed.setSentToABRC("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                return seed;

            case PLANT_TYPE:
                seed.setPlantType(ArabidopsisSeed.PlantType.valueOf(value));
                return seed;

            case PARENTS:
                seed.setParents(value);
                return seed;

            default:
                return seed;
        }
    }
}
