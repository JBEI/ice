package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.entry.model.Part.AssemblyStandard;
import org.jbei.ice.lib.models.Sequence;

/**
 * Interface for for Biobrick Assembly.
 *
 * @author Timothy Ham
 */
public interface IAssemblyUtils {

    /**
     * Determine the assembly standard of given sequence string.
     *
     * @param partSequenceString Sequence string to evaluate.
     * @return {@link AssemblyStandard}.
     * @throws UtilityException
     */
    public AssemblyStandard determineAssemblyStandard(String partSequenceString)
            throws UtilityException;

    /**
     * Search for recognized assembly format features such as prefixes, suffixes and
     * scars for a given {@link Sequence}.
     *
     * @param partSequence Sequence to evaluate.
     * @return {@link SequenceFeatureCollection}.
     * @throws UtilityException
     */
    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException;

    /**
     * Automatically annotate the given {@link Sequence} for recognized assembly features, such as
     * prefixes, suffixes and scars.
     *
     * @param partSequence Sequence to evaluate.
     * @return Sequence with new annotations.
     * @throws UtilityException
     */
    public Sequence populateAssemblyFeatures(Sequence partSequence) throws UtilityException;

    /**
     * Perform assembly join operations, calculating the correct prefix/suffix and/or scar
     * sequences.
     *
     * @param part1 First part.
     * @param part2 Second part.
     * @return {@link Sequence} of the joined assembly.
     * @throws UtilityException
     */
    public Sequence join(Sequence part1, Sequence part2) throws UtilityException;

    /**
     * Compare two {@link SequenceFeatureCollection}s to determine if they are equal.
     *
     * @param sequenceFeatures1
     * @param sequenceFeatures2
     * @return Joined sequence.
     */
    public int compareAssemblyAnnotations(SequenceFeatureCollection sequenceFeatures1,
            SequenceFeatureCollection sequenceFeatures2);
}
