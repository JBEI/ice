package org.jbei.ice.server;

import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Parameter;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.lib.utils.UtilsDAO;
import org.jbei.ice.shared.dto.*;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.web.utils.WebUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory for converting {@link Entry}s to their corresponding {@link EntryInfo} data transfer
 * objects
 * <p/>
 * TODO : this duplicates some of the functionality of EntryViewFactory. Consolidate
 *
 * @author Hector Plahar
 */
public class EntryToInfoFactory {

    public static EntryInfo getInfo(Account account, Entry entry, List<Attachment> attachments,
            Map<Sample, LinkedList<Storage>> samples, List<TraceSequence> sequences,
            boolean hasSequence) {
        EntryInfo info = null;
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            return null;

        switch (type) {
            case PLASMID:
                info = plasmidInfo(entry);
                break;

            case STRAIN:
                info = strainInfo(account, (Strain) entry);
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
                SampleStorage sampleStorage = new SampleStorage(key,
                                                                getStorageListInfo(storageList));
                samplesList.add(sampleStorage);
            }
        }
        info.setSampleMap(samplesList);
        info.setHasSample(!samplesList.isEmpty());

        // get trace sequences 
        ArrayList<SequenceAnalysisInfo> analysisInfo = getSequenceAnaylsis(sequences);
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

    public static ArrayList<SequenceAnalysisInfo> getSequenceAnaylsis(List<TraceSequence> sequences) {
        ArrayList<SequenceAnalysisInfo> infos = new ArrayList<SequenceAnalysisInfo>();
        if (sequences == null)
            return infos;

        for (TraceSequence sequence : sequences) {
            SequenceAnalysisInfo info = new SequenceAnalysisInfo();
            info.setCreated(sequence.getCreationTime());
            info.setName(sequence.getFilename());
            AccountInfo account = new AccountInfo();
            account.setEmail(sequence.getDepositor());
            info.setDepositor(account);
            infos.add(info);
            info.setFileId(sequence.getFileId());
        }

        return infos;
    }

    private static PartInfo partInfo(Entry entry) {
        PartInfo info = new PartInfo();
        info = (PartInfo) getCommon(info, entry);

        // part specific
        Part part = (Part) entry;
        String packageFormat = null;
        if (part.getPackageFormat() != null) {
            packageFormat = part.getPackageFormat().toString();
        }

        info.setPackageFormat(JbeiConstants.getPackageFormat(packageFormat));
        return info;
    }

    private static ArabidopsisSeedInfo seedInfo(Entry entry) {
        ArabidopsisSeedInfo info = new ArabidopsisSeedInfo();
        info = (ArabidopsisSeedInfo) getCommon(info, entry);

        // seed specific
        ArabidopsisSeed seed = (ArabidopsisSeed) entry;

        if (seed.getPlantType() != null) {
            PlantType type = PlantType.valueOf(seed.getPlantType().name());
            info.setPlantType(type);
        }

        if (seed.getGeneration() != null) {
            Generation generation = Generation.valueOf(seed.getGeneration().name());
            info.setGeneration(generation);
        }
        info.setHomozygosity(seed.getHomozygosity());
        info.setEcotype(seed.getEcotype());
        info.setParents(seed.getParents());
        info.setHarvestDate(seed.getHarvestDate());

        return info;
    }

    private static StrainInfo strainInfo(Account account, Strain strain) {
        StrainInfo info = new StrainInfo();
        info = (StrainInfo) getCommon(info, strain);

        // strain specific
        info.setGenotypePhenotype(strain.getGenotypePhenotype());
        info.setPlasmids(strain.getPlasmids());
        info.setLinkifiedPlasmids(WebUtils.linkifyText(account, info.getPlasmids()));
        info.setHost(strain.getHost());

        return info;
    }

    private static PlasmidInfo plasmidInfo(Entry entry) {
        PlasmidInfo info = new PlasmidInfo();
        info = (PlasmidInfo) getCommon(info, entry);
        Plasmid plasmid = (Plasmid) entry;

        // plasmid specific fields
        info.setBackbone(plasmid.getBackbone());
        info.setCircular(plasmid.getCircular());
        info.setOriginOfReplication(plasmid.getOriginOfReplication());
        info.setPromoters(plasmid.getPromoters());

        /// get strains for plasmid
        try {
            Set<Strain> strains = UtilsDAO.getStrainsForPlasmid(plasmid);
            if (strains != null) {
                for (Strain strain : strains) {
                    info.getStrains()
                        .put(strain.getId(), strain.getOnePartNumber().getPartNumber());
                }
            }
        } catch (ManagerException e) {
            Logger.error(e);
        }

        return info;
    }

    private static EntryInfo getCommon(EntryInfo info, Entry entry) {

        info.setId(entry.getId());
        info.setRecordId(entry.getRecordId());
        info.setVersionId(entry.getVersionId());
        info.setName(entry.getNamesAsString());
        info.setSelectionMarkers(entry.getSelectionMarkersAsString());

        info.setOwner(entry.getOwner());
        info.setOwnerEmail(entry.getOwnerEmail());
        info.setCreator(entry.getCreator());
        info.setCreatorEmail(entry.getCreatorEmail());
        info.setAlias(entry.getAlias());
        info.setKeywords(entry.getKeywords());
        info.setStatus(entry.getStatus());
        info.setShortDescription(entry.getShortDescription());
        info.setLongDescription(entry.getLongDescription());
        info.setLongDescriptionType(entry.getLongDescriptionType());
        info.setShortDescription(entry.getShortDescription());
        info.setReferences(entry.getReferences());
        info.setCreationTime(entry.getCreationTime());
        info.setModificationTime(entry.getModificationTime());
        info.setBioSafetyLevel(entry.getBioSafetyLevel());
        info.setPartId(entry.getPartNumbersAsString());
        info.setIntellectualProperty(entry.getIntellectualProperty());
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
                paramInfo.setType(ParameterInfo.Type.valueOf(parameter.getParameterType().name()));
                params.add(paramInfo);
            }
        }
        info.setParameters(params);

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
        info.setPartId(entry.getPartNumbersAsString());
        info.setName(info.getName());
        return info;
    }
}
