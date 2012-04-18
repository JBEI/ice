package org.jbei.ice.server;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;
import org.jbei.ice.web.common.CommaSeparatedField;

/**
 * Factory object for converting data transfer objects to model
 * 
 * @author Hector Plahar
 * 
 */
public class InfoToModelFactory {

    /**
     * 
     * @param info
     * @param entry
     *            if null, a new entry is created otherwise entry is used
     * @return
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

            plasmid.setRecordType(Entry.PLASMID_ENTRY_TYPE);
            PlasmidInfo plasmidInfo = (PlasmidInfo) info;

            plasmid.setBackbone(plasmidInfo.getBackbone());
            plasmid.setOriginOfReplication(plasmidInfo.getOriginOfReplication());
            plasmid.setPromoters(plasmidInfo.getPromoters());
            plasmid.setCircular(plasmidInfo.getCircular());

            break;

        case STRAIN:
            Strain strain;
            if (entry == null) {
                strain = new Strain();
                entry = strain;
            } else
                strain = (Strain) entry;

            strain.setRecordType(Entry.STRAIN_ENTRY_TYPE);
            StrainInfo strainInfo = (StrainInfo) info;

            strain.setHost(strainInfo.getHost());
            strain.setGenotypePhenotype(strain.getGenotypePhenotype());
            strain.setPlasmids(strainInfo.getPlasmids());

            entry = strain;
            break;

        case PART:
            Part part;
            if (entry == null) {
                part = new Part();
                entry = part;
            } else
                part = (Part) entry;
            part.setRecordType(Entry.PART_ENTRY_TYPE);

            // default is RAW until sequence is supplied.
            part.setPackageFormat(AssemblyStandard.RAW);

            entry = part;
            break;

        case ARABIDOPSIS:
            ArabidopsisSeed seed;
            if (entry == null) {
                seed = new ArabidopsisSeed();
                entry = seed;
            } else
                seed = (ArabidopsisSeed) entry;

            seed.setRecordType(Entry.ARABIDOPSIS_SEED_ENTRY_TYPE);
            ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) info;

            seed.setHomozygosity(seedInfo.getHomozygosity());
            seed.setHarvestDate(seedInfo.getHarvestDate());
            seed.setEcotype(seedInfo.getEcotype());
            seed.setParents(seedInfo.getParents());

            if (seedInfo.getGeneration() != null) {
                ArabidopsisSeed.Generation generation = ArabidopsisSeed.Generation.valueOf(seedInfo
                        .getGeneration().name());
                seed.setGeneration(generation);
            }

            if (seedInfo.getPlantType() != null) {
                ArabidopsisSeed.PlantType plantType = ArabidopsisSeed.PlantType.valueOf(seedInfo
                        .getPlantType().name());
                seed.setPlantType(plantType);
            }

            entry = seed;
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

        HashSet<Name> names = getNames(info.getName(), entry);
        entry.setNames(names);
        HashSet<SelectionMarker> markers = getSelectionMarkers(info.getSelectionMarkers(), entry);
        entry.setSelectionMarkers(markers);
        entry.setOwner(info.getOwner());
        entry.setRecordId(info.getRecordId());
        entry.setOwnerEmail(info.getOwnerEmail());
        entry.setCreator(info.getCreator());
        entry.setCreatorEmail(info.getCreatorEmail());
        entry.setStatus(info.getStatus());
        entry.setAlias(info.getAlias());
        entry.setBioSafetyLevel(info.getBioSafetyLevel());
        entry.setShortDescription(info.getShortDescription());
        entry.setLongDescription(info.getLongDescription());
        entry.setLongDescriptionType(info.getLongDescriptionType());
        entry.setVersionId(info.getVersionId());
        CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(Link.class, "getLink",
                "setLink");
        linksField.setString(info.getLinks());
        entry.setLinks(linksField.getItemsAsSet());

        FundingSource fundingSource = new FundingSource();
        fundingSource.setFundingSource((info.getFundingSource() != null) ? info.getFundingSource()
                : "");
        fundingSource.setPrincipalInvestigator(info.getPrincipalInvestigator());
        EntryFundingSource newEntryFundingSource = new EntryFundingSource();
        newEntryFundingSource.setEntry(entry);
        newEntryFundingSource.setFundingSource(fundingSource);
        Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
        entryFundingSources.add(newEntryFundingSource);
        entry.setEntryFundingSources(entryFundingSources);

        return entry;
    }

    private static HashSet<SelectionMarker> getSelectionMarkers(String markerStr, Entry entry) {

        HashSet<SelectionMarker> markers = new HashSet<SelectionMarker>();

        if (markerStr != null && !markerStr.isEmpty()) {
            String[] itemsAsString = markerStr.split("\\s*,+\\s*");

            for (int i = 0; i < itemsAsString.length; i++) {
                String currentItem = itemsAsString[i];
                if (!currentItem.trim().isEmpty()) {
                    SelectionMarker marker = new SelectionMarker();
                    marker.setName(currentItem);
                    marker.setEntry(entry);
                    markers.add(marker);
                }
            }
        }

        return markers;
    }

    private static HashSet<Name> getNames(String nameStr, Entry entry) {
        HashSet<Name> names = new HashSet<Name>();
        if (nameStr == null || nameStr.isEmpty())
            return names;

        String[] items = nameStr.split("\\s*,+\\s*");
        for (String item : items) {
            if (!item.isEmpty()) {
                Name name = new Name();
                name.setName(item);
                name.setEntry(entry);
                names.add(name);
            }
        }

        return names;
    }
}
