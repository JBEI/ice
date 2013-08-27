package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Parameter;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData.Generation;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData.PlantType;
import org.jbei.ice.lib.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.shared.dto.entry.CustomField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.lib.shared.dto.entry.StrainData;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.shared.dto.user.User;

/**
 * Factory for converting {@link Entry}s to their corresponding {@link org.jbei.ice.lib.shared.dto.entry.PartData}
 * data transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    public static PartData getInfo(Entry entry) {
        PartData info;
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            return null;

        switch (type) {
            case PLASMID:
                info = plasmidInfo(entry);
                break;

            case STRAIN:
                info = strainInfo(entry);
                break;

            case ARABIDOPSIS:
                info = seedInfo(entry);
                break;

            case PART:
                info = partInfo(entry);
                break;

            default:
                Logger.error("Do not know how to handle entry type " + type);
                return null;
        }
        return info;
    }

    public static ArrayList<SampleStorage> getSamples(Map<Sample, LinkedList<Storage>> samples) {
        ArrayList<SampleStorage> samplesList = new ArrayList<>();
        if (samples == null)
            return samplesList;

        for (Map.Entry<Sample, LinkedList<Storage>> sample : samples.entrySet()) {
            PartSample key = getSampleInfo(sample.getKey());
            Storage storage = sample.getKey().getStorage();
            if (storage != null) {
                key.setLocationId(String.valueOf(storage.getId()));
                key.setLocation(storage.getIndex());
            }

            SampleStorage sampleStorage = new SampleStorage(key, getStorageListInfo(sample.getValue()));
            samplesList.add(sampleStorage);
        }
        return samplesList;
    }

    private static PartSample getSampleInfo(Sample sample) {
        PartSample part = new PartSample();
        if (sample == null)
            return part;

        part.setSampleId(Long.toString(sample.getId()));
        part.setCreationTime(sample.getCreationTime());
        part.setLabel(sample.getLabel());
        part.setNotes(sample.getNotes());
        part.setDepositor(sample.getDepositor());

        Storage storage = sample.getStorage(); // specific storage to this sample. e.g. Tube
        if (storage != null) {
            part.setLocationId(String.valueOf(storage.getId()));
            part.setLocation(storage.getIndex());
        }
        return part;
    }

    private static LinkedList<StorageInfo> getStorageListInfo(LinkedList<Storage> storageList) {
        LinkedList<StorageInfo> info = new LinkedList<>();

        if (storageList == null)
            return info;

        for (Storage storage : storageList) {
            info.add(getStorageInfo(storage));
        }

        return info;
    }

    public static ArrayList<AttachmentInfo> getAttachments(List<Attachment> attachments) {
        ArrayList<AttachmentInfo> infos = new ArrayList<>();
        if (attachments == null)
            return infos;

        for (Attachment attachment : attachments) {
            AttachmentInfo info = new AttachmentInfo();
            info.setDescription(attachment.getDescription());
            info.setFilename(attachment.getFileName());
            info.setId(attachment.getId());
            info.setFileId(attachment.getFileId());
            infos.add(info);
        }

        return infos;
    }

    public static StorageInfo getStorageInfo(Storage storage) {
        StorageInfo info = new StorageInfo();
        if (storage == null)
            return info;

        info.setDisplay(storage.getIndex());
        info.setId(storage.getId());
        info.setType(storage.getStorageType().name());
        return info;
    }

    public static ArrayList<SequenceAnalysisInfo> getSequenceAnalysis(List<TraceSequence> sequences) {
        ArrayList<SequenceAnalysisInfo> infos = new ArrayList<>();
        if (sequences == null)
            return infos;

        AccountController accountController = ControllerFactory.getAccountController();
        for (TraceSequence sequence : sequences) {
            SequenceAnalysisInfo info = new SequenceAnalysisInfo();
            info.setCreated(sequence.getCreationTime());
            info.setName(sequence.getFilename());
            User user = new User();
            try {
                Account account = accountController.getByEmail(sequence.getDepositor());
                if (account != null) {
                    user.setFirstName(account.getFirstName());
                    user.setLastName(account.getLastName());
                    user.setId(account.getId());
                }
            } catch (ControllerException e) {
                Logger.warn(e.getMessage());
            }
            info.setDepositor(user);
            infos.add(info);
            info.setFileId(sequence.getFileId());
        }

        return infos;
    }

    private static PartData partInfo(Entry entry) {
        PartData info = new PartData();
        return getCommon(info, entry);
    }

    private static ArabidopsisSeedData seedInfo(Entry entry) {
        ArabidopsisSeedData data = new ArabidopsisSeedData();
        data = (ArabidopsisSeedData) getCommon(data, entry);

        // seed specific
        ArabidopsisSeed seed = (ArabidopsisSeed) entry;

        if (seed.getPlantType() != null && seed.getPlantType() != ArabidopsisSeed.PlantType.NULL) {
            PlantType type = PlantType.valueOf(seed.getPlantType().name());
            data.setPlantType(type);
        }

        if (seed.getGeneration() != null && seed.getGeneration() != ArabidopsisSeed.Generation.NULL) {
            Generation generation = Generation.valueOf(seed.getGeneration().name());
            data.setGeneration(generation);
        }
        data.setHomozygosity(seed.getHomozygosity());
        data.setEcotype(seed.getEcotype());
        data.setParents(seed.getParents());
        data.setHarvestDate(seed.getHarvestDate());
        boolean isSent = !(seed.isSentToABRC() == null || !seed.isSentToABRC());
        data.setSentToAbrc(isSent);
        return data;
    }

    private static StrainData strainInfo(Entry entry) {
        StrainData data = new StrainData();
        data = (StrainData) getCommon(data, entry);

        // strain specific
        Strain strain = (Strain) entry;
        data.setGenotypePhenotype(strain.getGenotypePhenotype());
        data.setHost(strain.getHost());
        return data;
    }

    private static PlasmidData plasmidInfo(Entry entry) {
        PlasmidData data = new PlasmidData();
        data = (PlasmidData) getCommon(data, entry);
        Plasmid plasmid = (Plasmid) entry;

        // plasmid specific fields
        data.setBackbone(plasmid.getBackbone());
        data.setCircular(plasmid.getCircular());
        data.setOriginOfReplication(plasmid.getOriginOfReplication());
        data.setPromoters(plasmid.getPromoters());
        data.setReplicatesIn(plasmid.getReplicatesIn());
        return data;
    }

    private static PartData getCommon(PartData info, Entry entry) {
        info.setId(entry.getId());
        info.setRecordId(entry.getRecordId());
        info.setPartId(entry.getPartNumber());
        info.setName(entry.getName());
        info.setOwner(entry.getOwner());
        info.setOwnerEmail(entry.getOwnerEmail());
        info.setCreator(entry.getCreator());
        info.setCreatorEmail(entry.getCreatorEmail());

        AccountController accountController = ControllerFactory.getAccountController();
        try {
            long ownerId = accountController.getAccountId(entry.getOwnerEmail());
            info.setOwnerId(ownerId);
            if (entry.getCreatorEmail() != null && !entry.getCreatorEmail().isEmpty()) {
                long creatorId = accountController.getAccountId(entry.getCreatorEmail());
                info.setCreatorId(creatorId);
            }
        } catch (ControllerException ce) {
            Logger.warn(ce.getMessage());
        }

        info.setAlias(entry.getAlias());
        info.setKeywords(entry.getKeywords());
        info.setStatus(entry.getStatus());
        info.setShortDescription(entry.getShortDescription());
        info.setCreationTime(entry.getCreationTime().getTime());
        info.setModificationTime(entry.getModificationTime().getTime());
        info.setBioSafetyLevel(entry.getBioSafetyLevel());

        info.setLongDescription(entry.getLongDescription());
        info.setIntellectualProperty(entry.getIntellectualProperty());
        info.setSelectionMarkers(entry.getSelectionMarkersAsString());

        // funding sources
        if (!entry.getEntryFundingSources().isEmpty()) {
            Iterator iterator = entry.getEntryFundingSources().iterator();
            EntryFundingSource source = (EntryFundingSource) iterator.next();
            info.setPrincipalInvestigator(source.getFundingSource().getPrincipalInvestigator());
            info.setFundingSource(source.getFundingSource().getFundingSource());

            while (iterator.hasNext()) {
                EntryFundingSource next = (EntryFundingSource) iterator.next();
                String pi = next.getFundingSource().getPrincipalInvestigator();
                String fs = next.getFundingSource().getFundingSource();

                if (pi != null && !pi.trim().isEmpty()) {
                    info.setPrincipalInvestigator(info.getPrincipalInvestigator() + ", " + pi);
                }

                if (fs != null && !fs.trim().isEmpty()) {
                    info.setFundingSource(info.getFundingSource() + ", " + fs);
                }
            }
        }

        // linked entries
        for (Entry linkedEntry : entry.getLinkedEntries()) {
            PartData data = new PartData();
            EntryType linkedType = EntryType.nameToType(linkedEntry.getRecordType());
            data.setType(linkedType);
            data.setId(linkedEntry.getId());
            data.setPartId(linkedEntry.getPartNumber());
            data.setName(linkedEntry.getName());
            info.getLinkedParts().add(data);
        }

        info.setLinks(entry.getLinksAsString());
        ArrayList<CustomField> params = new ArrayList<>();

        if (entry.getParameters() != null) {
            for (Parameter parameter : entry.getParameters()) {
                CustomField paramInfo = new CustomField();
                paramInfo.setName(parameter.getKey());
                paramInfo.setValue(parameter.getValue());
                params.add(paramInfo);
            }
        }
        info.setCustomFields(params);

        // get visibility
        info.setVisibility(Visibility.valueToEnum(entry.getVisibility()));

        info.setLongDescription(entry.getLongDescription());
        info.setShortDescription(entry.getShortDescription());

        String links = "";
        StringBuilder linkStr = new StringBuilder();
        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
                if (link.getLink() != null && !link.getLink().isEmpty()) {
                    linkStr.append(link.getLink()).append(", ");
                } else if (link.getUrl() != null && !link.getUrl().isEmpty())
                    linkStr.append(link.getUrl()).append(", ");
            }

            links = linkStr.toString();
            if (!links.isEmpty())
                links = links.substring(0, links.length() - 1);
        }
        info.setLinks(links);
        info.setReferences(entry.getReferences());
        return info;
    }

    private static void getTipViewCommon(PartData view, Entry entry) {
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(entry.getPartNumber());
        view.setName(entry.getName());
        view.setAlias(entry.getAlias());
        view.setCreator(entry.getCreator());
        view.setCreatorEmail(entry.getCreatorEmail());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerEmail(entry.getOwnerEmail());
        view.setSelectionMarkers(entry.getSelectionMarkersAsString());

        AccountController accountController = ControllerFactory.getAccountController();
        try {
            Account account1;
            if ((account1 = accountController.getByEmail(entry.getOwnerEmail())) != null)
                view.setOwnerId(account1.getId());

            if ((account1 = accountController.getByEmail(entry.getCreatorEmail())) != null)
                view.setCreatorId(account1.getId());
        } catch (ControllerException ce) {
            Logger.warn(ce.getMessage());
        }

        view.setKeywords(entry.getKeywords());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime().getTime());
        view.setModificationTime(entry.getModificationTime().getTime());
        view.setBioSafetyLevel(entry.getBioSafetyLevel());
        if (!entry.getEntryFundingSources().isEmpty()) {
            EntryFundingSource source = entry.getEntryFundingSources().iterator().next();
            view.setFundingSource(source.getFundingSource().getFundingSource());
            view.setPrincipalInvestigator(source.getFundingSource().getPrincipalInvestigator());
        }

        for (Entry linkedEntry : entry.getLinkedEntries()) {
            PartData data = new PartData();
            EntryType linkedType = EntryType.nameToType(linkedEntry.getRecordType());
            data.setType(linkedType);
            data.setId(linkedEntry.getId());
            data.setPartId(linkedEntry.getPartNumber());
            data.setName(linkedEntry.getName());
            view.getLinkedParts().add(data);
        }
    }

    public static PartData createTableViewData(Entry entry, boolean includeOwnerInfo) {
        if (entry == null)
            return null;
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData view = new PartData();
        view.setType(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(entry.getPartNumber());
        view.setName(entry.getName());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime().getTime());
        view.setStatus(entry.getStatus());
        if (includeOwnerInfo) {
            view.setOwner(entry.getOwner());
            view.setOwnerEmail(entry.getOwnerEmail());

            AccountController accountController = ControllerFactory.getAccountController();
            try {
                Account account1;
                if ((account1 = accountController.getByEmail(entry.getOwnerEmail())) != null)
                    view.setOwnerId(account1.getId());

                if ((account1 = accountController.getByEmail(entry.getCreatorEmail())) != null)
                    view.setCreatorId(account1.getId());
            } catch (ControllerException ce) {
                Logger.warn(ce.getMessage());
            }
        }

        // attachments
        boolean hasAttachment = false;
        try {
            AttachmentController attachmentController = ControllerFactory.getAttachmentController();
            hasAttachment = attachmentController.hasAttachment(entry);
        } catch (ControllerException e) {
            Logger.error(e);
        }
        view.setHasAttachment(hasAttachment);

        // has sample
        try {
            SampleController sampleController = ControllerFactory.getSampleController();
            view.setHasSample(sampleController.hasSample(entry));
        } catch (ControllerException e) {
            Logger.error(e);
        }

        // has sequence
        try {
            SequenceController sequenceController = ControllerFactory.getSequenceController();
            view.setHasSequence(sequenceController.hasSequence(entry.getId()));
            view.setHasOriginalSequence(sequenceController.hasOriginalSequence(entry.getId()));
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return view;
    }

    public static PartData createTipView(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        switch (type) {

            case STRAIN: {
                StrainData view = new StrainData();

                // common
                getTipViewCommon(view, entry);

                // strain specific
                Strain strain = (Strain) entry;
                view.setHost(strain.getHost());
                view.setGenotypePhenotype(strain.getGenotypePhenotype());
                return view;
            }

            case ARABIDOPSIS: {
                ArabidopsisSeedData view = new ArabidopsisSeedData();
                getTipViewCommon(view, entry);

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
                PartData view = new PartData();
                getTipViewCommon(view, entry);
                return view;
            }

            case PLASMID: {
                PlasmidData view = new PlasmidData();
                getTipViewCommon(view, entry);

                Plasmid plasmid = (Plasmid) entry;
                view.setBackbone(plasmid.getBackbone());
                view.setOriginOfReplication(plasmid.getOriginOfReplication());
                view.setPromoters(plasmid.getPromoters());
                view.setReplicatesIn(plasmid.getReplicatesIn());
                return view;
            }

            default:
                return null;
        }
    }
}
