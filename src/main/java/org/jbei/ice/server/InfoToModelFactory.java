package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.Parameter;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Part.AssemblyStandard;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;
import org.jbei.ice.shared.dto.Visibility;

/**
 * Factory object for converting data transfer objects to model
 *
 * @author Hector Plahar
 */
public class InfoToModelFactory {

    public static Entry infoToEntry(EntryInfo info) {
        return infoToEntry(info, null);
    }

    /**
     * @param info  EntryInfo object to converted to Entry
     * @param entry if null, a new entry is created otherwise entry is used
     * @return converted EntryInfo object
     */
    public static Entry infoToEntry(EntryInfo info, Entry entry) {

        EntryType type = info.getType();

        switch (type) {
            case PLASMID:
                Plasmid plasmid;
                if (entry == null) {
                    plasmid = new Plasmid();
                    entry = plasmid;
                } else
                    plasmid = (Plasmid) entry;

                plasmid.setRecordType(EntryType.PLASMID.getName());
                PlasmidInfo plasmidInfo = (PlasmidInfo) info;

                plasmid.setBackbone(plasmidInfo.getBackbone());
                plasmid.setOriginOfReplication(plasmidInfo.getOriginOfReplication());
                plasmid.setPromoters(plasmidInfo.getPromoters());
                plasmid.setCircular(plasmidInfo.getCircular() == null ? false : plasmidInfo
                        .getCircular());

                break;

            case STRAIN:
                Strain strain;
                if (entry == null) {
                    strain = new Strain();
                    entry = strain;
                } else
                    strain = (Strain) entry;

                strain.setRecordType(EntryType.STRAIN.getName());
                StrainInfo strainInfo = (StrainInfo) info;

                strain.setHost(strainInfo.getHost());
                strain.setGenotypePhenotype(strainInfo.getGenotypePhenotype());
                strain.setPlasmids(strainInfo.getPlasmids());
                break;

            case PART:
                Part part;
                if (entry == null) {
                    part = new Part();
                    entry = part;
                } else
                    part = (Part) entry;
                part.setRecordType(EntryType.PART.getName());

                // default is RAW until sequence is supplied.
                part.setPackageFormat(AssemblyStandard.RAW);
                break;

            case ARABIDOPSIS:
                ArabidopsisSeed seed;
                if (entry == null) {
                    seed = new ArabidopsisSeed();
                    entry = seed;
                } else
                    seed = (ArabidopsisSeed) entry;

                seed.setRecordType(EntryType.ARABIDOPSIS.getName());
                ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) info;

                seed.setHomozygosity(seedInfo.getHomozygosity());
                seed.setHarvestDate(seedInfo.getHarvestDate());
                seed.setEcotype(seedInfo.getEcotype());
                seed.setParents(seedInfo.getParents());

                if (seedInfo.getGeneration() != null) {
                    ArabidopsisSeed.Generation generation = ArabidopsisSeed.Generation.valueOf(seedInfo
                                                                                                       .getGeneration()
                                                                                                       .name());
                    seed.setGeneration(generation);
                }

                if (seedInfo.getPlantType() != null) {
                    ArabidopsisSeed.PlantType plantType = ArabidopsisSeed.PlantType.valueOf(seedInfo
                                                                                                    .getPlantType()
                                                                                                    .name());
                    seed.setPlantType(plantType);
                }
                break;

            default:
                return null;
        }

