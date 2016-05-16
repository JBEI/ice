package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class PartSequence extends HasEntry {

    private final Entry entry;
    private SequenceDAO sequenceDAO;
    private EntryAuthorization entryAuthorization;

    public PartSequence(String entryId) {
        this.entry = super.getEntry(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with identifier " + entryId);
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.entryAuthorization = new EntryAuthorization();
    }

    public FeaturedDNASequence get(String userId) {
        if (!new PermissionsController().isPubliclyVisible(entry))
            entryAuthorization.expectRead(userId, entry);

        boolean canEdit = entryAuthorization.canWriteThoroughCheck(userId, entry);
        return getFeaturedSequence(entry, canEdit);
    }

    /**
     * Parses a sequence as a string and assigns it to the entry
     *
     * @param sequenceString sequence as string to be parsed
     * @param name           optional filename
     * @return sequence information
     */
    public SequenceInfo parseSequence(String sequenceString, String name) {
//        EntryType type = EntryType.nameToType(entryType);

//        Entry entry;
//        if (StringUtils.isBlank(recordId)) {
//            EntryCreator creator = new EntryCreator();
//            Account account = DAOFactory.getAccountDAO().getByEmail(userId);
//
//            entry = EntryFactory.buildEntry(type);
//            String entryName = account.getFullName();
//            String entryEmail = account.getEmail();
//            entry.setOwner(entryName);
//            entry.setOwnerEmail(entryEmail);
//            entry.setCreator(entryName);
//            entry.setCreatorEmail(entryEmail);
//            entry.setVisibility(Visibility.DRAFT.getValue());
//            entry = creator.createEntry(account, entry, null);
//        } else {
//            entry = DAOFactory.getEntryDAO().getByRecordId(recordId);
//            if (entry == null)
//                return null;
//        }

        // parse actual sequence
        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        if (dnaSequence == null)
            return null;

        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        if (!StringUtils.isBlank(name))
            sequence.setFileName(name);

        Sequence result = sequenceDAO.saveSequence(sequence);
        BlastPlus.scheduleBlastIndexRebuildTask(true);
        SequenceInfo info = result.toDataTransferObject();
        info.setSequence(dnaSequence);
        return info;
    }

    protected FeaturedDNASequence getFeaturedSequence(Entry entry, boolean canEdit) {
        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return null;

        FeaturedDNASequence featuredDNASequence = sequenceToDNASequence(sequence);
        featuredDNASequence.setCanEdit(canEdit);
        featuredDNASequence.setIdentifier(entry.getPartNumber());
        String uriPrefix = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX).getValue();
        if (!StringUtils.isEmpty(uriPrefix)) {
            featuredDNASequence.setUri(uriPrefix + "/entry/" + entry.getId());
        }
        return featuredDNASequence;
    }

    public FeaturedDNASequence sequenceToDNASequence(Sequence sequence) {
        if (sequence == null) {
            return null;
        }

        List<DNAFeature> features = new LinkedList<>();

        if (sequence.getSequenceFeatures() != null && sequence.getSequenceFeatures().size() > 0) {
            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                DNAFeature dnaFeature = new DNAFeature();
                dnaFeature.setUri(sequenceFeature.getUri());

                for (SequenceFeatureAttribute attribute : sequenceFeature.getSequenceFeatureAttributes()) {
                    String key = attribute.getKey();
                    String value = attribute.getValue();
                    DNAFeatureNote dnaFeatureNote = new DNAFeatureNote(key, value);
                    dnaFeatureNote.setQuoted(attribute.getQuoted());
                    dnaFeature.addNote(dnaFeatureNote);
                }

                Set<AnnotationLocation> locations = sequenceFeature.getAnnotationLocations();
                for (AnnotationLocation location : locations) {
                    dnaFeature.getLocations().add(
                            new DNAFeatureLocation(location.getGenbankStart(), location.getEnd()));
                }

                dnaFeature.setId(sequenceFeature.getId());
                dnaFeature.setType(sequenceFeature.getGenbankType());
                dnaFeature.setName(sequenceFeature.getName());
                dnaFeature.setStrand(sequenceFeature.getStrand());

                if (sequenceFeature.getAnnotationType() != null) {
                    dnaFeature.setAnnotationType(sequenceFeature.getAnnotationType().toString());
                }

                features.add(dnaFeature);
            }
        }

        boolean circular = false;
        Entry entry = sequence.getEntry();
        if (entry.getRecordType().equalsIgnoreCase(EntryType.PLASMID.name()))
            circular = ((Plasmid) sequence.getEntry()).getCircular();
        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence(
                sequence.getSequence(), entry.getName(), circular, features, "");
        featuredDNASequence.setUri(sequence.getUri());

        return featuredDNASequence;
    }
}
