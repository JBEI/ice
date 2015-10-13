package org.jbei.ice.lib.entry.sequence.composers.pigeon;

import org.jbei.ice.storage.model.AnnotationLocation;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;

import java.util.Set;

/**
 * @author Hector Plahar
 */
public class SequenceVisitor {

    public SequenceVisitor() {
    }

    public void visit(Sequence sequence) {
        Set<SequenceFeature> features = sequence.getSequenceFeatures();
        if (features != null) {
            for (SequenceFeature feature : features)
                visit(feature);
        }
    }

    public void visit(SequenceFeature feature) {
        AnnotationLocation location = null;
        if (feature.getAnnotationLocations() != null && !feature.getAnnotationLocations().isEmpty()) {
            location = (AnnotationLocation) feature.getAnnotationLocations().toArray()[0];

            if (location.getGenbankStart() <= location.getEnd()) {
            }
        }

        // add a dna sequence for cases where the feature wraps around the origin
        if (location != null && location.getGenbankStart() > location.getEnd()) {
            String sequence = location.getSequenceFeature().getSequence().getSequence();
            StringBuilder builder = new StringBuilder();
            builder.append(sequence.substring(location.getGenbankStart() - 1, sequence.length()));
            builder.append(sequence.substring(0, location.getEnd()));
        }
    }
}
