package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Note;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

public class GenbankParser extends AbstractParser {
    private static final String GENBANK_PARSER = "GenBank";

    public String getName() {
        return GENBANK_PARSER;
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }

    @Override
    @SuppressWarnings("unchecked")
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        textSequence = cleanSequence(textSequence);

        BufferedReader br = new BufferedReader(new StringReader(textSequence));
        FeaturedDNASequence sequence = null;

        try {
            RichSequenceIterator richSequences = IOTools.readGenbankDNA(br, null);

            if (richSequences.hasNext()) {
                RichSequence richSequence = richSequences.nextRichSequence();

                Set<Feature> featureSet = richSequence.getFeatureSet();

                List<DNAFeature> dnaFeatures = new LinkedList<DNAFeature>();

                sequence = new FeaturedDNASequence(richSequence.seqString(), dnaFeatures);

                for (Feature feature : featureSet) {
                    RichFeature richFeature = (RichFeature) feature;

                    String featureName = "";

                    Map<String, String> notesMap = new LinkedHashMap<String, String>();
                    Set<Note> notes = richFeature.getNoteSet();
                    for (Note note : notes) {
                        if (note.getTerm().getName().toLowerCase().equals("name")) {
                            notesMap.put("name", note.getValue());

                            featureName = note.getValue();

                            continue;
                        } else if (note.getTerm().getName().toLowerCase().equals("label")) {
                            featureName = note.getValue();

                            continue;
                        } else if (note.getTerm().getName().toLowerCase().equals("apeinfo_label")) { // ApE only
                            featureName = note.getValue();

                            continue;
                        }

                        notesMap.put(note.getTerm().getName(), note.getValue());
                    }

                    RichLocation featureLocation = (RichLocation) richFeature.getLocation();
                    String genbankType = richFeature.getType();
                    int start = featureLocation.getMin() - 1;
                    int end = featureLocation.getMax() - 1;

                    String dnaSequence = sequence.getSequence();

                    if (start < 0) {
                        start = 0;
                    } else if (start > dnaSequence.length() - 1) {
                        start = dnaSequence.length() - 1;
                    }

                    if (end < 0) {
                        end = 0;
                    } else if (end > dnaSequence.length() - 1) {
                        end = dnaSequence.length() - 1;
                    }

                    String featureDNASequence = "";

                    if (start > end) { // over zero case
                        featureDNASequence = dnaSequence.substring(start, dnaSequence.length() - 1);
                        featureDNASequence += dnaSequence.substring(0, end);
                    } else { // normal
                        featureDNASequence = sequence.getSequence().substring(start, end);
                    }

                    DNAFeature dnaFeature = new DNAFeature(start + 1, end + 1, genbankType,
                            featureName, featureLocation.getStrand().intValue(), notesMap);

                    dnaFeatures.add(dnaFeature);
                }
            }
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couldn't parse GenBank sequence!", e);
        }

        return sequence;
    }
}
