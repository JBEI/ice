package org.jbei.ice.controllers;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part.AssemblyStandard;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.BiobrickAUtils;
import org.jbei.ice.lib.utils.BiobrickBUtils;
import org.jbei.ice.lib.utils.IAssemblyUtils;
import org.jbei.ice.lib.utils.RawAssemblyUtils;
import org.jbei.ice.lib.utils.SequenceFeatureCollection;
import org.jbei.ice.lib.utils.UtilityException;

import java.util.ArrayList;

/**
 * ABI to manipulate DNA Assembly sequences (BioBricks).
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class AssemblyController extends Controller {
    private ArrayList<IAssemblyUtils> assemblyUtils = new ArrayList<IAssemblyUtils>();

    public AssemblyController(Account account) {
        super(account, new EntryPermissionVerifier());
        getAssemblyUtils().add(new BiobrickAUtils());
        getAssemblyUtils().add(new BiobrickBUtils());
        getAssemblyUtils().add(new RawAssemblyUtils());
    }

    /**
     * Determine the {@link AssemblyStandard} of the given sequence.
     *
     * @param partSequence
     * @return assemblyStandard
     * @throws ControllerException
     */
    public AssemblyStandard determineAssemblyStandard(Sequence partSequence)
            throws ControllerException {
        AssemblyStandard result = null;
        String partSequenceString = partSequence.getSequence();
        result = determineAssemblyStandard(partSequenceString);
        return result;
    }

    /**
     * Determine the {@link AssemblyStandard} of the given sequence String.
     *
     * @param partSequenceString
     * @return assemblyStandard
     * @throws ControllerException
     */
    public AssemblyStandard determineAssemblyStandard(String partSequenceString)
            throws ControllerException {
        AssemblyStandard result = null;
        int counter = 0;
        while (result == null && counter < getAssemblyUtils().size()) {
            try {
                result = getAssemblyUtils().get(counter).determineAssemblyStandard(
                        partSequenceString);
            } catch (UtilityException e) {
                throw new ControllerException(e);
            }
            counter = counter + 1;
        }
        return result;
    }

    /**
     * Test for different assembly standards, and return the assembly features appropriate for that
     * standard, if any.
     *
     * @param partSequence
     * @return SequenceFeatureCollection object.
     * @throws ControllerException
     */
    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws ControllerException {

        SequenceFeatureCollection sequenceFeatures = null;
        String partSequenceString = partSequence.getSequence();
        AssemblyStandard standard = determineAssemblyStandard(partSequenceString);
        try {
            if (standard == AssemblyStandard.BIOBRICKA) {
                sequenceFeatures = getAssemblyUtils().get(0)
                        .determineAssemblyFeatures(partSequence);
            } else if (standard == AssemblyStandard.BIOBRICKB) {
                sequenceFeatures = getAssemblyUtils().get(1)
                        .determineAssemblyFeatures(partSequence);
            } else if (standard == AssemblyStandard.RAW) {
                sequenceFeatures = getAssemblyUtils().get(2)
                        .determineAssemblyFeatures(partSequence);
            }
        } catch (UtilityException e) {
            throw new ControllerException(e);
        }

        return sequenceFeatures;
    }

    /**
     * Comparator for assembly basic sequence features.
     * <p/>
     * Uses Assembly Annotations the system knows about to compare identity.
     *
     * @param sequenceFeatures1
     * @param sequenceFeatures2
     * @return 0 if identical.
     */
    public int compareAssemblyAnnotations(AssemblyStandard standard,
            SequenceFeatureCollection sequenceFeatures1, SequenceFeatureCollection sequenceFeatures2) {
        int result = -1;
        if (standard == AssemblyStandard.BIOBRICKA) {
            result = getAssemblyUtils().get(0).compareAssemblyAnnotations(sequenceFeatures1,
                                                                          sequenceFeatures2);
        } else if (standard == AssemblyStandard.BIOBRICKB) {
            result = getAssemblyUtils().get(1).compareAssemblyAnnotations(sequenceFeatures1,
                                                                          sequenceFeatures2);
        } else if (standard == AssemblyStandard.RAW) {
            result = getAssemblyUtils().get(2).compareAssemblyAnnotations(sequenceFeatures1,
                                                                          sequenceFeatures2);
        }
        return result;
    }

    /**
     * Automatically annotate BioBrick parts.
     * <p/>
     * Annotates prefixes, suffixes, and scar sequences by calling populateAssemblyFeatures for
     * assembly methods known to the system.
     *
     * @param partSequence
     * @return True if
     * @throws ControllerException
     */
    public boolean populateAssemblyAnnotations(Sequence partSequence) throws ControllerException {
        Sequence result = null;
        AssemblyStandard standard = determineAssemblyStandard(partSequence);
        try {
            if (standard == AssemblyStandard.BIOBRICKA) {
                result = getAssemblyUtils().get(0).populateAssemblyFeatures(partSequence);
            } else if (standard == AssemblyStandard.BIOBRICKB) {
                result = getAssemblyUtils().get(1).populateAssemblyFeatures(partSequence);
            } else if (standard == AssemblyStandard.RAW) {
                result = getAssemblyUtils().get(2).populateAssemblyFeatures(partSequence);
            }
        } catch (UtilityException e) {
            // If auto annotation fails, continue.
            Logger.warn("Error in annotating assembly features for "
                                + partSequence.getEntry().getId());
            Logger.warn(e.toString());
        }
        if (result != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Join two BioBrick sequences together.
     * <p/>
     * Join using the proper BioBrick assembly algorithm and saves the result into the database as a
     * new {@link Entry}.
     *
     * @param sequence1
     * @param sequence2
     * @return New joined Sequence. Null if join failed.
     * @throws ControllerException
     */
    public Sequence joinBiobricks(Sequence sequence1, Sequence sequence2)
            throws ControllerException {
        AssemblyStandard part1Standard = determineAssemblyStandard(sequence1);
        AssemblyStandard part2Standard = determineAssemblyStandard(sequence2);

        Sequence result = null;

        if (part1Standard != part2Standard) {
            return null;
        }
        try {
            if (part1Standard == AssemblyStandard.BIOBRICKA) {
                result = getAssemblyUtils().get(0).join(sequence1, sequence2);
            } else if (part1Standard == AssemblyStandard.BIOBRICKB) {
                result = getAssemblyUtils().get(1).join(sequence1, sequence2);
            } else if (part1Standard == AssemblyStandard.RAW) {
                result = getAssemblyUtils().get(3).join(sequence1, sequence2);
            }
        } catch (UtilityException e) {
            throw new ControllerException(e);
        }

        Entry entry = result.getEntry();
        entry.setCreator(getAccount().getFullName());
        entry.setCreatorEmail(getAccount().getEmail());
        entry.setOwner(getAccount().getFullName());
        entry.setOwnerEmail(getAccount().getEmail());
        SequenceController sequenceController = null;
        try {
            sequenceController = new SequenceController();
            result = sequenceController.save(getAccount(), result);
        } catch (PermissionException e) {
            throw new ControllerException(e);
        }
        return result;
    }

    public ArrayList<IAssemblyUtils> getAssemblyUtils() {
        return assemblyUtils;
    }
}
