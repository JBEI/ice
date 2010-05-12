package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Part.AssemblyStandard;

public interface AssemblyUtils {

    public AssemblyStandard determineAssemblyStandard(String partSequenceString)
            throws UtilityException;

    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException;

    public Sequence populateAssemblyFeatures(Sequence partSequence) throws UtilityException;

    public Sequence join(Sequence part1, Sequence part2) throws UtilityException;

    public int compareAssemblyAnnotations(SequenceFeatureCollection sequenceFeatures1,
            SequenceFeatureCollection sequenceFeatures2);
}
