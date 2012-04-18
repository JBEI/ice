package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.StrainInfo;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.utils.WebUtils;

public class EntryViewFactory {

    private static void getCommon(EntryInfo view, Entry entry) {

        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
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
        view.setBioSafetyLevel(entry.getBioSafetyLevel());

        try {
            boolean hasAttachment = (AttachmentManager.getByEntry(entry).size() > 0);
            view.setHasAttachment(hasAttachment);

            ArrayList<Sample> samples = SampleManager.getSamplesByEntry(entry);
            ArrayList<SampleStorage> sampleMap = new ArrayList<SampleStorage>();

            if (samples != null) {
                for (Sample sample : samples) {
                    Storage storage = sample.getStorage();

                    LinkedList<Storage> storageList = new LinkedList<Storage>();

                    List<Storage> storages = StorageManager.getStoragesUptoScheme(storage);
                    if (storages != null)
                        storageList.addAll(storages);
                    Storage scheme = StorageManager.getSchemeContainingParentStorage(storage);
                    if (scheme != null)
                        storageList.add(scheme);

                    SampleInfo sampleInfo = getSampleInfo(sample);
                    SampleStorage sampleStorage = new SampleStorage(sampleInfo,
                            getStorageListInfo(storageList));
                    sampleMap.add(sampleStorage);
                }
            }

            boolean hasSample = (samples.size() > 0);
            view.setHasSample(hasSample);
            view.setSampleMap(sampleMap);
            boolean hasSequence = (SequenceManager.getByEntry(entry) != null);
            view.setHasSequence(hasSequence);
        } catch (ManagerException e) {
            Logger.error(e);
        }
    }

    private static LinkedList<StorageInfo> getStorageListInfo(LinkedList<Storage> storageList) {
        LinkedList<StorageInfo> info = new LinkedList<StorageInfo>();

        if (storageList == null)
            return info;

        for (Storage storage : storageList) {
            info.add(getStorageInfo(storage));
        }

        return info;
    }

    private static StorageInfo getStorageInfo(Storage storage) {
        StorageInfo info = new StorageInfo();
        if (storage == null)
            return info;

        //        info.setChildCount(storage.getChildren().size());
        info.setDisplay(storage.toString());
        info.setId(storage.getId());
        info.setType(storage.getStorageType().name());

        return info;
    }

    private static SampleInfo getSampleInfo(Sample sample) {
        SampleInfo info = new SampleInfo();
        if (sample == null)
            return info;

        info.setCreationTime(sample.getCreationTime());
        info.setLabel(sample.getLabel());
        info.setNotes(sample.getNotes());
        info.setDepositor(sample.getDepositor());

        Storage storage = sample.getStorage(); // specific storage to this sample. e.g. Tube
        if (storage != null) {
            info.setLocationId(String.valueOf(storage.getId()));
            info.setLocation(storage.getIndex());
        }

        return info;
    }

    public static EntryInfo createTableViewData(Entry entry) {

        EntryType type = EntryType.nameToType(entry.getRecordType());

        EntryInfo view = new EntryInfo(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(entry.getPartNumbersAsString());
        view.setName(entry.getNamesAsString());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerEmail(entry.getOwnerEmail());

        // attachments
        boolean hasAttachment = false;
        try {
            hasAttachment = (AttachmentManager.getByEntry(entry).size() > 0);
        } catch (ManagerException e) {
            Logger.error(e);
        }
        view.setHasAttachment(hasAttachment);

        // has sample
        try {
            view.setHasSample(SampleManager.getSamplesByEntry(entry).size() > 0);
        } catch (ManagerException e) {
            Logger.error(e);
        }

        // has sequence
        try {
            view.setHasSequence(SequenceManager.getByEntry(entry) != null);
        } catch (ManagerException e) {
            Logger.error(e);
        }

        return view;
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
            String link = WebUtils.linkifyText(IceSession.get().getAccount(), strain.getPlasmids());
            view.setPlasmids(link);
            view.setSelectionMarkers(strain.getSelectionMarkersAsString());

            return view;
        }

        case arabidopsis: {

            ArabidopsisSeedInfo view = new ArabidopsisSeedInfo();
            getCommon(view, entry);

            ArabidopsisSeed seed = (ArabidopsisSeed) entry;
            PlantType plantType = PlantType.valueOf(seed.getPlantType().toString());
            view.setPlantType(plantType);

            Generation generation = Generation.valueOf(seed.getGeneration().toString());
            view.setGeneration(generation);
            view.setHomozygosity(seed.getHomozygosity());
            view.setEcotype(seed.getEcotype());
            view.setParents(seed.getParents());
            view.setHarvestDate(seed.getHarvestDate());

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

            // get strains for plasmid
            try {
                Set<Strain> strains = UtilsManager.getStrainsForPlasmid(plasmid);
                if (strains != null) {
                    for (Strain strain : strains) {
                        view.getStrains().put(strain.getId(),
                            strain.getOnePartNumber().getPartNumber());
                    }
                }
            } catch (ManagerException e) {
                Logger.error(e);
            }

            return view;
        }

        default:
            return null;
        }
    }
}
