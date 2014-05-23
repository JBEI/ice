package org.jbei.ice.servlet;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.StorageInfo;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Parameter;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;

/**
 * Factory for converting {@link Entry}s to their corresponding {@link org.jbei.ice.lib.dto.entry.PartData}
 * data transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    private static EntryAuthorization authorization = new EntryAuthorization();

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

//    public static ArrayList<SampleStorage> getSamples(Map<Sample, LinkedList<Storage>> samples) {
//        ArrayList<SampleStorage> samplesList = new ArrayList<>();
//        if (samples == null)
//            return samplesList;
//
//        for (Map.Entry<Sample, LinkedList<Storage>> sample : samples.entrySet()) {
//            PartSample key = getSampleInfo(sample.getKey());
//            Storage storage = sample.getKey().getStorage();
//            if (storage != null) {
//                key.setLocationId(String.valueOf(storage.getId()));
//                key.setLocation(storage.getIndex());
//            }
//
//            SampleStorage sampleStorage = new SampleStorage(key, getStorageListInfo(sample.getValue()));
//            samplesList.add(sampleStorage);
//        }
//        return samplesList;
//    }

//    private static PartSample getSampleInfo(Sample sample) {
//        PartSample part = new PartSample();
//        if (sample == null)
//            return part;
//
//        part.setSampleId(Long.toString(sample.getId()));
//        part.setCreationTime(sample.getCreationTime());
//        part.setLabel(sample.getLabel());
//        part.setNotes(sample.getNotes());
//        part.setDepositor(sample.getDepositor());
//
//        Storage storage = sample.getStorage(); // specific storage to this sample. e.g. Tube
//        if (storage != null) {
//            part.setLocationId(String.valueOf(storage.getId()));
//            part.setLocation(storage.getIndex());
//        }
//        return part;
//    }

//    private static LinkedList<StorageInfo> getStorageListInfo(LinkedList<Storage> storageList) {
//        LinkedList<StorageInfo> info = new LinkedList<>();
//
//        if (storageList == null)
//            return info;
//
//        for (Storage storage : storageList) {
//            info.add(getStorageInfo(storage));
//        }
//
//        return info;
//    }

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

        AccountController accountController = new AccountController();
        for (TraceSequence sequence : sequences) {
            SequenceAnalysisInfo info = new SequenceAnalysisInfo();
            info.setId(sequence.getId());
            info.setCreated(sequence.getCreationTime());
            info.setName(sequence.getFilename());
            AccountTransfer accountTransfer = new AccountTransfer();
            Account account = accountController.getByEmail(sequence.getDepositor());
            if (account != null) {
                accountTransfer.setFirstName(account.getFirstName());
                accountTransfer.setLastName(account.getLastName());
                accountTransfer.setId(account.getId());
            }
            info.setDepositor(accountTransfer);
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

        if (seed.getPlantType() != null && seed.getPlantType() != PlantType.NULL) {
            PlantType type = PlantType.fromString(seed.getPlantType().name());
            data.setPlantType(type);
        }

        if (seed.getGeneration() != null && seed.getGeneration() != Generation.UNKNOWN) {
            Generation generation = Generation.fromString(seed.getGeneration().name());
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

        AccountController accountController = new AccountController();
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
        info.setSelectionMarkers(EntryUtil.getSelectionMarkersAsList(entry.getSelectionMarkers()));

        // funding sources
        info.setFundingSource(entry.getFundingSource());
        info.setPrincipalInvestigator(entry.getPrincipalInvestigator());

        // linked entries
        for (Entry linkedEntry : entry.getLinkedEntries()) {
            PartData data = new PartData();
//            EntryType linkedType = EntryType.nameToType(linkedEntry.getRecordType());
//            data.setType(linkedType);
            data.setId(linkedEntry.getId());
//            data.setPartId(linkedEntry.getPartNumber());
//            data.setName(linkedEntry.getName());
            info.getLinkedParts().add(data);
        }

        ArrayList<String> links = new ArrayList<>();
        if(entry.getLinks() != null) {
            for(Link link : entry.getLinks()){
                links.add(link.getLink());
            }
        }
        info.setLinks(links);
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
        view.setSelectionMarkers(EntryUtil.getSelectionMarkersAsList(entry.getSelectionMarkers()));

        AccountController accountController = new AccountController();
        Account account1;
        if ((account1 = accountController.getByEmail(entry.getOwnerEmail())) != null)
            view.setOwnerId(account1.getId());

        if ((account1 = accountController.getByEmail(entry.getCreatorEmail())) != null)
            view.setCreatorId(account1.getId());

        view.setKeywords(entry.getKeywords());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime().getTime());
        view.setModificationTime(entry.getModificationTime().getTime());
        view.setBioSafetyLevel(entry.getBioSafetyLevel());
        view.setFundingSource(entry.getFundingSource());
        view.setPrincipalInvestigator(entry.getPrincipalInvestigator());

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

    public static PartData createTableViewData(String userId, Entry entry, boolean includeOwnerInfo) {
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
        view.setVisibility(Visibility.valueToEnum(entry.getVisibility()));

        if (userId != null)
            view.setCanEdit(authorization.canWrite(userId, entry));

        // information about the owner
        if (includeOwnerInfo) {
            view.setOwner(entry.getOwner());
            view.setOwnerEmail(entry.getOwnerEmail());

            AccountController accountController = new AccountController();
            Account account1;
            if ((account1 = accountController.getByEmail(entry.getOwnerEmail())) != null)
                view.setOwnerId(account1.getId());

            if ((account1 = accountController.getByEmail(entry.getCreatorEmail())) != null)
                view.setCreatorId(account1.getId());
        }

        // attachments
        boolean hasAttachment = false;
        try {
            AttachmentController attachmentController = new AttachmentController();
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
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        view.setHasSequence(sequenceDAO.hasSequence(entry.getId()));
        view.setHasOriginalSequence(sequenceDAO.hasOriginalSequence(entry.getId()));

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
                PlantType plantType = PlantType.fromString(seed.getPlantType().toString());
                view.setPlantType(plantType);

                Generation generation = Generation.fromString(seed.getGeneration().toString());
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
