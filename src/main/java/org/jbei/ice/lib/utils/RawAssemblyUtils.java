package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.Part.AssemblyStandard;

public class RawAssemblyUtils implements AssemblyUtils {

    @Override
    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException {
        return determineRawAssemblyFeatures(partSequence);
    }

    @Override
    public AssemblyStandard determineAssemblyStandard(String partSequenceString) {
        return AssemblyStandard.RAW;
    }

    @Override
    public Sequence join(Part part1, Part part2) throws UtilityException {
        // TODO Auto-generated method stub
        return null;
    }

    private SequenceFeatureCollection determineRawAssemblyFeatures(Sequence partSequence) {
        SequenceFeatureCollection sequenceFeatures = new SequenceFeatureCollection();
        String partSequenceString = partSequence.getSequence();
        Entry part = partSequence.getEntry();
        String featureName = part.getRecordId(); // uuid of the given part
        String featureDescription = featureName;
        String featureIdentification = part.getRecordId();
        Feature innerPartFeature = new Feature(featureName, featureDescription,
                featureIdentification, partSequenceString, 0, "misc_feature");
        SequenceFeature sequenceFeature = new SequenceFeature(partSequence, innerPartFeature, 1,
                partSequenceString.length(), +1, innerPartFeature.getName(), innerPartFeature
                        .getDescription(), innerPartFeature.getGenbankType());
        sequenceFeature.setFlag(SequenceFeature.Flag.INNER);
        sequenceFeatures.add(sequenceFeature);
        return sequenceFeatures;
    }
}
