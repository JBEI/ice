package org.jbei.ice.server;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.PlasmidInfo;

/**
 * Factory object for converting data transfer objects to model
 * 
 * @author Hector Plahar
 * 
 */
public class InfoToModelFactory {

    public static Entry infoToEntry(EntryInfo info) {

        EntryType type = info.getType();
        Entry entry;

        switch (type) {
        case PLASMID:
            entry = new Plasmid();
            if (info.getSelectionMarkers() != null && !info.getSelectionMarkers().isEmpty()) {
                String[] itemsAsString = info.getSelectionMarkers().split("\\s*,+\\s*");
                HashSet<SelectionMarker> markers = new HashSet<SelectionMarker>();

                for (int i = 0; i < itemsAsString.length; i++) {
                    String currentItem = itemsAsString[i];
                    if (!currentItem.trim().isEmpty()) {
                        SelectionMarker marker = new SelectionMarker();
                        marker.setName(currentItem);
                        markers.add(marker);
                    }
                }

                entry.setSelectionMarkers(markers); // TODO : looks like selection marker is a common field
            }

            Plasmid plasmid = (Plasmid) entry;
            PlasmidInfo plasmidInfo = (PlasmidInfo) info;

            plasmid.setBackbone(plasmidInfo.getBackbone());
            plasmid.setOriginOfReplication(plasmidInfo.getOriginOfReplication());
            plasmid.setPromoters(plasmidInfo.getPromoters());
            plasmid.setCircular(plasmidInfo.getCircular());
            break;

        default:
            return null;
        }

        entry = setCommon(entry, info);
        return entry;
    }

    private static Entry setCommon(Entry entry, EntryInfo info) {
        entry.setNames(getNames(info.getName()));
        entry.setOwner(info.getOwner());
        entry.setOwnerEmail(info.getOwnerEmail());
        entry.setCreator(info.getCreator());
        entry.setCreatorEmail(info.getCreatorEmail());
        entry.setStatus(info.getStatus());
        entry.setAlias(info.getAlias());
        entry.setBioSafetyLevel(info.getBioSafetyLevel());
        entry.setShortDescription(info.getShortDescription());
        entry.setLongDescription(info.getLongDescription());
        entry.setLongDescriptionType("text"); // TODO

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

    private static HashSet<Name> getNames(String nameStr) {
        HashSet<Name> names = new HashSet<Name>();
        if (nameStr == null || nameStr.isEmpty())
            return names;

        String[] items = nameStr.split("\\s*,+\\s*");
        for (String item : items) {
            if (!item.isEmpty()) {
                Name name = new Name();
                name.setName(item);
            }
        }

        return names;
    }
}
