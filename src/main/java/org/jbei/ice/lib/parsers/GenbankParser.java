package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Note;
import org.biojavax.RankedCrossRef;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

public class GenbankParser extends AbstractParser {
    private static final String GENBANK_PARSER = "GenBank";

    @Override
    public String getName() {
        return GENBANK_PARSER;
    }

    /**
     * This parser cannot succeed with errors, so always return false, or fail.
     */
    @Override
    public Boolean hasErrors() {
        return false;
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

                    String genbankType = richFeature.getType();

                    String featureName = "";

                    DNAFeature dnaFeature = new DNAFeature();

                    Set<Note> notes = richFeature.getNoteSet();
                    for (Note note : notes) {
                        if (note.getTerm().getName().toLowerCase().equals("name")) {
                            featureName = cleanFeatureValue(note.getValue());

                            dnaFeature.addNote(new DNAFeatureNote("name", featureName));

                            continue;
                        } else if (note.getTerm().getName().toLowerCase().equals("label")) {
                            featureName = cleanFeatureValue(note.getValue());

                            continue;
                        } else if (note.getTerm().getName().toLowerCase().equals("apeinfo_label")) { // ApE only
                            featureName = cleanFeatureValue(note.getValue());

                            continue;
                        }

                        dnaFeature.addNote(new DNAFeatureNote(note.getTerm().getName(),
                                cleanFeatureValue(note.getValue())));
                    }

                    // special case for source feature; it stores organism info and db_xref in richSequence
                    if (genbankType.equals("source") && richSequence.getTaxon() != null) {
                        if (richSequence.getTaxon().getDisplayName() != null
                                && !richSequence.getTaxon().getDisplayName().isEmpty()) {

                            dnaFeature.addNote(new DNAFeatureNote("organism", richSequence
                                    .getTaxon().getDisplayName()));
                        }

                        if (richSequence.getTaxon().getNCBITaxID() > 0) {
                            dnaFeature.addNote(new DNAFeatureNote("db_xref",
                                    cleanFeatureValue("taxon:"
                                            + richSequence.getTaxon().getNCBITaxID())));
                        }
                    }

                    if (richFeature.getRankedCrossRefs() != null
                            && richFeature.getRankedCrossRefs().size() > 0) {
                        for (Object object : richFeature.getRankedCrossRefs()) {
                            RankedCrossRef rankedCrossRef = (RankedCrossRef) object;

                            dnaFeature.addNote(new DNAFeatureNote("db_xref",
                                    cleanFeatureValue(rankedCrossRef.getCrossRef().toString())));
                        }
                    }

                    RichLocation featureLocation = (RichLocation) richFeature.getLocation();
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

                    int strand = featureLocation.getStrand().intValue();

                    dnaFeature.getLocations().add(new DNAFeatureLocation(start + 1, end + 1));
                    dnaFeature.setStrand(strand);
                    dnaFeature.setType(genbankType);
                    dnaFeature.setName(featureName);

                    dnaFeatures.add(dnaFeature);
                }
            }
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couldn't parse GenBank sequence!", e);
        }

        return sequence;
    }

    private String cleanFeatureValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String result = value.trim();
        result = result.replace("&quot;", "\""); // remove &quot;
        result = result.replace("\\", " "); // remove VectorNTI slashes
        result = result.replaceAll("\\s+", " "); // remove double spaces
        result = result.trim(); // trim again

        return result;
    }
}
