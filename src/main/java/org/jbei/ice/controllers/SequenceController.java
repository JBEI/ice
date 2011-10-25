package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.SequencePermissionVerifier;
import org.jbei.ice.lib.composers.SequenceComposer;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.IFormatter;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeature.AnnotationType;
import org.jbei.ice.lib.models.SequenceFeatureAttribute;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

/**
 * ABI to manipulate {@link Sequence}s.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class SequenceController extends Controller {
    public SequenceController(Account account) {
        super(account, new SequencePermissionVerifier());
    }

    /**
     * Check if the user has read permission to the given {@link Sequence}.
     * 
     * @param sequence
     * @return True if user has read permission.
     */
    public boolean hasReadPermission(Sequence sequence) {
        return getSequencePermissionVerifier().hasReadPermissions(sequence, getAccount());
    }

    /**
     * Check if the user has write permission to the given {@link Sequence}.
     * 
     * @param sequence
     * @return True if user has write permission.
     */
    public boolean hasWritePermission(Sequence sequence) {
        return getSequencePermissionVerifier().hasWritePermissions(sequence, getAccount());
    }

    /**
     * Retrieve the {@link Sequence} associated with the given {@link Entry} from the database.
     * 
     * @param entry
     * @return Sequence
     * @throws ControllerException
     */
    public Sequence getByEntry(Entry entry) throws ControllerException {
        Sequence sequence = null;

        try {
            sequence = SequenceManager.getByEntry(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return sequence;
    }

    /**
     * Save the given {@link Sequence} into the database, then rebuild the search index.
     * 
     * @param sequence
     * @return Saved Sequence
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sequence save(Sequence sequence) throws ControllerException, PermissionException {
        return save(sequence, true);
    }

    /**
     * Save the given {@link Sequence} into the database, with the option to rebuild the search
     * index.
     * 
     * @param sequence
     * @param scheduleIndexRebuild
     * @return Saved Sequence
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sequence save(Sequence sequence, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        Sequence result = null;

        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!hasWritePermission(sequence)) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            if (sequence.getEntry() instanceof Part) {
                Part part = (Part) sequence.getEntry();
                AssemblyController assemblyController = new AssemblyController(getAccount());
                AssemblyStandard assemblyType = assemblyController
                        .determineAssemblyStandard(sequence);
                part.setPackageFormat(assemblyType);

                assemblyController.populateAssemblyAnnotations(sequence);

            }
            result = SequenceManager.saveSequence(sequence);
            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleBlastIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Update the {@link Sequence} in the database, then rebuild the search index.
     * <p>
     * Replace the existing sequence with a new one.
     * 
     * @param sequence
     * @return Saved Sequence.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sequence update(Sequence sequence) throws ControllerException, PermissionException {
        return update(sequence, true);
    }

    /**
     * Update the {@link Sequence} in the database, with the option to rebuild the search index.
     * 
     * @param sequence
     * @param scheduleIndexRebuild
     * @return Saved Sequence.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sequence update(Sequence sequence, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        Sequence result = null;

        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!hasWritePermission(sequence)) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            Entry entry = sequence.getEntry();

            if (entry != null) {
                Sequence oldSequence = getByEntry(entry);

                if (oldSequence != null) {
                    if ((sequence.getSequenceUser() == null || sequence.getSequenceUser().isEmpty())
                            && oldSequence.getSequenceUser() != null) {
                        sequence.setSequenceUser(oldSequence.getSequenceUser());
                    }

                    SequenceManager.deleteSequence(oldSequence);
                }
            }

            result = save(sequence);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (scheduleIndexRebuild) {
            ApplicationContoller.scheduleBlastIndexRebuildJob();
        }

        return result;
    }

    /**
     * Delete the {@link Sequence} in the database, then rebuild the search index.
     * 
     * @param sequence
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Sequence sequence) throws ControllerException, PermissionException {
        delete(sequence, true);
    }

    /**
     * Delete the {@link Sequence} in the database, with the option to rebuild the search index.
     * 
     * @param sequence
     * @param scheduleIndexRebuild
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Sequence sequence, boolean scheduleIndexRebuild) throws ControllerException,
            PermissionException {
        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!hasWritePermission(sequence)) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            SequenceManager.deleteSequence(sequence);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleBlastIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Parse the given String into an {@link IDNASequence} object.
     * 
     * @param sequence
     * @return
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
     * @throws SequenceComposerException
     */
    public static String compose(Sequence sequence, IFormatter formatter)
            throws SequenceComposerException {
        return SequenceComposer.compose(sequence, formatter);
    }

    /**
     * Generate a {@link FeaturedDNASequence} from a given {@link Sequence} object.
     * 
     * @param sequence
     * @return FeaturedDNASequence
     */
    public static FeaturedDNASequence sequenceToDNASequence(Sequence sequence) {
        if (sequence == null) {
            return null;
        }

        List<DNAFeature> features = new LinkedList<DNAFeature>();

        if (sequence.getSequenceFeatures() != null && sequence.getSequenceFeatures().size() > 0) {
            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                DNAFeature dnaFeature = new DNAFeature();

                for (SequenceFeatureAttribute attribute : sequenceFeature
                        .getSequenceFeatureAttributes()) {
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

        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence(sequence.getSequence(),
                sequence.getEntry().getNamesAsString(),
                (sequence.getEntry() instanceof Plasmid) ? ((Plasmid) sequence.getEntry())
                        .getCircular() : false, features, "", "");

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
        String revHash = SequenceUtils.calculateSequenceHash(SequenceUtils
                .reverseComplement(sequenceString));

        Sequence sequence = new Sequence(sequenceString, "", fwdHash, revHash, null);
        Set<SequenceFeature> sequenceFeatures = sequence.getSequenceFeatures();
        if (dnaSequence instanceof FeaturedDNASequence) {
            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) dnaSequence;

            if (featuredDNASequence.getFeatures() != null
                    && featuredDNASequence.getFeatures().size() > 0) {
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
                            featureSequence += featuredDNASequence.getSequence().substring(
                                genbankStart - 1, end);
                        }

                        if (genbankStart > end) { // over zero case
                            featureSequence = featuredDNASequence.getSequence().substring(
                                genbankStart - 1, featuredDNASequence.getSequence().length());
                            featureSequence += featuredDNASequence.getSequence().substring(0, end);
                        } else { // normal
                            featureSequence = featuredDNASequence.getSequence().substring(
                                genbankStart - 1, end);
                        }

                        if (dnaFeature.getStrand() == -1) {
                            featureSequence = SequenceUtils.reverseComplement(featureSequence);
                        }
                    }

                    AnnotationType annotationType = null;
                    if (dnaFeature.getAnnotationType() != null
                            && !dnaFeature.getAnnotationType().isEmpty()) {
                        annotationType = AnnotationType.valueOf(dnaFeature.getAnnotationType());
                    }

                    Feature feature = new Feature(dnaFeature.getName(), "", featureSequence, 0,
                            dnaFeature.getType());

                    SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature,
                            dnaFeature.getStrand(), dnaFeature.getName(), dnaFeature.getType(),
                            annotationType);

                    for (DNAFeatureLocation location1 : locations) {
                        sequenceFeature.getAnnotationLocations().add(
                            new AnnotationLocation(location1.getGenbankStart(), location1.getEnd(),
                                    sequenceFeature));
                    }

                    ArrayList<SequenceFeatureAttribute> sequenceFeatureAttributes = new ArrayList<SequenceFeatureAttribute>();
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

                    sequenceFeature.getSequenceFeatureAttributes()
                            .addAll(sequenceFeatureAttributes);
                    sequenceFeatures.add(sequenceFeature);
                }
            }
        }

        return sequence;
    }

    /**
     * Return the {@link SequencePermissionVerifier}.
     * 
     * @return sequencePermissionVerifier.
     */
    protected SequencePermissionVerifier getSequencePermissionVerifier() {
        return (SequencePermissionVerifier) getPermissionVerifier();
    }

}
