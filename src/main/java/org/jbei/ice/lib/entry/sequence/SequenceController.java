package org.jbei.ice.lib.entry.sequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.composers.formatters.FormatterException;
import org.jbei.ice.lib.composers.formatters.IFormatter;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeature.AnnotationType;
import org.jbei.ice.lib.models.SequenceFeatureAttribute;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

/**
 * ABI to manipulate {@link Sequence}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class SequenceController {

    private final SequenceDAO dao;
    private final PermissionsController permissionsController;

    public SequenceController() {
        dao = new SequenceDAO();
        permissionsController = new PermissionsController();
    }

    /**
     * Retrieve the {@link Sequence} associated with the given {@link Entry} from the database.
     *
     * @param entry entry whose sequence is being retrieved
     * @return Sequence
     * @throws ControllerException
     */
    public Sequence getByEntry(Entry entry) throws ControllerException {
        Sequence sequence;

        try {
            sequence = dao.getByEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return sequence;
    }

    public void parseAndSaveSequence(Account account, Entry entry, String sequenceString) throws ControllerException {
        IDNASequence dnaSequence = parse(sequenceString);

        if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
            String errorMsg = "Couldn't parse sequence file! Supported formats: "
                    + GeneralParser.getInstance().availableParsersToString()
                    + ". "
                    + "If you believe this is an error, please contact the administrator with your file";

            throw new ControllerException(errorMsg);
        }

        Sequence sequence;

        try {
            sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
            sequence.setSequenceUser(sequenceString);
            sequence.setEntry(entry);
            save(account, sequence);
        } catch (PermissionException e) {
            Logger.error(e);
            throw new ControllerException("User does not have permissions to save sequence");
        }
    }

    /**
     * Save the given {@link Sequence} into the database, with the option to rebuild the search
     * index.
     *
     * @param account  account of user saving sequence
     * @param sequence sequence to save
     * @return Saved Sequence
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sequence save(Account account, Sequence sequence) throws ControllerException, PermissionException {
        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!permissionsController.hasWritePermission(account, sequence.getEntry())) {
            throw new PermissionException("No write permission for sequence!");
        }

        Sequence result;
        try {
            result = dao.saveSequence(sequence);
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return result;
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

        if (!permissionsController.hasWritePermission(account, sequence.getEntry())) {
            throw new PermissionException("No write permission for sequence!");
        }

        Sequence result;
        try {
            Entry entry = sequence.getEntry();
            entry.setModificationTime(Calendar.getInstance().getTime());
            Sequence oldSequence = getByEntry(entry);

            if (oldSequence != null) {
                String tmpDir = ControllerFactory.getConfigurationController()
                                                 .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
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
                result = dao.saveSequence(sequence);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

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
        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!permissionsController.hasWritePermission(account, sequence.getEntry())) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            String tmpDir = ControllerFactory.getConfigurationController()
                                             .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            dao.deleteSequence(sequence, tmpDir);
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Parse the given String into an {@link IDNASequence} object.
     *
     * @param sequence
     * @return parsed IDNASequence object.
     */
    public static IDNASequence parse(String sequence) {
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

    public FeaturedDNASequence retrievePartSequence(Account account, String recordId) throws ControllerException {
        Entry entry;
        try {
            entry = ControllerFactory.getEntryController().getByRecordId(account, recordId);
        } catch (PermissionException e) {
            throw new ControllerException("No permission to view part");
        }

        if (entry == null)
            throw new ControllerException("The part could not be located");

        Sequence sequence = getByEntry(entry);
        if (sequence == null)
            return null;

        return sequenceToDNASequence(sequence);
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
                sequence.getSequence(), entry.getName(), circular, features, "", "");
        featuredDNASequence.setUri(sequence.getUri());

        return featuredDNASequence;
    }

    /**
     * Create a {@link Sequence} object from an {@link IDNASequence} object.
     *
     * @param dnaSequence
     * @return Translated Sequence object.
     */
    public static Sequence dnaSequenceToSequence(IDNASequence dnaSequence) {
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

                    Feature feature = new Feature(dnaFeature.getName(), "", featureSequence, 0, dnaFeature.getType());
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

    public boolean hasSequence(long entryId) throws ControllerException {
        try {
            return dao.hasSequence(entryId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Determines if the user uploaded a sequence file and associated it with an entry
     *
     * @param entryId unique identifier for entry
     * @return true if there is a sequence file that was originally uploaded by user, false otherwise
     * @throws ControllerException
     */
    public boolean hasOriginalSequence(long entryId) throws ControllerException {
        try {
            return dao.hasOriginalSequence(entryId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * @return sequences for entries which are not deleted, not pending and not drafts
     * @throws ControllerException
     */
    public Set<Sequence> getAllSequences() throws ControllerException {
        try {
            return dao.getAllSequences();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<Feature> getAllFeatures() throws ControllerException {
        try {
            return dao.getAllFeatures();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Sequence saveSequence(Sequence partSequence) throws ControllerException {
        try {
            return dao.saveSequence(partSequence);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
