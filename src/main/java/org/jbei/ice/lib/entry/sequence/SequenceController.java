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
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.sequence.composers.formatters.FormatterException;
import org.jbei.ice.lib.entry.sequence.composers.formatters.IFormatter;
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

/**
 * ABI to manipulate {@link Sequence}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class SequenceController {

    private final SequenceDAO dao;
    private final EntryAuthorization authorization;

    public SequenceController() {
        dao = new SequenceDAO();
        authorization = new EntryAuthorization();
    }

    public void parseAndSaveSequence(Account account, Entry entry, String sequenceString) throws ControllerException {
        DNASequence dnaSequence = parse(sequenceString);

        if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
            String errorMsg = "Couldn't parse sequence file! Supported formats: "
                    + GeneralParser.getInstance().availableParsersToString()
                    + ". "
                    + "If you believe this is an error, please contact the administrator with your file";

            throw new ControllerException(errorMsg);
        }

        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        save(account, sequence);
    }

    /**
     * Save the given {@link Sequence} into the database, with the option to rebuild the search
     * index.
     *
     * @param account  account of user saving sequence
     * @param sequence sequence to save
     * @return Saved Sequence
     * @throws AuthorizationException if the user does not have the permission to update the entry associated with
     *                                the sequence
     */
    public Sequence save(Account account, Sequence sequence) throws AuthorizationException {
        authorization.expectWrite(account.getEmail(), sequence.getEntry());
        Sequence result = dao.create(sequence);
        ApplicationController.scheduleBlastIndexRebuildTask(true);
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

        authorization.expectWrite(account.getEmail(), sequence.getEntry());
        Sequence result;

        try {
            Entry entry = sequence.getEntry();
            entry.setModificationTime(Calendar.getInstance().getTime());
            Sequence oldSequence = dao.getByEntry(entry);

            if (oldSequence != null) {
                String tmpDir = new ConfigurationController()
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
                result = dao.create(sequence);
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
        authorization.expectWrite(account.getEmail(), sequence.getEntry());
        String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        dao.deleteSequence(sequence, tmpDir);
        ApplicationController.scheduleBlastIndexRebuildTask(true);
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

    public FeaturedDNASequence retrievePartSequence(Account account, String recordId) throws ControllerException {
        Entry entry = DAOFactory.getEntryDAO().getByRecordId(recordId);

        if (entry == null)
            throw new ControllerException("The part could not be located");

        Sequence sequence = dao.getByEntry(entry);
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
}
