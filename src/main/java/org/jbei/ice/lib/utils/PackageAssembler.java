package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.models.Part;

public abstract class PackageAssembler {
    public abstract Part join(Part part1, Part part2) throws UtilityException;

    protected abstract String concatSequence(String part1Sequence, String part2Sequence)
            throws UtilityException;

}
