package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Part.AssemblyStandard;

public class RawAssemblyUtils implements AssemblyUtils {

    @Override
    public SequenceFeatureCollection determineAssemblyFeatures(Part part) throws UtilityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AssemblyStandard determineAssemblyStandard(String partSequenceString) {
        // TODO Auto-generated method stub
        return AssemblyStandard.RAW;
    }

    @Override
    public Sequence join(Part part1, Part part2) throws UtilityException {
        // TODO Auto-generated method stub
        return null;
    }

}