        entry = setCommon(entry, info);
        return entry;
    }

    private static Entry setCommon(Entry entry, EntryInfo info) {
        if (entry == null || info == null)
            return null;

        Set<Name> names = getNames(info.getName(), entry);
        entry.setNames(names);
        Set<SelectionMarker> markers = getSelectionMarkers(info.getSelectionMarkers(), entry);
        entry.setSelectionMarkers(markers);
        entry.setReferences(info.getReferences());
        entry.setRecordId(info.getRecordId());
        entry.setOwner(info.getOwner());
        entry.setOwnerEmail(info.getOwnerEmail());
        entry.setCreator(info.getCreator());
        entry.setCreatorEmail(info.getCreatorEmail());
        entry.setStatus(info.getStatus() == null ? "" : info.getStatus());
        entry.setAlias(info.getAlias());
        entry.setBioSafetyLevel(info.getBioSafetyLevel() == null ? new Integer(0) : info
                .getBioSafetyLevel());
        entry.setShortDescription(info.getShortDescription());
        entry.setLongDescription(info.getLongDescription());
        entry.setLongDescriptionType(info.getLongDescriptionType() != null ? info
                .getLongDescriptionType() : "text");
        entry.setIntellectualProperty(info.getIntellectualProperty());
        entry.setVersionId(info.getVersionId());
        Set<Link> links = getLinks(info.getLinks(), entry);
        entry.setLinks(links);

        Visibility visibility = info.getVisibility();
        if (visibility != null)
            entry.setVisibility(visibility.getValue());

        getFundingSources(info.getFundingSource(), info.getPrincipalInvestigator(), entry);
//        entry.setEntryFundingSources(entryFundingSources);
        entry.setKeywords(info.getKeywords());

        // parameters 
        List<Parameter> parameters = getParameters(info.getParameters(), entry);
        entry.setParameters(parameters);

        return entry;
    }

    private static List<Parameter> getParameters(ArrayList<ParameterInfo> infos, Entry entry) {
        List<Parameter> parameters = new ArrayList<Parameter>();

        if (infos == null)
            return parameters;

        for (ParameterInfo info : infos) {
            Parameter param = new Parameter();
            Parameter.ParameterType type = Parameter.ParameterType.valueOf(info.getType().name());
            param.setParameterType(type);
            param.setEntry(entry);
            param.setKey(info.getName());
            param.setValue(info.getValue());
            parameters.add(param);
        }
        return parameters;
    }

    private static Set<EntryFundingSource> getFundingSources(String fundingSourcesStr, String pI,
            Entry entry) {

        Set<EntryFundingSource> fundingSources = entry.getEntryFundingSources();
        if (fundingSources == null) {
            fundingSources = new HashSet<EntryFundingSource>();
            entry.setEntryFundingSources(fundingSources);
        }

        if (fundingSourcesStr != null && !fundingSourcesStr.isEmpty()) {
            String[] itemsAsString = fundingSourcesStr.split("\\s*,+\\s*");

            for (int i = 0; i < itemsAsString.length; i++) {
                String currentItem = itemsAsString[i];
                if (!currentItem.trim().isEmpty()) {
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
            }
        } else if (pI != null && !pI.trim().isEmpty()) {

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

            if (fundingSourcesStr == null)
                fundingSourcesStr = "";
            fundingSource.setFundingSource(fundingSourcesStr);
            fundingSource.setPrincipalInvestigator(pI);
            entryFundingSource.setEntry(entry);
        }

        return fundingSources;
    }

    private static Set<SelectionMarker> getSelectionMarkers(String markerStr, Entry entry) {

        Set<SelectionMarker> markers = entry.getSelectionMarkers();
        if (markers == null)
            markers = new HashSet<SelectionMarker>();

        if (markerStr != null && !markerStr.isEmpty()) {
            String[] itemsAsString = markerStr.split("\\s*,+\\s*");

            for (int i = 0; i < itemsAsString.length; i++) {
                String currentItem = itemsAsString[i];
                if (!currentItem.trim().isEmpty()) {
                    SelectionMarker marker;

                    if (markers.size() > i) {
                        marker = (SelectionMarker) markers.toArray()[i];
                    } else {
                        marker = new SelectionMarker();
                        markers.add(marker);
                    }

                    marker.setName(currentItem);
                    marker.setEntry(entry);
                }
            }
        }

        return markers;
    }

    private static Set<Link> getLinks(String linkString, Entry entry) {
        Set<Link> links = entry.getLinks();
        if (links == null)
            links = new HashSet<Link>();

        if (linkString != null && !linkString.isEmpty()) {
            String[] itemsAsString = linkString.split("\\s*,+\\s*");

            for (int i = 0; i < itemsAsString.length; i++) {
                String currentItem = itemsAsString[i];
                if (!currentItem.trim().isEmpty()) {
                    Link link;

                    if (links.size() > i) {
                        link = (Link) links.toArray()[i];
                    } else {
                        link = new Link();
                        links.add(link);
                    }
                    link.setLink(currentItem);
                    link.setEntry(entry);
                }
            }
        }

        return links;
    }

    private static Set<Name> getNames(String nameStr, Entry entry) {
        Set<Name> names = entry.getNames();
        if (names == null)
            names = new HashSet<Name>();

        if (nameStr == null || nameStr.isEmpty())
            return names;

        String[] items = nameStr.split("\\s*,+\\s*");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            if (!item.trim().isEmpty()) {
                Name name;

                if (names.size() > i) {
                    name = (Name) names.toArray()[i];
                } else {
                    name = new Name();
                    names.add(name);
                }
                name.setName(item);
                name.setEntry(entry);
            }
        }

        return names;
    }
}
