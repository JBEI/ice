package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Note;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jbei.ice.lib.models.FeatureDNA;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;

public class GenbankParser extends AbstractParser {
    @Override
    @SuppressWarnings("unchecked")
    public Sequence parse(String textSequence) throws InvalidFormatParserException {
        BufferedReader br = new BufferedReader(new StringReader(textSequence));
        Sequence sequence = null;

        try {
            RichSequenceIterator richSequences = IOTools.readGenbankDNA(br, null);

            if (richSequences.hasNext()) {
                RichSequence richSequence = richSequences.nextRichSequence();

                Set<Feature> featureSet = richSequence.getFeatureSet();

                Set<SequenceFeature> sequenceFeatureSet = new HashSet<SequenceFeature>();

                sequence = new Sequence(richSequence.seqString(), textSequence, "", "", null,
                        sequenceFeatureSet);

                for (Feature feature : featureSet) {
                    RichFeature richFeature = (RichFeature) feature;

                    String featureDescription = "";
                    String featureName = "";

                    Set<Note> notes = richFeature.getNoteSet();
                    for (Note note : notes) {
                        featureDescription = note.getTerm().getName() + "=" + note.getValue();

                        if (note.getTerm().getName().toLowerCase().equals("name")
                                || note.getTerm().getName().toLowerCase().equals("label")) {
                            featureName = note.getValue();
                        }
                    }

                    RichLocation featureLocation = (RichLocation) richFeature.getLocation();
                    String genbankType = richFeature.getType();
                    int start = featureLocation.getMin();
                    int end = featureLocation.getMax();

                    String featureDNASequence = sequence.getSequence()
                            .substring(start - 1, end - 1);

                    String featureDNASequenceHash = SequenceUtils
                            .calculateSequenceHash(featureDNASequence);

                    org.jbei.ice.lib.models.Feature ourFeature = new org.jbei.ice.lib.models.Feature(
                            featureName, featureDescription, "", Utils.generateUUID(), 0,
                            genbankType);

                    FeatureDNA featureDNA = new FeatureDNA(featureDNASequenceHash,
                            featureDNASequence, ourFeature);

                    ourFeature.setFeatureDna(featureDNA);

                    SequenceFeature sequenceFeature = new SequenceFeature(sequence, ourFeature,
                            start, end, featureLocation.getStrand().intValue(), featureName);

                    sequenceFeatureSet.add(sequenceFeature);
                }

                sequence.setFwdHash(SequenceUtils.calculateSequenceHash(sequence.getSequence()));
                sequence.setRevHash(SequenceUtils.calculateSequenceHash(SequenceUtils
                        .reverseComplement(sequence.getSequence())));
            }
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couln't parse GenBank sequence!", e);
        }

        return sequence;
    }

    @Override
    public String getName() {
        return "GenBank";
    }
}
