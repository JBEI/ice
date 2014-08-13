package org.jbei.ice.lib.entry.sequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.AuthorizationException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.sequence.composers.formatters.FastaFormatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.FormatterException;
import org.jbei.ice.lib.entry.sequence.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.IFormatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.SBOLFormatter;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeature.AnnotationType;
import org.jbei.ice.lib.models.SequenceFeatureAttribute;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * ABI to manipulate {@link Sequence}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class SequenceController {

    private final SequenceDAO dao;
    private final EntryAuthorization authorization;
    private final EntryRetriever retriever;

    public SequenceController() {
        dao = new SequenceDAO();
        authorization = new EntryAuthorization();
        retriever = new EntryRetriever();
    }

    public boolean parseAndSaveSequence(String userId, long partId, String sequenceString) {
        DNASequence dnaSequence = parse(sequenceString);

        if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
            String errorMsg = "Couldn't parse sequence file! Supported formats: "
                    + GeneralParser.getInstance().availableParsersToString()
                    + ". "
                    + "If you believe this is an error, please contact the administrator with your file";
            throw new IllegalArgumentException(errorMsg);
        }

        Sequence sequence = dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        Entry entry = DAOFactory.getEntryDAO().get(partId);
        if (entry == null)
            return false;

        sequence.setEntry(entry);
        return save(userId, sequence) != null;
    }

    // either or both recordId and entryType has to have a value
    public SequenceInfo parseSequence(String userId, String recordId, String entryType, String sequenceString) {
        EntryType type = EntryType.nameToType(entryType);
        EntryRetriever retriever = new EntryRetriever();
        EntryCreator creator = new EntryCreator();
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        Entry entry;
        if (StringUtils.isBlank(recordId)) {
            entry = EntryUtil.createEntryFromType(type, account.getFullName(), account.getEmail());
            entry.setVisibility(Visibility.DRAFT.getValue());
            entry = creator.createEntry(account, entry, null);
        } else {
            entry = retriever.getByRecordId(userId, recordId);
        }

        // parse actual sequence
        DNASequence dnaSequence = parse(sequenceString);
        if (dnaSequence == null)
            return null;

        Sequence sequence = dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        SequenceInfo info = save(userId, sequence).toDataTransferObject();
        info.setSequence(dnaSequence);
        return info;
    }

    /**
     * Save the given {@link Sequence} into the database, with the option to rebuild the search
     * index.
     *
     * @param userId   unique identifier of user saving sequence
     * @param sequence sequence to save
     * @return Saved Sequence
     * @throws AuthorizationException if the user does not have the permission to update the entry associated with
     *                                the sequence
     */
    public Sequence save(String userId, Sequence sequence) throws AuthorizationException {
        authorization.expectWrite(userId, sequence.getEntry());
        Sequence result = dao.saveSequence(sequence);
        ApplicationController.scheduleBlastIndexRebuildTask(true);
        return result;
    }

    public FeaturedDNASequence updateSequence(String userId, long entryId, FeaturedDNASequence featuredDNASequence) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null)
            return null;


        try {
            Entry entry = retriever.get(userId, entryId);
            if (entry == null) {
                return null;
            }

            Sequence existing = DAOFactory.getSequenceDAO().getByEntry(entry);
            if (existing != null) {
                Files.deleteIfExists(Paths.get(existing.getFwdHash() + ".png"));
            }
            Sequence sequence = dnaSequenceToSequence(featuredDNASequence);
            sequence.setEntry(entry);
            sequence = update(account, sequence);
            if (sequence != null)
                return sequenceToDNASequence(sequence);
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    /**
     * Update the {@link Sequence} in the database, with the option to rebuild the search index.
     *
     * @param sequence
     * @return Saved Sequence.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sequence update(Account account, Sequence sequence) throws ControllerException, PermissionException {
        if (sequence == null || sequence.getEntry() == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        authorization.expectWrite(account.getEmail(), sequence.getEntry());
        Sequence result;

        Entry entry = sequence.getEntry();
        entry.setModificationTime(Calendar.getInstance().getTime());
        Sequence oldSequence = dao.getByEntry(entry);

        if (oldSequence != null) {
            String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            String hash = oldSequence.getFwdHash();
            try {
                Files.deleteIfExists(Paths.get(tmpDir, hash + ".png"));
            } catch (IOException e) {
                Logger.warn(e.getMessage());
            }
            oldSequence.setSequenceUser(sequence.getSequenceUser());
            oldSequence.setSequence(sequence.getSequence());
            oldSequence.setFwdHash(sequence.getFwdHash());
            oldSequence.setRevHash(sequence.getRevHash());
            result = dao.updateSequence(oldSequence, sequence.getSequenceFeatures());
        } else
            result = dao.create(sequence);

        ApplicationController.scheduleBlastIndexRebuildTask(true);
        return result;
    }

    /**
     * Delete the {@link Sequence} in the database, then rebuild the search index.
     *
     * @param sequence
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Sequence sequence) throws ControllerException, PermissionException {
        authorization.expectWrite(account.getEmail(), sequence.getEntry());
        String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        dao.deleteSequence(sequence, tmpDir);
        ApplicationController.scheduleBlastIndexRebuildTask(true);
    }

    public boolean deleteSequence(String requester, long partId) {
        Entry entry = DAOFactory.getEntryDAO().get(partId);
        authorization.expectWrite(requester, entry);

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return true;

        String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        dao.deleteSequence(sequence, tmpDir);
        ApplicationController.scheduleBlastIndexRebuildTask(true);
        return true;
    }

    /**
     * Parse the given String into an {@link DNASequence} object.
     *
     * @param sequence
     * @return parsed DNASequence object.
     */
    public static DNASequence parse(String sequence) {
        return GeneralParser.getInstance().parse(sequence);
    }

    /**
     * Generate a formatted text of a given {@link IFormatter} from the given {@link Sequence}.
     *
     * @param sequence
     * @param formatter
     * @return Text of a formatted sequence.
     * @throws ControllerException
     */
    public String compose(Sequence sequence, IFormatter formatter) throws ControllerException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            formatter.format(sequence, byteStream);
        } catch (FormatterException | IOException e) {
            throw new ControllerException(e);
        }
        return byteStream.toString();
    }

    public FeaturedDNASequence retrievePartSequence(String userId, String recordId) {
        Entry entry = DAOFactory.getEntryDAO().getByRecordId(recordId);

        if (entry == null)
            throw new IllegalArgumentException("The part " + recordId + " could not be located");

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return null;

        return sequenceToDNASequence(sequence);
    }

    public FeaturedDNASequence retrievePartSequence(String userId, long recordId) {
        Entry entry = DAOFactory.getEntryDAO().get(recordId);
        if (entry == null)
            throw new IllegalArgumentException("The part " + recordId + " could not be located");

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return null;

        FeaturedDNASequence featuredDNASequence = sequenceToDNASequence(sequence);
        boolean canEdit = authorization.canWrite(userId, entry);
        featuredDNASequence.setCanEdit(canEdit);
        return featuredDNASequence;
    }

    /**
     * Generate a {@link FeaturedDNASequence} from a given {@link Sequence} object.
     *
     * @param sequence
     * @return FeaturedDNASequence
     */
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

    /**
     * Create a {@link Sequence} object from an {@link DNASequence} object.
     *
     * @param dnaSequence
     * @return Translated Sequence object.
     */
    public static Sequence dnaSequenceToSequence(DNASequence dnaSequence) {
        if (dnaSequence == null) {
            return null;
        }

        String sequenceString = dnaSequence.getSequence().toLowerCase();
        String fwdHash = SequenceUtils.calculateSequenceHash(sequenceString);
        String revHash;
        try {
            revHash = SequenceUtils.calculateSequenceHash(SequenceUtils.reverseComplement(sequenceString));
        } catch (UtilityException e) {
            revHash = "";
        }

        Sequence sequence = new Sequence(sequenceString, "", fwdHash, revHash, null);

        Set<SequenceFeature> sequenceFeatures = sequence.getSequenceFeatures();
        if (dnaSequence instanceof FeaturedDNASequence) {
            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) dnaSequence;
            sequence.setUri(featuredDNASequence.getUri());
            sequence.setComponentUri(featuredDNASequence.getDcUri());
            sequence.setIdentifier(featuredDNASequence.getIdentifier());

            if (featuredDNASequence.getFeatures() != null && !featuredDNASequence.getFeatures().isEmpty()) {
                for (DNAFeature dnaFeature : featuredDNASequence.getFeatures()) {
                    List<DNAFeatureLocation> locations = dnaFeature.getLocations();
                    String featureSequence = "";

                    for (DNAFeatureLocation location : locations) {
                        int genbankStart = location.getGenbankStart();
                        int end = location.getEnd();

                        if (genbankStart < 1) {
                            genbankStart = 1;
                        } else if (genbankStart > featuredDNASequence.getSequence().length()) {
                            genbankStart = featuredDNASequence.getSequence().length();
                        }

                        if (end < 1) {
                            end = 1;
                        } else if (end > featuredDNASequence.getSequence().length()) {
                            end = featuredDNASequence.getSequence().length();
                        }
                        if (genbankStart > end) { // over zero case
                            featureSequence += featuredDNASequence.getSequence().substring(
                                    genbankStart - 1, featuredDNASequence.getSequence().length());
                            featureSequence += featuredDNASequence.getSequence().substring(0, end);
                        } else { // normal
                            featureSequence += featuredDNASequence.getSequence().substring(genbankStart - 1, end);
                        }

                        if (genbankStart > end) { // over zero case
                            featureSequence = featuredDNASequence.getSequence().substring(
                                    genbankStart - 1, featuredDNASequence.getSequence().length());
                            featureSequence += featuredDNASequence.getSequence().substring(0, end);
                        } else { // normal
                            featureSequence = featuredDNASequence.getSequence().substring(genbankStart - 1, end);
                        }

                        if (dnaFeature.getStrand() == -1) {
                            try {
                                featureSequence = SequenceUtils.reverseComplement(featureSequence);
                            } catch (UtilityException e) {
                                featureSequence = "";
                            }
                        }
                    }

                    AnnotationType annotationType = null;
                    if (dnaFeature.getAnnotationType() != null && !dnaFeature.getAnnotationType().isEmpty()) {
                        annotationType = AnnotationType.valueOf(dnaFeature.getAnnotationType());
                    }

                    Feature feature = new Feature(dnaFeature.getName(), dnaFeature.getIdentifier(), featureSequence, 0,
                                                  dnaFeature.getType());
                    if (dnaFeature.getLocations() != null && !dnaFeature.getLocations().isEmpty())
                        feature.setUri(dnaFeature.getLocations().get(0).getUri());

                    SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature,
                                                                          dnaFeature.getStrand(), dnaFeature.getName(),
                                                                          dnaFeature.getType(), annotationType);
                    sequenceFeature.setUri(dnaFeature.getUri());

                    for (DNAFeatureLocation location : locations) {
                        int start = location.getGenbankStart();
                        int end = location.getEnd();
                        AnnotationLocation annotationLocation = new AnnotationLocation(start, end, sequenceFeature);
                        sequenceFeature.getAnnotationLocations().add(annotationLocation);
                    }

                    ArrayList<SequenceFeatureAttribute> sequenceFeatureAttributes = new ArrayList<>();
                    if (dnaFeature.getNotes() != null && dnaFeature.getNotes().size() > 0) {
                        for (DNAFeatureNote dnaFeatureNote : dnaFeature.getNotes()) {
                            SequenceFeatureAttribute sequenceFeatureAttribute = new SequenceFeatureAttribute();
                            sequenceFeatureAttribute.setSequenceFeature(sequenceFeature);
                            sequenceFeatureAttribute.setKey(dnaFeatureNote.getName());
                            sequenceFeatureAttribute.setValue(dnaFeatureNote.getValue());
                            sequenceFeatureAttribute.setQuoted(dnaFeatureNote.isQuoted());
                            sequenceFeatureAttributes.add(sequenceFeatureAttribute);
                        }
                    }

                    sequenceFeature.getSequenceFeatureAttributes().addAll(sequenceFeatureAttributes);
                    sequenceFeatures.add(sequenceFeature);
                }
            }
        }

        return sequence;
    }

    public Sequence saveSequence(Sequence partSequence) throws ControllerException {
        try {
            Sequence sequence = dao.create(partSequence);
            if (sequence != null)
                ApplicationController.scheduleBlastIndexRebuildTask(true);
            return sequence;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ByteArrayWrapper getSequenceFile(String userId, long partId, String type) {
        Entry entry = retriever.get(userId, partId);
        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");

        ByteArrayWrapper wrapper;
        String name;
        String sequenceString;

        try {
            switch (type) {
                case "original":
                    sequenceString = sequence.getSequenceUser();
                    name = entry.getName() + ".seq";
                    break;

                case "genbank":
                default:
                    GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
                    // TODO
                    genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular() : false);
                    sequenceString = compose(sequence, genbankFormatter);
                    name = entry.getName() + ".gb";
                    break;

                case "fasta":
                    FastaFormatter formatter = new FastaFormatter(sequence.getEntry().getName());
                    sequenceString = compose(sequence, formatter);
                    name = entry.getName() + ".fasta";
                    break;

                case "sbol":
                    sequenceString = compose(sequence, new SBOLFormatter());
                    name = entry.getName() + ".xml";
                    break;

                case "pigeonI":
                    try {
                        URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                        byte[] bytes = IOUtils.toByteArray(uri.toURL().openStream());
                        return new ByteArrayWrapper(bytes, entry.getName() + ".png");
                    } catch (Exception e) {
                        Logger.error(e);
                        return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");
                    }

                case "pigeonS":
                    sequenceString = PigeonSBOLv.generatePigeonScript(sequence);
                    name = entry.getName() + ".txt";
                    break;
            }
        } catch (ControllerException e) {
            Logger.error("Failed to generate genbank file for download!", e);
            return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");
        }

        return new ByteArrayWrapper(sequenceString.getBytes(), name);
    }
}
