package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for converting {@link Entry}s to a {@link org.jbei.ice.lib.dto.entry.PartData}
 * data transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    public static PartData getInfo(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            return null;

        PartData partData = new PartData(type);
        PartData part = getCommon(partData, entry);

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
        if (seed.getHarvestDate() != null) {
            DateFormat format = new SimpleDateFormat("MM/dd/YYYY");
            String dateFormat = format.format(seed.getHarvestDate());
            data.setHarvestDate(dateFormat);
        }
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

    public static PartData getCommon(PartData info, Entry entry) {
        info.setId(entry.getId());
        info.setRecordId(entry.getRecordId());
        info.setPartId(entry.getPartNumber());
        info.setName(entry.getName());
        String owner = entry.getOwner();
        if (owner.trim().isEmpty())
            owner = entry.getOwnerEmail();
        info.setOwner(owner);
        info.setOwnerEmail(entry.getOwnerEmail());
        Account ownerAccount = DAOFactory.getAccountDAO().getByEmail(info.getOwnerEmail());
        if (ownerAccount != null)
            info.setOwnerId(ownerAccount.getId());
        info.setCreator(entry.getCreator());
        info.setCreatorEmail(entry.getCreatorEmail());
        Account creatorAccount = DAOFactory.getAccountDAO().getByEmail(info.getCreatorEmail());
        if (creatorAccount != null)
            info.setCreatorId(creatorAccount.getId());

        AccountController accountController = new AccountController();
        try {
            long ownerId = accountController.getAccountId(entry.getOwnerEmail());
            info.setOwnerId(ownerId);
            if (entry.getCreatorEmail() != null && !entry.getCreatorEmail().isEmpty()) {
                long creatorId = accountController.getAccountId(entry.getCreatorEmail());
                info.setCreatorId(creatorId);
            }
        } catch (Exception ce) {
            Logger.debug(ce.getMessage());
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
        try {
            if (!StringUtils.isEmpty(entry.getPrincipalInvestigatorEmail())) {
                Account piAccount = accountController.getByEmail(entry.getPrincipalInvestigatorEmail());
                if (piAccount != null) {
                    info.setPrincipalInvestigator(piAccount.getFullName());
                    info.setPrincipalInvestigatorEmail(piAccount.getEmail());
                    info.setPrincipalInvestigatorId(piAccount.getId());
                } else
                    info.setPrincipalInvestigatorEmail(entry.getPrincipalInvestigatorEmail());
            }
        } catch (Exception e) {
            Logger.debug(e.getMessage());
        }

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
                paramInfo.setId(parameter.getId());
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

    private static long getAccountId(String email) {
        if (email == null || email.isEmpty())
            return 0;

        Account account = DAOFactory.getAccountDAO().getByEmail(email);
        if (account == null)
            return 0;

        return account.getId();
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
        view.setAlias(entry.getAlias());
        view.setOwnerEmail(entry.getOwnerEmail());
        view.setVisibility(Visibility.valueToEnum(entry.getVisibility()));

        if (userId != null) {
            EntryAuthorization authorization = new EntryAuthorization();
            view.setCanEdit(authorization.canWrite(userId, entry));
        }

        // information about the owner and creator
        if (includeOwnerInfo) {
            view.setOwner(entry.getOwner());
            view.setOwnerId(getAccountId(entry.getOwnerEmail()));

            // creator
            view.setCreator(entry.getCreator());
            view.setCreatorEmail(entry.getCreatorEmail());
            view.setCreatorId(getAccountId(entry.getCreatorEmail()));
        }

        // has sample
        view.setHasSample(DAOFactory.getSampleDAO().hasSample(entry));

        // has sequence
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        view.setHasSequence(sequenceDAO.hasSequence(entry.getId()));
        view.setHasOriginalSequence(sequenceDAO.hasOriginalSequence(entry.getId()));

        return view;
    }

    public static PartData createTipView(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            throw new IllegalArgumentException("Invalid entry type " + entry.getRecordType());

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
                if (seed.getHarvestDate() != null) {
                    DateFormat format = new SimpleDateFormat("MM/dd/YYYY");
                    String dateFormat = format.format(seed.getHarvestDate());
                    seedData.setHarvestDate(dateFormat);
                }
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
