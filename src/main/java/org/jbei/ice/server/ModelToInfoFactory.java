package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Parameter;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.ParameterType;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.Visibility;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.PartInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;

/**
 * Factory for converting {@link Entry}s to their corresponding {@link org.jbei.ice.shared.dto.entry.EntryInfo} data
 * transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    public static EntryInfo getInfo(Account account, Entry entry, List<Attachment> attachments,
            Map<Sample, LinkedList<Storage>> samples, List<TraceSequence> sequences, boolean hasSequence) {
        EntryInfo info;
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            return null;

        switch (type) {
            case PLASMID:
                info = plasmidInfo(account, entry);
                break;

            case STRAIN:
                info = strainInfo(account, (Strain) entry);
                break;

            case ARABIDOPSIS:
                info = seedInfo(account, entry);
                break;

            case PART:
                info = partInfo(account, entry);
                break;

            default:
                Logger.error("Do not know how to handle entry type " + type);
                return null;
        }

        if (info == null)
            return info;

        // get attachments
        ArrayList<AttachmentInfo> attachmentInfos = getAttachments(attachments);
        info.setAttachments(attachmentInfos);
        info.setHasAttachment(!attachmentInfos.isEmpty());

        // get samples
        ArrayList<SampleStorage> samplesList = new ArrayList<SampleStorage>();
        if (samples != null) {
            for (Sample sample : samples.keySet()) {
                SampleInfo key = getSampleInfo(sample);
                Storage storage = sample.getStorage();
                if (storage != null) {
                    key.setLocationId(String.valueOf(storage.getId()));
                    key.setLocation(storage.getIndex());
                }

                LinkedList<Storage> storageList = samples.get(sample);
                SampleStorage sampleStorage = new SampleStorage(key, getStorageListInfo(storageList));
                samplesList.add(sampleStorage);
            }
        }
        info.setSampleMap(samplesList);
        info.setHasSample(!samplesList.isEmpty());

        // get trace sequences 
        ArrayList<SequenceAnalysisInfo> analysisInfo = getSequenceAnalysis(sequences);
        info.setSequenceAnalysis(analysisInfo);

        // has sequence (different from trace sequence above)
        info.setHasSequence(hasSequence);

        return info;
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

    private static ArrayList<AttachmentInfo> getAttachments(List<Attachment> attachments) {
        ArrayList<AttachmentInfo> infos = new ArrayList<AttachmentInfo>();
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
        ArrayList<SequenceAnalysisInfo> infos = new ArrayList<SequenceAnalysisInfo>();
        if (sequences == null)
            return infos;

        AccountController accountController = ApplicationController.getAccountController();
        for (TraceSequence sequence : sequences) {
            SequenceAnalysisInfo info = new SequenceAnalysisInfo();
            info.setCreated(sequence.getCreationTime());
            info.setName(sequence.getFilename());
            AccountInfo accountInfo = new AccountInfo();
            try {
                Account account = accountController.getByEmail(sequence.getDepositor());
                if (account != null) {
                    accountInfo.setFirstName(account.getFirstName());
                    accountInfo.setLastName(account.getLastName());
                    accountInfo.setId(account.getId());
                }
            } catch (ControllerException e) {
                Logger.warn(e.getMessage());
            }
            info.setDepositor(accountInfo);
            infos.add(info);
            info.setFileId(sequence.getFileId());
        }

        return infos;
    }

    private static PartInfo partInfo(Account account, Entry entry) {
        PartInfo info = new PartInfo();
        info = (PartInfo) getCommon(account, info, entry);

        // part specific
        Part part = (Part) entry;
        String packageFormat = null;
        if (part.getPackageFormat() != null) {
            packageFormat = part.getPackageFormat().toString();
        }

        info.setPackageFormat(JbeiConstants.getPackageFormat(packageFormat));
        return info;
    }

    private static ArabidopsisSeedInfo seedInfo(Account account, Entry entry) {
        ArabidopsisSeedInfo info = new ArabidopsisSeedInfo();
        info = (ArabidopsisSeedInfo) getCommon(account, info, entry);

        // seed specific
        ArabidopsisSeed seed = (ArabidopsisSeed) entry;

        if (seed.getPlantType() != null && seed.getPlantType() != ArabidopsisSeed.PlantType.NULL) {
            PlantType type = PlantType.valueOf(seed.getPlantType().name());
            info.setPlantType(type);
        }

        if (seed.getGeneration() != null && seed.getGeneration() != ArabidopsisSeed.Generation.NULL) {
            Generation generation = Generation.valueOf(seed.getGeneration().name());
            info.setGeneration(generation);
        }
        info.setHomozygosity(seed.getHomozygosity());
        info.setEcotype(seed.getEcotype());
        info.setParents(seed.getParents());
        info.setHarvestDate(seed.getHarvestDate());
        info.setSentToAbrc(seed.isSentToABRC());

        return info;
    }

    private static StrainInfo strainInfo(Account account, Strain strain) {
        StrainInfo info = new StrainInfo();
        info = (StrainInfo) getCommon(account, info, strain);

        // strain specific
        info.setGenotypePhenotype(strain.getGenotypePhenotype());
        info.setPlasmids(strain.getPlasmids());
        info.setLinkifiedPlasmids(EntryUtil.linkifyText(account, info.getPlasmids()));
        info.setHost(strain.getHost());
        info.setLinkifiedHost(EntryUtil.linkifyText(account, info.getHost()));
        return info;
    }

    private static PlasmidInfo plasmidInfo(Account account, Entry entry) {
        PlasmidInfo info = new PlasmidInfo();
        info = (PlasmidInfo) getCommon(account, info, entry);
        Plasmid plasmid = (Plasmid) entry;

        // plasmid specific fields
        info.setBackbone(plasmid.getBackbone());
        info.setCircular(plasmid.getCircular());
        info.setOriginOfReplication(plasmid.getOriginOfReplication());
        info.setPromoters(plasmid.getPromoters());

        // get strains for plasmid
        Set<Strain> strains = EntryUtil.getStrainsForPlasmid(plasmid);
        if (strains != null) {
            for (Strain strain : strains) {
                info.getStrains()
                    .put(strain.getId(), strain.getOnePartNumber().getPartNumber());
            }
        }

        return info;
    }

    private static EntryInfo getCommon(Account account, EntryInfo info, Entry entry) {
        info.setId(entry.getId());
        info.setRecordId(entry.getRecordId());
        info.setPartId(EntryUtil.getPartNumbersAsString(entry));
        info.setVersionId(entry.getVersionId());
        info.setName(entry.getNamesAsString());
        info.setOwner(entry.getOwner());
        info.setOwnerEmail(entry.getOwnerEmail());
        info.setCreator(entry.getCreator());
        info.setCreatorEmail(entry.getCreatorEmail());

        AccountController accountController = ApplicationController.getAccountController();
        try {
            long ownerId = accountController.getAccountId(entry.getOwnerEmail());
            info.setOwnerId(ownerId);
            if (entry.getCreatorEmail() != null) {
                long creatorId = accountController.getAccountId(entry.getCreatorEmail());
                info.setCreatorId(creatorId);
            }
        } catch (ControllerException ce) {
        }

        info.setAlias(entry.getAlias());
        info.setKeywords(entry.getKeywords());
        info.setStatus(entry.getStatus());
        info.setShortDescription(entry.getShortDescription());
        info.setCreationTime(entry.getCreationTime());
        info.setModificationTime(entry.getModificationTime());
        info.setBioSafetyLevel(entry.getBioSafetyLevel());

        info.setLongDescription(entry.getLongDescription());
        info.setLongDescriptionType(entry.getLongDescriptionType());
        info.setIntellectualProperty(entry.getIntellectualProperty());
        info.setSelectionMarkers(entry.getSelectionMarkersAsString());

        if (!entry.getEntryFundingSources().isEmpty()) {
            EntryFundingSource source = entry.getEntryFundingSources().iterator().next();
            info.setPrincipalInvestigator(source.getFundingSource().getPrincipalInvestigator());
            info.setFundingSource(source.getFundingSource().getFundingSource());
        }

        info.setLinks(entry.getLinksAsString());
        ArrayList<ParameterInfo> params = new ArrayList<ParameterInfo>();

        if (entry.getParameters() != null) {
            for (Parameter parameter : entry.getParameters()) {
                ParameterInfo paramInfo = new ParameterInfo();
                paramInfo.setName(parameter.getKey());
                paramInfo.setValue(parameter.getValue());
                paramInfo.setType(ParameterType.valueOf(parameter.getParameterType().name()));
                params.add(paramInfo);
            }
        }
        info.setParameters(params);

        // get visibility
        info.setVisibility(Visibility.valueToEnum(entry.getVisibility()));

        String parsed = EntryUtil.getParsedNotes(entry.getLongDescriptionType());
        info.setLongDescription(entry.getLongDescription());
        info.setParsedDescription(parsed);
        if (account != null) {
            String parsedShortDesc = EntryUtil.linkifyText(account, entry.getShortDescription());
            info.setLinkifiedShortDescription(parsedShortDesc);

            String linkStr = "";
            if (entry.getLinks() != null) {
                for (Link link : entry.getLinks()) {
                    if (link.getLink() != null && !link.getLink().isEmpty())
                        linkStr += (link.getLink() + ", ");
                    else if (link.getUrl() != null && !link.getUrl().isEmpty())
                        linkStr += (link.getUrl() + ", ");
                }
                if (!linkStr.isEmpty())
                    linkStr = linkStr.substring(0, linkStr.length() - 1);
            }
            String parsedLinks = EntryUtil.linkifyText(account, linkStr);
            info.setLinkifiedLinks(parsedLinks);
            String parsedReferences = EntryUtil.linkifyText(account, entry.getReferences());
            info.setReferences(parsedReferences);
        }
        return info;
    }

    public static EntryInfo getSummaryInfo(Entry entry) {
        EntryInfo info = null;
        EntryType type = EntryType.nameToType(entry.getRecordType());

        switch (type) {
            case ARABIDOPSIS:
                info = new ArabidopsisSeedInfo();
                break;

            case PART:
                info = new PartInfo();
                break;

            case PLASMID:
                info = new PlasmidInfo();
                break;

            case STRAIN:
                info = new StrainInfo();
                break;
        }

        info.setId(entry.getId());
        info.setRecordId(entry.getRecordId());
        info.setPartId(EntryUtil.getPartNumbersAsString(entry));
        info.setName(entry.getNamesAsString());
        return info;
    }

    private static void getTipViewCommon(EntryInfo view, Entry entry) {
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(EntryUtil.getPartNumbersAsString(entry));
        view.setName(entry.getNamesAsString());
        view.setAlias(entry.getAlias());
        view.setCreator(entry.getCreator());
        view.setCreatorEmail(entry.getCreatorEmail());
        view.setStatus(entry.getStatus());
        view.setOwner(entry.getOwner());
        view.setOwnerEmail(entry.getOwnerEmail());

        AccountController accountController = ApplicationController.getAccountController();
        try {
            Account account1;
            if ((account1 = accountController.getByEmail(entry.getOwnerEmail())) != null)
                view.setOwnerId(account1.getId());

            if ((account1 = accountController.getByEmail(entry.getCreatorEmail())) != null)
                view.setCreatorId(account1.getId());
        } catch (ControllerException ce) {
        }

        view.setKeywords(entry.getKeywords());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime());
        view.setModificationTime(entry.getModificationTime());
        view.setBioSafetyLevel(entry.getBioSafetyLevel());
        if (!entry.getEntryFundingSources().isEmpty()) {
            EntryFundingSource source = entry.getEntryFundingSources().iterator().next();
            view.setFundingSource(source.getFundingSource().getFundingSource());
            view.setPrincipalInvestigator(source.getFundingSource().getPrincipalInvestigator());
        }
    }

    public static EntryInfo createTableViewData(Entry entry, boolean includeOwnerInfo) {
        if (entry == null)
            return null;
        EntryType type = EntryType.nameToType(entry.getRecordType());
        EntryInfo view = new EntryInfo();
        view.setType(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(EntryUtil.getPartNumbersAsString(entry));
        view.setName(entry.getNamesAsString());
        view.setShortDescription(entry.getShortDescription());
        view.setCreationTime(entry.getCreationTime());
        view.setStatus(entry.getStatus());
        if (includeOwnerInfo) {
            view.setOwner(entry.getOwner());
            view.setOwnerEmail(entry.getOwnerEmail());

            AccountController accountController = ApplicationController.getAccountController();
            try {
                Account account1;
                if ((account1 = accountController.getByEmail(entry.getOwnerEmail())) != null)
                    view.setOwnerId(account1.getId());

                if ((account1 = accountController.getByEmail(entry.getCreatorEmail())) != null)
                    view.setCreatorId(account1.getId());
            } catch (ControllerException ce) {
            }
        }

        // attachments
        boolean hasAttachment = false;
        try {
            AttachmentController attachmentController = ApplicationController.getAttachmentController();
            hasAttachment = attachmentController.hasAttachment(entry);
        } catch (ControllerException e) {
            Logger.error(e);
        }
        view.setHasAttachment(hasAttachment);

        // has sample
        try {
            SampleController sampleController = ApplicationController.getSampleController();
            view.setHasSample(sampleController.hasSample(entry));
        } catch (ControllerException e) {
            Logger.error(e);
        }

        // has sequence
        try {
            SequenceController sequenceController = ApplicationController.getSequenceController();
            view.setHasSequence(sequenceController.hasSequence(entry));
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return view;
    }

    public static EntryInfo createTipView(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        switch (type) {

            case STRAIN: {
                StrainInfo view = new StrainInfo();

                // common
                getTipViewCommon(view, entry);

                // strain specific
                Strain strain = (Strain) entry;
                view.setHost(strain.getHost());
                view.setGenotypePhenotype(strain.getGenotypePhenotype());
                view.setPlasmids(strain.getPlasmids());
                view.setSelectionMarkers(strain.getSelectionMarkersAsString());

                return view;
            }

            case ARABIDOPSIS: {
                ArabidopsisSeedInfo view = new ArabidopsisSeedInfo();
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
                PartInfo view = new PartInfo();

                getTipViewCommon(view, entry);

                Part part = (Part) entry;
                view.setPackageFormat(part.getPackageFormat().toString());
                return view;
            }

            case PLASMID: {
                PlasmidInfo view = new PlasmidInfo();
                getTipViewCommon(view, entry);

                Plasmid plasmid = (Plasmid) entry;
                view.setBackbone(plasmid.getBackbone());
                view.setOriginOfReplication(plasmid.getOriginOfReplication());
                view.setPromoters(plasmid.getPromoters());

                // get strains for plasmid
                Set<Strain> strains = EntryUtil.getStrainsForPlasmid(plasmid);
                if (strains != null) {
                    for (Strain strain : strains) {
                        view.getStrains().put(strain.getId(), strain.getOnePartNumber().getPartNumber());
                    }
                }

                return view;
            }

            default:
                return null;
        }
    }
}
