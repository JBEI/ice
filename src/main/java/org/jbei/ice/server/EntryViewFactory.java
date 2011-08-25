package org.jbei.ice.server;

import java.util.Date;

import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.PartTipView;
import org.jbei.ice.shared.PlasmidTipView;
import org.jbei.ice.shared.SeedTipView;
import org.jbei.ice.shared.StrainTipView;

public class EntryViewFactory {

    private static void getCommon(EntryDataView view, Entry entry) {

        view.setType(entry.getRecordType());
        view.setPartId(entry.getPartNumbersAsString());
        view.setName(entry.getNamesAsString());
        view.setAlias(entry.getAlias());
        view.setCreator(entry.getCreator());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerId(entry.getOwnerEmail());
        view.setKeywords(entry.getKeywords());
        view.setSummary(entry.getShortDescription());
        Date creationDate = entry.getCreationTime();
        if (creationDate != null)
            view.setCreated(creationDate.getTime());
        Date modification = entry.getModificationTime();
        if (modification != null && modification.getTime() > 0)
            view.setModified(modification.getTime());
    }

    public static EntryDataView createTipView(Entry entry) throws ManagerException {

        Entry.EntryType type = Entry.EntryType.nameToType(entry.getRecordType());
        switch (type) {

        case strain: {
            StrainTipView view = new StrainTipView();

            // common
            getCommon(view, entry);
            //            view.setType(entry.getRecordType());
            //            view.setPartId(entry.getPartNumbersAsString());
            //            view.setName(entry.getNamesAsString());
            //            view.setAlias(entry.getAlias());
            //            view.setCreator(entry.getCreator());
            //            view.setStatus(entry.getStatus());
            //            view.setOwner(entry.getOwner());
            //            view.setOwnerId(entry.getOwnerEmail());
            //            view.setKeywords(entry.getKeywords());
            //            view.setSummary(entry.getShortDescription());
            //
            //            Date creationDate = entry.getCreationTime();
            //            if (creationDate != null)
            //                view.setCreated(creationDate.getTime());
            //            Date modification = entry.getModificationTime();
            //            if (modification != null)
            //                view.setModified(modification.getTime());
            // strain specific
            Strain strain = (Strain) entry;
            view.setHost(strain.getHost());
            view.setGenPhen(strain.getGenotypePhenotype());
            view.setPlasmids(strain.getPlasmids());
            view.setMarkers(strain.getSelectionMarkersAsString());

            boolean hasAttachment = (AttachmentManager.getByEntry(entry).size() > 0);
            view.setHasAttachment(hasAttachment);
            boolean hasSample = (SampleManager.getSamplesByEntry(entry).size() > 0);
            view.setHasSample(hasSample);
            boolean hasSequence = (SequenceManager.getByEntry(entry) != null);
            view.setHasSequence(hasSequence);
            return view;
        }

        case arabidopsis: {

            SeedTipView view = new SeedTipView();
            view.setType(entry.getRecordType());
            view.setPartId(entry.getPartNumbersAsString());
            view.setName(entry.getNamesAsString());
            view.setAlias(entry.getAlias());
            view.setCreator(entry.getCreator());
            view.setStatus(entry.getStatus());
            view.setOwner(entry.getOwner());
            view.setOwnerId(entry.getOwnerEmail());
            view.setKeywords(entry.getKeywords());
            view.setSummary(entry.getShortDescription());
            Date creationDate = entry.getCreationTime();
            if (creationDate != null)
                view.setCreated(creationDate.getTime());
            Date modification = entry.getModificationTime();
            if (modification != null)
                view.setModified(modification.getTime());

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
            PartTipView view = new PartTipView();
            view.setType(entry.getRecordType());
            view.setPartId(entry.getPartNumbersAsString());
            view.setName(entry.getNamesAsString());
            view.setAlias(entry.getAlias());
            view.setCreator(entry.getCreator());
            view.setStatus(entry.getStatus());
            view.setOwner(entry.getOwner());
            view.setOwnerId(entry.getOwnerEmail());
            view.setKeywords(entry.getKeywords());
            view.setSummary(entry.getShortDescription());
            Date creationDate = entry.getCreationTime();
            if (creationDate != null)
                view.setCreated(creationDate.getTime());
            Date modification = entry.getModificationTime();
            if (modification != null)
                view.setModified(modification.getTime());

            Part part = (Part) entry;
            view.setPackagingFormat(part.getPackageFormat().toString());
            return view;
        }

        case plasmid: {
            PlasmidTipView view = new PlasmidTipView();
            view.setType(entry.getRecordType());
            view.setPartId(entry.getPartNumbersAsString());
            view.setName(entry.getNamesAsString());
            view.setAlias(entry.getAlias());
            view.setCreator(entry.getCreator());
            view.setStatus(entry.getStatus());
            view.setOwner(entry.getOwner());
            view.setOwnerId(entry.getOwnerEmail());
            view.setKeywords(entry.getKeywords());
            view.setSummary(entry.getShortDescription());
            view.setMarkers(entry.getSelectionMarkersAsString());
            Date creationDate = entry.getCreationTime();
            if (creationDate != null)
                view.setCreated(creationDate.getTime());
            Date modification = entry.getModificationTime();
            if (modification != null)
                view.setModified(modification.getTime());

            Plasmid plasmid = (Plasmid) entry;
            view.setBackbone(plasmid.getBackbone());
            view.setOrigin(plasmid.getOriginOfReplication());
            view.setPromoters(plasmid.getPromoters());

            return view;
        }

        default:
            return null;
        }
    }

    //    private static String generateDate(long time) {
    //
    //        DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
    //        Date date = new Date(time);
    //        String value = format.format(date);
    //        return value;
    //    }
}
