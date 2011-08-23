package org.jbei.ice.lib.utils;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;

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
    public Sequence populateAssemblyFeatures(Sequence partSequence) throws UtilityException {
        SequenceFeatureCollection newSequenceFeatures = determineAssemblyFeatures(partSequence);
        Set<SequenceFeature> temp = partSequence.getSequenceFeatures();
        if (!(temp instanceof SequenceFeatureCollection)) {
            throw new UtilityException("Not A SequenceFeatureCollection");
        }
        SequenceFeatureCollection oldSequenceFeatures = (SequenceFeatureCollection) temp;
        for (SequenceFeature newSequenceFeature : newSequenceFeatures) {
            List<SequenceFeature> foundOldSequenceFeatures = oldSequenceFeatures
                    .getBySequence(newSequenceFeature.getFeature().getSequence());
            if (foundOldSequenceFeatures.size() > 1) {
                // multiple with same sequence. Remove them and replace with new feature
                oldSequenceFeatures.removeAll(foundOldSequenceFeatures);
                oldSequenceFeatures.add(newSequenceFeature);
            } else if (foundOldSequenceFeatures.size() == 1) {
                SequenceFeature foundOldSequenceFeature = foundOldSequenceFeatures.get(0);
                if (!(foundOldSequenceFeature.getAnnotationType() == newSequenceFeature
                        .getAnnotationType())) {
                    oldSequenceFeatures.remove(foundOldSequenceFeature);
                    oldSequenceFeatures.add(newSequenceFeature);
                }
            } else if (foundOldSequenceFeatures.size() == 0) {
                oldSequenceFeatures.add(newSequenceFeature);
            }
        }
        try {
            SequenceManager.saveSequence(partSequence);
        } catch (ManagerException e) {
            throw new UtilityException(e);
        }
        return partSequence;
    }

    @Override
    public Sequence join(Sequence part1, Sequence part2) throws UtilityException {
        throw new NotImplementedException();
    }

    @Override
    public int compareAssemblyAnnotations(SequenceFeatureCollection sequenceFeatures1,
            SequenceFeatureCollection sequenceFeatures2) {
        int result = 1;
        if (sequenceFeatures1 == null || sequenceFeatures2 == null) {
            return result;
        }
        SequenceFeature inner1 = null;
        SequenceFeature inner2 = null;
        List<SequenceFeature> temp = sequenceFeatures1.get(SequenceFeature.AnnotationType.INNER);
        if (temp.size() == 1) {
            inner1 = temp.get(0);
        }

        temp = sequenceFeatures2.get(SequenceFeature.AnnotationType.INNER);
        if (temp.size() == 1) {
            inner2 = temp.get(0);
        }

        if (inner1 != null && inner2 != null) {
            if (inner1.getSequence().getFwdHash().equals(inner2.getSequence().getFwdHash())) {
                result = 0;
            }
        }

        return result;
    }

    private SequenceFeatureCollection determineRawAssemblyFeatures(Sequence partSequence) {
        SequenceFeatureCollection sequenceFeatures = new SequenceFeatureCollection();
        String partSequenceString = partSequence.getSequence();
        Entry part = partSequence.getEntry();
        String featureName = part.getRecordId(); // uuid of the given part
        String featureIdentification = part.getRecordId();
        Feature innerPartFeature = new Feature(featureName, featureIdentification,
                partSequenceString, 0, "misc_feature");
        SequenceFeature sequenceFeature = new SequenceFeature(partSequence, innerPartFeature, 1,
                partSequenceString.length(), +1, innerPartFeature.getName(),
                innerPartFeature.getGenbankType(),
                SequenceFeature.AnnotationType.INNER);

        sequenceFeatures.add(sequenceFeature);

        return sequenceFeatures;
    }

}
