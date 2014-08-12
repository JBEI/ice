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
import org.jbei.ice.lib.models.TraceSequence;

/**
 * Factory for converting {@link Entry}s to a {@link org.jbei.ice.lib.dto.entry.PartData}
 * data transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    private static EntryAuthorization authorization = new EntryAuthorization();

    public static PartData getInfo(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            return null;

        PartData part = getCommon(type, entry);

        switch (type) {
            case PLASMID:
                part.setPlasmidData(plasmidInfo(entry));
                break;

            case STRAIN:
                part.setStrainData(strainInfo(entry));
                break;

            case ARABIDOPSIS:
                part.setArabidopsisSeedData(seedInfo(entry));
                break;

            default:
            case PART:
                return part;
        }
        return part;
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

    public static ArrayList<TraceSequenceAnalysis> getSequenceAnalysis(List<TraceSequence> sequences) {
        ArrayList<TraceSequenceAnalysis> infos = new ArrayList<>();
        if (sequences == null)
            return infos;

        AccountController accountController = new AccountController();
        for (TraceSequence sequence : sequences) {
            TraceSequenceAnalysis info = new TraceSequenceAnalysis();
            info.setId(sequence.getId());
            info.setCreated(sequence.getCreationTime());
            info.setFilename(sequence.getFilename());
            info.setSequence(sequence.getSequence());
            if (sequence.getTraceSequenceAlignment() != null) {
                info.setTraceSequenceAlignment(sequence.getTraceSequenceAlignment().toDataTransferObject());
            }
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

    private static ArabidopsisSeedData seedInfo(Entry entry) {
        ArabidopsisSeedData data = new ArabidopsisSeedData();

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
        data.setSeedParents(seed.getParents());
        data.setHarvestDate(seed.getHarvestDate());
        boolean isSent = !(seed.isSentToABRC() == null || !seed.isSentToABRC());
        data.setSentToAbrc(isSent);
        return data;
    }

    private static StrainData strainInfo(Entry entry) {
        StrainData data = new StrainData();

        // strain specific
        Strain strain = (Strain) entry;
        data.setGenotypePhenotype(strain.getGenotypePhenotype());
        data.setHost(strain.getHost());
        return data;
    }

    private static PlasmidData plasmidInfo(Entry entry) {
        PlasmidData data = new PlasmidData();
        Plasmid plasmid = (Plasmid) entry;

        // plasmid specific fields
        data.setBackbone(plasmid.getBackbone());
        data.setCircular(plasmid.getCircular());
        data.setOriginOfReplication(plasmid.getOriginOfReplication());
        data.setPromoters(plasmid.getPromoters());
        data.setReplicatesIn(plasmid.getReplicatesIn());
        return data;
    }

    private static PartData getCommon(EntryType type, Entry entry) {
        PartData info = new PartData(type);
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
        info.setPrincipalInvestigatorEmail(entry.getPrincipalInvestigatorEmail());

        ArrayList<String> links = new ArrayList<>();
        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
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

        // linked entries
        for (Entry linkedEntry : entry.getLinkedEntries()) {
            PartData linkedPartData = getInfo(linkedEntry);
            if (linkedPartData != null)
                info.getLinkedParts().add(linkedPartData);
        }

        return info;
    }

    private static PartData getTipViewCommon(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData view = new PartData(type);
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
        Account account;
        String ownerEmail = entry.getOwnerEmail();
        if (ownerEmail != null && (account = accountController.getByEmail(ownerEmail)) != null)
            view.setOwnerId(account.getId());

        String creatorEmail = entry.getCreatorEmail();
        if (creatorEmail != null && (account = accountController.getByEmail(creatorEmail)) != null)
            view.setCreatorId(account.getId());

        view.setKeywords(entry.getKeywords());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime().getTime());
        view.setModificationTime(entry.getModificationTime().getTime());
        view.setBioSafetyLevel(entry.getBioSafetyLevel());
        view.setFundingSource(entry.getFundingSource());
        view.setPrincipalInvestigator(entry.getPrincipalInvestigator());
        return view;
    }

    public static PartData createTableViewData(String userId, Entry entry, boolean includeOwnerInfo) {
        if (entry == null)
            return null;
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData view = new PartData(type);
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
            if (entry.getOwnerEmail() != null && (account1 = accountController.getByEmail(
                    entry.getOwnerEmail())) != null)
                view.setOwnerId(account1.getId());

            if (entry.getCreatorEmail() != null && (account1 = accountController.getByEmail(
                    entry.getCreatorEmail())) != null)
                view.setCreatorId(account1.getId());
        }

        // attachments
        AttachmentController attachmentController = new AttachmentController();
        boolean hasAttachment = attachmentController.hasAttachment(entry);
        view.setHasAttachment(hasAttachment);

        // has sample
        SampleController sampleController = new SampleController();
        view.setHasSample(sampleController.hasSample(entry));

        // has sequence
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        view.setHasSequence(sequenceDAO.hasSequence(entry.getId()));
        view.setHasOriginalSequence(sequenceDAO.hasOriginalSequence(entry.getId()));

        return view;
    }

    public static PartData createTipView(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData part = getTipViewCommon(entry);

        switch (type) {
            case STRAIN:
                StrainData strainData = new StrainData();

                // strain specific
                Strain strain = (Strain) entry;
                strainData.setHost(strain.getHost());
                strainData.setGenotypePhenotype(strain.getGenotypePhenotype());
                part.setStrainData(strainData);
                break;

            case ARABIDOPSIS:
                ArabidopsisSeedData seedData = new ArabidopsisSeedData();
                ArabidopsisSeed seed = (ArabidopsisSeed) entry;
                PlantType plantType = PlantType.fromString(seed.getPlantType().toString());
                seedData.setPlantType(plantType);

                Generation generation = Generation.fromString(seed.getGeneration().toString());
                seedData.setGeneration(generation);
                seedData.setHomozygosity(seed.getHomozygosity());
                seedData.setEcotype(seed.getEcotype());
                seedData.setSeedParents(seed.getParents());
                seedData.setHarvestDate(seed.getHarvestDate());
                part.setArabidopsisSeedData(seedData);
                break;

            default:
            case PART:
                return part;

            case PLASMID:
                PlasmidData plasmidData = new PlasmidData();

                Plasmid plasmid = (Plasmid) entry;
                plasmidData.setBackbone(plasmid.getBackbone());
                plasmidData.setOriginOfReplication(plasmid.getOriginOfReplication());
                plasmidData.setPromoters(plasmid.getPromoters());
                plasmidData.setReplicatesIn(plasmid.getReplicatesIn());
                part.setPlasmidData(plasmidData);
                break;
        }

        return part;
    }
}
