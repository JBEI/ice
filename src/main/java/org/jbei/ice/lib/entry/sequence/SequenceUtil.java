package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SequenceUtil {
    /**
     * Create a {@link Sequence} object from an {@link DNASequence} object.
     *
     * @param dnaSequence object to convert
     * @return Translated Sequence object.
     */
    public static Sequence dnaSequenceToSequence(DNASequence dnaSequence) {
        if (dnaSequence == null) {
            return null;
        }

        String fwdHash = "";
        String revHash = "";

        String sequenceString = dnaSequence.getSequence();
        if (!StringUtils.isEmpty(sequenceString)) {
            fwdHash = SequenceUtils.calculateSequenceHash(sequenceString);
            try {
                revHash = SequenceUtils.calculateSequenceHash(SequenceUtils.reverseComplement(sequenceString));
            } catch (UtilityException e) {
                revHash = "";
            }
        }

        Sequence sequence = new Sequence(sequenceString, "", fwdHash, revHash, null);
        Set<SequenceFeature> sequenceFeatures = sequence.getSequenceFeatures();

        if (dnaSequence instanceof FeaturedDNASequence) {
            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) dnaSequence;
            sequence.setUri(featuredDNASequence.getUri());
            sequence.setComponentUri(featuredDNASequence.getDcUri());
            sequence.setIdentifier(featuredDNASequence.getIdentifier());

            if (featuredDNASequence.getFeatures() != null && !featuredDNASequence.getFeatures().isEmpty()) {
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
                            featureSequence = featuredDNASequence.getSequence().substring(
                                    genbankStart - 1, featuredDNASequence.getSequence().length());
                            featureSequence += featuredDNASequence.getSequence().substring(0, end);
                        } else { // normal
                            featureSequence = featuredDNASequence.getSequence().substring(genbankStart - 1, end);
                        }

                        if (dnaFeature.getStrand() == -1) {
                            try {
                                featureSequence = SequenceUtils.reverseComplement(featureSequence);
                            } catch (UtilityException e) {
                                featureSequence = "";
                            }
                        }
                    }

                    SequenceFeature.AnnotationType annotationType = null;
                    if (dnaFeature.getAnnotationType() != null && !dnaFeature.getAnnotationType().isEmpty()) {
                        annotationType = SequenceFeature.AnnotationType.valueOf(dnaFeature.getAnnotationType());
                    }

                    String name = dnaFeature.getName().length() < 127 ? dnaFeature.getName()
                            : dnaFeature.getName().substring(0, 123) + "...";
                    Feature feature = new Feature(name, dnaFeature.getIdentifier(), featureSequence,
                            dnaFeature.getType());
                    if (dnaFeature.getLocations() != null && !dnaFeature.getLocations().isEmpty())
                        feature.setUri(dnaFeature.getLocations().get(0).getUri());

                    SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature,
                            dnaFeature.getStrand(), name,
                            dnaFeature.getType(), annotationType);
                    sequenceFeature.setUri(dnaFeature.getUri());

                    for (DNAFeatureLocation location : locations) {
                        int start = location.getGenbankStart();
                        int end = location.getEnd();
                        AnnotationLocation annotationLocation = new AnnotationLocation(start, end, sequenceFeature);
                        sequenceFeature.getAnnotationLocations().add(annotationLocation);
                    }

                    ArrayList<SequenceFeatureAttribute> sequenceFeatureAttributes = new ArrayList<>();
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

                    sequenceFeature.getSequenceFeatureAttributes().addAll(sequenceFeatureAttributes);
                    sequenceFeatures.add(sequenceFeature);
                }
            }
        }

        return sequence;
    }
}
