package org.jbei.ice.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Parameter;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.shared.ArabidopsisSeedInfo;
import org.jbei.ice.shared.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.StrainInfo;

/**
 * Factory for converting {@link Entry}s to their corresponding {@link EntryInfo} data transfer
 * objects
 * 
 * @author Hector Plahar
 */
public class EntryToInfoFactory {

    public static EntryInfo getInfo(Entry entry, List<Attachment> attachments,
            Map<Sample, LinkedList<Storage>> samples, List<TraceSequence> sequences,
            boolean hasSequence) {
        EntryInfo info = null;

        if (Entry.PLASMID_ENTRY_TYPE.equals(entry.getRecordType())) {
            info = plasmidInfo(entry);
        } else if (Entry.STRAIN_ENTRY_TYPE.equals(entry.getRecordType())) {
            info = strainInfo((Strain) entry);
        } else if (Entry.ARABIDOPSIS_SEED_ENTRY_TYPE.equals(entry.getRecordType())) {
            info = seedInfo(entry);
        } else if (Entry.PART_ENTRY_TYPE.equals(entry.getRecordType())) {
            info = partInfo(entry);
        } else {
            return null;
        }

        if (info == null)
            return info;

        // get attachments
        ArrayList<AttachmentInfo> attachmentInfos = getAttachments(attachments);
        info.setAttachments(attachmentInfos);
        info.setHasAttachment(!attachmentInfos.isEmpty());

        // get samples
        HashMap<SampleInfo, LinkedList<StorageInfo>> sampleMap = new HashMap<SampleInfo, LinkedList<StorageInfo>>();
        for (Sample sample : samples.keySet()) {
            SampleInfo key = getSampleInfo(sample);
            LinkedList<Storage> storage = samples.get(sample);
            sampleMap.put(key, getStorageListInfo(storage));
        }
        info.setSampleMap(sampleMap);
        info.setHasSample(!sampleMap.keySet().isEmpty());

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

    private static ArrayList<SequenceAnalysisInfo> getSequenceAnaylsis(List<TraceSequence> sequences) {
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
        PlantType type = PlantType.valueOf(seed.getPlantType().name());
        info.setPlantType(type);
        Generation generation = Generation.valueOf(seed.getGeneration().name());
        info.setGeneration(generation);
        info.setHomozygosity(seed.getHomozygosity());
        info.setEcotype(seed.getEcotype());
        info.setParents(seed.getParents());
        info.setHarvestDate(seed.getHarvestDate());

        return info;
    }

    private static StrainInfo strainInfo(Strain strain) {
        StrainInfo info = new StrainInfo();
        info = (StrainInfo) getCommon(info, strain);

        // strain specific
        info.setGenotypePhenotype(strain.getGenotypePhenotype());
        info.setPlasmids(strain.getPlasmids());
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

        return info;
    }

    private static EntryInfo getCommon(EntryInfo info, Entry entry) {

        info.setRecordId(entry.getRecordId());
        info.setVersionId(entry.getVersionId());
        info.setName(entry.getNamesAsString());
        EntryType type = getEntryType(entry);
        if (type != null)
            info.setType(type);
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

    static EntryType getEntryType(Entry entry) {

        if (Entry.PART_ENTRY_TYPE.equals(entry.getRecordType()))
            return EntryType.PART;

        if (Entry.ARABIDOPSIS_SEED_ENTRY_TYPE.equals(entry.getRecordType()))
            return EntryType.ARABIDOPSIS;

        if (Entry.STRAIN_ENTRY_TYPE.equals(entry.getRecordType()))
            return EntryType.STRAIN;

        if (Entry.PLASMID_ENTRY_TYPE.equals(entry.getRecordType()))
            return EntryType.PLASMID;

        return null;
    }
}
