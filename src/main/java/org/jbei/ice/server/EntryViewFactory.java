package org.jbei.ice.server;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageDAO;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.utils.UtilsDAO;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.StrainInfo;
import org.jbei.ice.web.utils.WebUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EntryViewFactory {

    private static void getCommon(Account account, EntryInfo view, Entry entry) {

        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(EntryUtil.getPartNumbersAsString(entry));
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
            AttachmentController attachmentController = new AttachmentController(account);
            boolean hasAttachment = attachmentController.hasAttachment(entry);
            view.setHasAttachment(hasAttachment);

            SampleController sampleController = new SampleController();
            ArrayList<Sample> samples = sampleController.getSamplesByEntry(entry);
            ArrayList<SampleStorage> sampleMap = new ArrayList<SampleStorage>();

            if (samples != null) {
                for (Sample sample : samples) {
                    Storage storage = sample.getStorage();

                    LinkedList<Storage> storageList = new LinkedList<Storage>();

                    List<Storage> storages = StorageDAO.getStoragesUptoScheme(storage);
                    if (storages != null)
                        storageList.addAll(storages);
                    Storage scheme = StorageDAO.getSchemeContainingParentStorage(storage);
                    if (scheme != null)
                        storageList.add(scheme);

                    SampleInfo sampleInfo = getSampleInfo(sample);
                    SampleStorage sampleStorage = new SampleStorage(sampleInfo,
                                                                    getStorageListInfo(storageList));
                    sampleMap.add(sampleStorage);
                }
            }

            boolean hasSample = (samples != null && samples.size() > 0);
            view.setHasSample(hasSample);
            view.setSampleMap(sampleMap);
            SequenceController sequenceController = new SequenceController();
            boolean hasSequence = sequenceController.hasSequence(entry);
            view.setHasSequence(hasSequence);
        } catch (ControllerException e) {
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

    public static EntryInfo createTableViewData(Account account, Entry entry) {

        EntryType type = EntryType.nameToType(entry.getRecordType());

        EntryInfo view = new EntryInfo(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(EntryUtil.getPartNumbersAsString(entry));
        view.setName(entry.getNamesAsString());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerEmail(entry.getOwnerEmail());

        // attachments
        boolean hasAttachment = false;
        try {
            AttachmentController attachmentController = new AttachmentController(account);
            hasAttachment = attachmentController.hasAttachment(entry);
        } catch (ControllerException e) {
            Logger.error(e);
        }
        view.setHasAttachment(hasAttachment);

        // has sample
        try {
            SampleController sampleController = new SampleController();
            view.setHasSample(sampleController.hasSample(entry));
        } catch (ControllerException e) {
            Logger.error(e);
        }

        // has sequence
        try {
            SequenceController sequenceController = new SequenceController();
            view.setHasSequence(sequenceController.hasSequence(entry));
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return view;
    }

    public static EntryInfo createTipView(Account account, Entry entry) {

        EntryType type = EntryType.nameToType(entry.getRecordType());
        switch (type) {

            case STRAIN: {
                StrainInfo view = new StrainInfo();

                // common
                getCommon(account, view, entry);

                // strain specific
                Strain strain = (Strain) entry;
                view.setHost(strain.getHost());
                view.setGenotypePhenotype(strain.getGenotypePhenotype());
                String link = WebUtils.linkifyText(account, strain.getPlasmids());
                view.setPlasmids(link);
                view.setSelectionMarkers(strain.getSelectionMarkersAsString());

                return view;
            }

            case ARABIDOPSIS: {

                ArabidopsisSeedInfo view = new ArabidopsisSeedInfo();
                getCommon(account, view, entry);

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

            case PART: {
                PartInfo view = new PartInfo();

                getCommon(account, view, entry);

                Part part = (Part) entry;
                view.setPackageFormat(part.getPackageFormat().toString());
                return view;
            }

            case PLASMID: {
                PlasmidInfo view = new PlasmidInfo();
                getCommon(account, view, entry);

                Plasmid plasmid = (Plasmid) entry;
                view.setBackbone(plasmid.getBackbone());
                view.setOriginOfReplication(plasmid.getOriginOfReplication());
                view.setPromoters(plasmid.getPromoters());

                // get strains for plasmid
                try {
                    Set<Strain> strains = UtilsDAO.getStrainsForPlasmid(plasmid);
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
