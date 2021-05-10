package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.storage.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SequenceUtil {

    private static Sequence createSequenceModel(String sequenceString) {
        String fwdHash = "";
        String revHash = "";

        if (!StringUtils.isEmpty(sequenceString)) {
            fwdHash = SequenceUtils.calculateSequenceHash(sequenceString);
            try {
                revHash = SequenceUtils.calculateSequenceHash(SequenceUtils.reverseComplement(sequenceString));
            } catch (UtilityException e) {
                revHash = "";
            }
        }

        return new Sequence(sequenceString, "", fwdHash, revHash, null);
    }

    static SequenceFeature dnaFeatureToSequenceFeature(Sequence sequence, DNAFeature dnaFeature) {
        String sequenceString = sequence.getSequence();
        List<DNAFeatureLocation> locations = dnaFeature.getLocations();
        String featureSequence = ""; // raw sequence for feature

        for (DNAFeatureLocation location : locations) {
            int genbankStart = location.getGenbankStart();
            int end = location.getEnd();

            if (genbankStart < 1) {
                genbankStart = 1;
            } else if (genbankStart > sequenceString.length()) {
                genbankStart = sequenceString.length();
            }

            if (end < 1) {
                end = 1;
            } else if (end > sequenceString.length()) {
                end = sequenceString.length();
            }

            // check spanning origin
            if (genbankStart > end) { // over zero case
                featureSequence = sequenceString.substring(genbankStart - 1);
                featureSequence += sequenceString.substring(0, end);
            } else { // normal
                featureSequence = sequenceString.substring(genbankStart - 1, end);
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
        if (dnaFeature.getAnnotationType() != null) {
            annotationType = SequenceFeature.AnnotationType.valueOf(dnaFeature.getAnnotationType());
        }

        String name = dnaFeature.getName().length() < 127 ? dnaFeature.getName() :
                dnaFeature.getName().substring(0, 123) + "...";
        Feature feature = new Feature(name, dnaFeature.getIdentifier(), featureSequence, dnaFeature.getType());
        if (dnaFeature.getLocations() != null && !dnaFeature.getLocations().isEmpty())
            feature.setUri(dnaFeature.getLocations().get(0).getUri());

        SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature,
                dnaFeature.getStrand(), name, dnaFeature.getType(), annotationType);
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
        return sequenceFeature;
    }

    static FeaturedDNASequence sequenceToDNASequence(Sequence sequence, List<SequenceFeature> sequenceFeatures) {
        if (sequence == null) {
            return null;
        }

        List<DNAFeature> features = new LinkedList<>();

        if (sequenceFeatures != null) {
            for (SequenceFeature sequenceFeature : sequenceFeatures) {
                DNAFeature dnaFeature = new DNAFeature();
                dnaFeature.setUri(sequenceFeature.getUri());

                for (SequenceFeatureAttribute attribute : sequenceFeature.getSequenceFeatureAttributes()) {
                    String key = attribute.getKey();
                    String value = attribute.getValue();
                    DNAFeatureNote dnaFeatureNote = new DNAFeatureNote(key, value);
                    dnaFeatureNote.setQuoted(attribute.getQuoted());
                    dnaFeature.addNote(dnaFeatureNote);
                }

                Set<AnnotationLocation> locations = sequenceFeature.getAnnotationLocations();
                for (AnnotationLocation location : locations) {
                    dnaFeature.getLocations().add(
                            new DNAFeatureLocation(location.getGenbankStart(), location.getEnd()));
                }

                dnaFeature.setId(sequenceFeature.getId());
                String type = sequenceFeature.getFeature() != null && !StringUtils.isEmpty(sequenceFeature.getFeature().getGenbankType()) ? sequenceFeature.getFeature().getGenbankType() : sequenceFeature.getGenbankType();
                dnaFeature.setType(type);
                String name = sequenceFeature.getFeature() != null ? sequenceFeature.getFeature().getName() : sequenceFeature.getName();
                dnaFeature.setName(name);
                dnaFeature.setStrand(sequenceFeature.getStrand());

                if (sequenceFeature.getAnnotationType() != null) {
                    dnaFeature.setAnnotationType(sequenceFeature.getAnnotationType().toString());
                }

                features.add(dnaFeature);
            }
        }

        boolean circular = false;
        Entry entry = sequence.getEntry();
        if (entry.getRecordType().equalsIgnoreCase(EntryType.PLASMID.name()))
            circular = ((Plasmid) sequence.getEntry()).getCircular();
        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence(
                sequence.getSequence(), entry.getName(), circular, features, "");
        featuredDNASequence.setUri(sequence.getUri());

        return featuredDNASequence;
    }

    // todo : this causes problems when the number of annotations are a lot
//    /**
//     * Normalize {@link AnnotationLocation}s by fixing strangely defined annotationLocations.
//     * <p/>
//     * Fix locations that encompass the entire sequence, but defined strangely. This causes problems
//     * elsewhere.
//     */
//    public static Sequence normalizeAnnotationLocations(Sequence sequence) {
//        if (sequence == null) {
//            return null;
//        }
//
//        if (sequence.getSequenceFeatures() == null) {
//            return sequence;
//        }
//
//        if (StringUtils.isEmpty(sequence.getSequence()))
//            return sequence;
//
//        int length = sequence.getSequence().length();
//        boolean wholeSequence;
//        for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
//            wholeSequence = false;
//            Set<AnnotationLocation> locations = sequenceFeature.getAnnotationLocations();
//            for (AnnotationLocation location : locations) {
//                if (location.getGenbankStart() == location.getEnd() + 1) {
//                    wholeSequence = true;
//                }
//            }
//            if (wholeSequence) {
//                sequenceFeature.setStrand(1);
//                sequenceFeature.getAnnotationLocations().clear();
//                sequenceFeature.getAnnotationLocations().add(new AnnotationLocation(1, length, sequenceFeature));
//            }
//        }
//        return sequence;
//    }

    /**
     * Create a {@link Sequence} object from an {@link DNASequence} object.
     *
     * @param dnaSequence object to convert
     * @return Translated Sequence object.
     */
    public static Sequence dnaSequenceToSequence(FeaturedDNASequence dnaSequence) {
        if (dnaSequence == null) {
            return null;
        }

        Sequence sequence = createSequenceModel(dnaSequence.getSequence());
        Set<SequenceFeature> sequenceFeatures = sequence.getSequenceFeatures();

        sequence.setUri(dnaSequence.getUri());
        sequence.setComponentUri(dnaSequence.getDcUri());
        sequence.setIdentifier(dnaSequence.getIdentifier());

        if (dnaSequence.getFeatures() != null) {
            for (DNAFeature dnaFeature : dnaSequence.getFeatures()) {
                SequenceFeature sequenceFeature = dnaFeatureToSequenceFeature(sequence, dnaFeature);
                sequenceFeatures.add(sequenceFeature);
            }
        }

        return sequence;
    }

    /**
     * Attempts to detect the sequence format from the first line of a stream using the following heuristics
     * <ul>
     * <li>If line starts with <code>LOCUS</code> then assumed to be a genbank file</li>
     * <li>If line starts with <code>></code> then assumed to be a fasta file</li>
     * <li>If line starts with <code><</code> then assumed to be an sbol file</li>
     * <li>Anything else is assumed to be plain nucleotides</li>
     * </ul>
     *
     * @param sequenceString input sequence
     * @return detected format
     * @throws IOException on exception parsing document
     */
    public static SequenceFormat detectFormat(String sequenceString) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(sequenceString));
        String line = reader.readLine();
        if (line == null)
            throw new IOException("Could not obtain line from document");

        line = line.trim();

        if (line.startsWith("LOCUS"))
            return SequenceFormat.GENBANK;

        if (line.startsWith(">"))
            return SequenceFormat.FASTA;

        if (line.startsWith("<"))
            return SequenceFormat.SBOL2;

        return SequenceFormat.PLAIN;
    }
}
