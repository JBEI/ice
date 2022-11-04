package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.account.AccountController;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.entry.EntryUtil;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory for converting {@link Entry}s to a {@link PartData}
 * data transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    public static PartData getInfo(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            throw new IllegalArgumentException("Invalid entry type: " + entry.getRecordType());

        return new PartData(type);
    }

    public static ArrayList<AttachmentInfo> getAttachments(List<Attachment> attachments, boolean canEdit) {
        ArrayList<AttachmentInfo> infos = new ArrayList<>();
        if (attachments == null)
            return infos;

        for (Attachment attachment : attachments) {
            AttachmentInfo info = new AttachmentInfo();
            info.setDescription(attachment.getDescription());
            info.setFilename(attachment.getFileName());
            info.setId(attachment.getId());
            info.setCanEdit(canEdit);
            info.setFileId(attachment.getFileId());
            infos.add(info);
        }

        return infos;
    }

    private static SeedData seedInfo(Entry entry) {
        SeedData data = new SeedData();

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
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
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

    private static ProteinData proteinInfo(Entry entry) {
        ProteinData data = new ProteinData();

        // protein specific
        Protein protein = (Protein) entry;
        data.setOrganism(protein.getOrganism());
        data.setFullName(protein.getFullName());
        data.setGeneName(protein.getGeneName());
        data.setUploadedFrom(protein.getUploadedFrom());
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
        AccountModel ownerAccount = org.jbei.ice.storage.DAOFactory.getAccountDAO().getByEmail(info.getOwnerEmail());
        if (ownerAccount != null)
            info.setOwnerId(ownerAccount.getId());
        info.setCreator(entry.getCreator());
        info.setCreatorEmail(entry.getCreatorEmail());
        AccountModel creatorAccount = org.jbei.ice.storage.DAOFactory.getAccountDAO().getByEmail(info.getCreatorEmail());
        if (creatorAccount != null)
            info.setCreatorId(creatorAccount.getId());

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
                AccountModel piAccount = org.jbei.ice.storage.DAOFactory.getAccountDAO().getByEmail(entry.getPrincipalInvestigatorEmail());
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
            for (ParameterModel parameter : entry.getParameters()) {
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
            PartData linkedPartData = new PartData(EntryType.nameToType(linkedEntry.getRecordType()));
            linkedPartData.setId(linkedEntry.getId());
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
        view.setShortDescription(entry.getShortDescription());

        view.setAlias(entry.getAlias());
        view.setCreator(entry.getCreator());
        view.setCreatorEmail(entry.getCreatorEmail());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerEmail(entry.getOwnerEmail());
        view.setSelectionMarkers(EntryUtil.getSelectionMarkersAsList(entry.getSelectionMarkers()));

        AccountController accountController = new AccountController();
        AccountModel account;
        String ownerEmail = entry.getOwnerEmail();
        if (ownerEmail != null && (account = accountController.getByEmail(ownerEmail)) != null)
            view.setOwnerId(account.getId());

        String creatorEmail = entry.getCreatorEmail();
        if (creatorEmail != null && (account = accountController.getByEmail(creatorEmail)) != null)
            view.setCreatorId(account.getId());

        view.setKeywords(entry.getKeywords());
        view.setCreationTime(entry.getCreationTime().getTime());
        view.setModificationTime(entry.getModificationTime().getTime());
        view.setBioSafetyLevel(entry.getBioSafetyLevel());
        view.setFundingSource(entry.getFundingSource());
        view.setPrincipalInvestigator(entry.getPrincipalInvestigator());
        return view;
    }



    public static PartData createTableView(long entryId, List<String> fields) {
        Set<String> fieldsToProcess;
        if (fields == null)
            fieldsToProcess = new HashSet<>();
        else
            fieldsToProcess = new HashSet<>(fields);

        fieldsToProcess.add("name");
        fieldsToProcess.add("status");
        fieldsToProcess.add("recordType");
        fieldsToProcess.add("creation_time");
        fieldsToProcess.add("short_description");

        // minimum set of values
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData view = new PartData(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(entry.getPartNumber());
        view.setName(entry.getName());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime().getTime());
        view.setStatus(entry.getStatus());
        view.setShortDescription(entry.getShortDescription());

        // has sample
        view.setHasSample(DAOFactory.getSampleDAO().hasSample(entry));

        // has sequence
        Visibility visibility = Visibility.valueToEnum(entry.getVisibility());
        view.setVisibility(visibility);
        if (visibility == Visibility.REMOTE) {
            view.setHasSequence(entry.getLongDescriptionType().equalsIgnoreCase("sequence"));
        } else {
            SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
            view.setHasSequence(sequenceDAO.hasSequence(entry.getId()));
            view.setHasOriginalSequence(sequenceDAO.hasOriginalSequence(entry.getId()));
        }

        // optional values
        if (fieldsToProcess.contains("alias")) {
            view.setAlias(entry.getAlias());
        }

        if (fieldsToProcess.contains("links")) {
            for (Entry linkedEntry : entry.getLinkedEntries()) {
                PartData linkedPartData = new PartData(EntryType.nameToType(linkedEntry.getRecordType()));
                linkedPartData.setId(linkedEntry.getId());
                view.getLinkedParts().add(linkedPartData);
            }

            List<Entry> parents = DAOFactory.getEntryDAO().getParents(entry.getId());
            if (parents != null) {
                for (Entry parentEntry : parents) {
                    PartData partData = new PartData(EntryType.nameToType(parentEntry.getRecordType()));
                    partData.setId(parentEntry.getId());
                    view.getParents().add(partData);
                }
            }
        }

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

            case SEED:
                SeedData seedData = new SeedData();
                ArabidopsisSeed seed = (ArabidopsisSeed) entry;
                PlantType plantType = PlantType.fromString(seed.getPlantType().toString());
                seedData.setPlantType(plantType);

                Generation generation = Generation.fromString(seed.getGeneration().toString());
                seedData.setGeneration(generation);
                seedData.setHomozygosity(seed.getHomozygosity());
                seedData.setEcotype(seed.getEcotype());
                seedData.setSeedParents(seed.getParents());
                if (seed.getHarvestDate() != null) {
                    DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                    String dateFormat = format.format(seed.getHarvestDate());
                    seedData.setHarvestDate(dateFormat);
                }
                part.setSeedData(seedData);
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

            case PROTEIN:
                ProteinData proteinData = new ProteinData();

                Protein protein = (Protein) entry;
                proteinData.setOrganism(protein.getOrganism());
                proteinData.setFullName(protein.getFullName());
                proteinData.setGeneName(protein.getGeneName());
                proteinData.setUploadedFrom(protein.getUploadedFrom());
                part.setProteinData(proteinData);
                break;
        }

        return part;
    }

    public static void entryToInfo(Entry entry, EntryField entryField) {
        if (entryField.isCustom())
            return;

        EntryFieldLabel label = EntryFieldLabel.fromString(entryField.getLabel());
        if (label == null)
            throw new IllegalArgumentException("Cannot find label for field");

        switch (label) {
            case PI -> entryField.setValue(entry.getPrincipalInvestigator());
            case CREATOR -> entryField.setValue(entry.getCreator());
            case CREATOR_EMAIL -> entryField.setValue(entry.getCreatorEmail());
            case FUNDING_SOURCE -> entryField.setValue(entry.getFundingSource());
            case IP -> entryField.setValue(entry.getIntellectualProperty());
            case BIO_SAFETY_LEVEL -> entryField.setValue(entry.getBioSafetyLevel().toString());
            case NAME -> entryField.setValue(entry.getName());
            case ALIAS -> entryField.setValue(entry.getAlias());
            case PI_EMAIL -> entryField.setValue(entry.getPrincipalInvestigatorEmail());
            case KEYWORDS -> entryField.setValue(entry.getKeywords());
            case SUMMARY -> entryField.setValue(entry.getShortDescription());
            case STATUS -> entryField.setValue(entry.getStatus());
//            case EXTERNAL_URL -> entryField.setValue();
            case NOTES -> entryField.setValue(entry.getLongDescription());
            case SELECTION_MARKERS -> entryField.setValue(entry.getSelectionMarkersAsString());

            case ORIGIN_OF_REPLICATION, CIRCULAR, BACKBONE, PROMOTERS, REPLICATES_IN ->
                setPlasmidField(entry, label, entryField);
            case GENOTYPE_OR_PHENOTYPE, HOST -> setStrainField(entry, label, entryField);
            case PLANT_TYPE, GENERATION, HOMOZYGOSITY, ECOTYPE, PARENTS, HARVEST_DATE, SENT_TO_ABRC ->
                setSeedFields(entry, label, entryField);
            case ORGANISM, FULL_NAME, GENE_NAME, UPLOADED_FROM -> setProteinFields(entry, label, entryField);
        }
    }

    private static void setPlasmidField(Entry entry, EntryFieldLabel label, EntryField entryField) {
        if (EntryType.nameToType(entry.getRecordType()) != EntryType.PLASMID)
            return;

        Plasmid plasmid = (Plasmid) entry;
        switch (label) {
            case ORIGIN_OF_REPLICATION -> entryField.setValue(plasmid.getOriginOfReplication());
            case BACKBONE -> entryField.setValue(plasmid.getBackbone());
            case CIRCULAR -> entryField.setValue(Boolean.toString(plasmid.getCircular()));
            case PROMOTERS -> entryField.setValue(plasmid.getPromoters());
            case REPLICATES_IN -> entryField.setValue(plasmid.getReplicatesIn());
        }
    }

    private static void setStrainField(Entry entry, EntryFieldLabel label, EntryField entryField) {
        if (EntryType.nameToType(entry.getRecordType()) != EntryType.STRAIN)
            return;

        // strain specific
        Strain strain = (Strain) entry;
        switch (label) {
            case GENOTYPE_OR_PHENOTYPE -> entryField.setValue(strain.getGenotypePhenotype());
            case HOST -> entryField.setValue(strain.getHost());
        }
    }

    private static void setSeedFields(Entry entry, EntryFieldLabel label, EntryField entryField) {
        if (EntryType.nameToType(entry.getRecordType()) != EntryType.STRAIN)
            return;

        // seed specific
        ArabidopsisSeed seed = (ArabidopsisSeed) entry;
        switch (label) {
            case PLANT_TYPE -> {
                if (seed.getPlantType() != null && seed.getPlantType() != PlantType.NULL) {
                    entryField.setValue(seed.getPlantType().name());
                }
            }

            case GENERATION -> {
                if (seed.getGeneration() != null && seed.getGeneration() != Generation.UNKNOWN) {
                    entryField.setValue(seed.getGeneration().name());
                }
            }

            case HOMOZYGOSITY -> entryField.setValue(seed.getHomozygosity());
            case ECOTYPE -> entryField.setValue(seed.getEcotype());
            case PARENTS -> entryField.setValue(seed.getParents());
            case HARVEST_DATE -> {
                if (seed.getHarvestDate() != null) {
                    DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                    String dateFormat = format.format(seed.getHarvestDate());
                    entryField.setValue(dateFormat);
                }
            }
            case SENT_TO_ABRC -> {
                boolean isSent = !(seed.isSentToABRC() == null || !seed.isSentToABRC());
                entryField.setValue(Boolean.toString(isSent));
            }
        }
    }

    private static void setProteinFields(Entry entry, EntryFieldLabel label, EntryField entryField) {
        if (EntryType.nameToType(entry.getRecordType()) != EntryType.PROTEIN)
            return;

        // protein specific
        Protein protein = (Protein) entry;

        switch (label) {
            case ORGANISM -> entryField.setValue(protein.getOrganism());
            case FULL_NAME -> entryField.setValue(protein.getFullName());
            case GENE_NAME -> entryField.setValue(protein.getGeneName());
            case UPLOADED_FROM -> entryField.setValue(protein.getUploadedFrom());
        }
    }
}
