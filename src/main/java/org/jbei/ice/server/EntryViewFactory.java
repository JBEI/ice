package org.jbei.ice.server;

import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.shared.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;

public class EntryViewFactory {

    private static void getCommon(EntryInfo view, Entry entry) {

        view.setRecordId(String.valueOf(entry.getId()));
        EntryType type = EntryType.nameToType(entry.getRecordType());
        view.setType(type);
        view.setPartId(entry.getPartNumbersAsString());
        view.setName(entry.getNamesAsString());
        view.setAlias(entry.getAlias());
        view.setCreator(entry.getCreator());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerEmail(entry.getOwnerEmail());
        view.setKeywords(entry.getKeywords());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime());
        view.setModificationTime(entry.getModificationTime());
    }

    public static EntryInfo createTipView(Entry entry) {

        Entry.EntryType type = Entry.EntryType.nameToType(entry.getRecordType());
        switch (type) {

        case strain: {
            StrainInfo view = new StrainInfo();

            // common
            getCommon(view, entry);

            // strain specific
            Strain strain = (Strain) entry;
            view.setHost(strain.getHost());
            view.setGenotypePhenotype(strain.getGenotypePhenotype());
            view.setPlasmids(strain.getPlasmids());
            view.setSelectionMarkers(strain.getSelectionMarkersAsString());

            try {
                boolean hasAttachment = (AttachmentManager.getByEntry(entry).size() > 0);
                view.setHasAttachment(hasAttachment);
                boolean hasSample = (SampleManager.getSamplesByEntry(entry).size() > 0);
                view.setHasSample(hasSample);
                boolean hasSequence = (SequenceManager.getByEntry(entry) != null);
                view.setHasSequence(hasSequence);
            } catch (ManagerException e) {
                return null;
            }
            return view;
        }

        case arabidopsis: {

            ArabidopsisSeedInfo view = new ArabidopsisSeedInfo();
            getCommon(view, entry);

            ArabidopsisSeed seed = (ArabidopsisSeed) entry;
            seed.getPlantType();
            seed.getGeneration().toString();
            seed.getHomozygosity();
            seed.getEcotype();
            seed.getParents();
            //            seed.getHarvestDate();

            return view;
        }

        case part: {
            PartInfo view = new PartInfo();

            getCommon(view, entry);

            Part part = (Part) entry;
            view.setPackageFormat(part.getPackageFormat().toString());
            return view;
        }

        case plasmid: {
            PlasmidInfo view = new PlasmidInfo();
            getCommon(view, entry);

            Plasmid plasmid = (Plasmid) entry;
            view.setBackbone(plasmid.getBackbone());
            view.setOriginOfReplication(plasmid.getOriginOfReplication());
            view.setPromoters(plasmid.getPromoters());

            return view;
        }

        default:
            return null;
        }
    }
}
