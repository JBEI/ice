package org.jbei.ice.lib.utils;

import org.jbei.ice.storage.model.Part;

/**
 * Abstract class for assembling standardized format DNA.
 *
 * @author Timothy Ham
 */
public abstract class PackageAssembler {
    /**
     * Join two standardized {@link Part}s.
     *
     * @param part1
     * @param part2
     * @return joined Part object.
     * @throws UtilityException
     */
    public abstract Part join(Part part1, Part part2) throws UtilityException;

    /**
     * Concatinate two DNA sequences.
     *
     * @param part1Sequence
     * @param part2Sequence
     * @return joined Part object.
     * @throws UtilityException
     */
    protected abstract String concatSequence(String part1Sequence, String part2Sequence)
            throws UtilityException;

}
