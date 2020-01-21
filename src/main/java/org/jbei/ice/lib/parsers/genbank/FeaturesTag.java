package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.DNAFeatureNote;
import org.jbei.ice.lib.dto.FeaturedDNASequence;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hector Plahar
 */
public class FeaturesTag extends Tag {

    private static final Pattern startStopPattern = Pattern.compile("[<>]*(\\d+)\\.\\.[<>]*(\\d+)");
    private static final Pattern startOnlyPattern = Pattern.compile("\\d+");
    private DNAFeature currentFeature;
    private DNAFeatureNote currentNote;

    public FeaturesTag(FeaturedDNASequence sequence) {
        super(sequence);
        currentFeature = null;
    }

    /**
     * Expecting a format as follows
     * <code>
     * type   startBP..stopBP
     * </code>
     *
     * @param line line to process
     * @return true if a feature start conforming to above was detected, false otherwise
     */
    private boolean detectFeatureStart(String line) {
        if (!line.contains(".."))
            return false;

        if (line.endsWith("\"") && !line.startsWith("\""))
            return false;

        String[] chunks = line.split("\\s+");
        if (chunks.length < 2)
            return false;

        // get location string
        String locationString = chunks[1].trim();
        boolean reversedLocations = locationString.startsWith("complement(join");

        boolean complement = false;
        if (locationString.startsWith("complement")) {
            complement = true;
            locationString = locationString.trim();
            locationString = locationString.substring(11, locationString.length() - 1).trim();
        }

        parseGenbankLocation(locationString);
        if (reversedLocations) {
            Collections.reverse(currentFeature.getLocations());
        }

        currentFeature.setType(chunks[0].trim());   // todo : type must be empty
        currentFeature.setStrand(complement ? -1 : 1);
        return true;
    }

    @Override
    public void process(String line) {
        line = line.trim();

        // check for first line
        if (line.startsWith("FEATURES")) {
            // todo : check first line should be "FEATURES....Location/Qualifiers
            return;
        }

        // check if we are starting a new qualifier line
        // e.g. /note="abc"
        boolean isQualifier = (line.startsWith("/") && line.contains("="));
        if (isQualifier) {
            parseQualifierLine(line);
            return;
        }

        // check if feature start
        if (!detectFeatureStart(line)) {
            // check for multi-qualifier
            if (!checkMultiQualifier(line)) {
                Logger.error("Don't know what to do with line " + line);
            }
        }
    }

    private void parseGenbankLocation(String input) {
        currentFeature = new DNAFeature();
        sequence.getFeatures().add(currentFeature);

        int genbankStart, end;

        if (input.startsWith("join")) {
            input = input.substring(5, input.length() - 1).trim();
        }

        final String[] chunks = input.split(",");
        for (String chunk : chunks) {
            chunk = chunk.trim();
            final Matcher startStopMatcher = startStopPattern.matcher(chunk);
            if (startStopMatcher.find()) {
                if (startStopMatcher.groupCount() == 2) {
                    genbankStart = Integer.parseInt(startStopMatcher.group(1));
                    end = Integer.parseInt(startStopMatcher.group(2));
                    currentFeature.getLocations().add(new DNAFeatureLocation(genbankStart, end));
                }
            } else {
                final Matcher startOnlyMatcher = startOnlyPattern.matcher(chunk);
                if (startOnlyMatcher.find()) {
                    genbankStart = Integer.parseInt(startOnlyMatcher.group(0));
                    end = Integer.parseInt(startOnlyMatcher.group(0));
                    currentFeature.getLocations().add(new DNAFeatureLocation(genbankStart, end));
                }
            }
        }
    }

    private boolean checkMultiQualifier(String line) {
        if (currentNote == null)
            return false;

        line = line.replaceAll("\\\\", " ");
        line = line.replaceAll("\"\"", "\"");

        currentNote.setValue(currentNote.getValue() + line);
        return true;
    }

    // /label = SEC13 or
    // /locus_tag="PAS_cacsr"
    private void parseQualifierLine(String line) {
        String[] chunks = line.split("=");
        if (chunks.length < 2)
            return;

        // starting a new note
        currentNote = new DNAFeatureNote();
        currentFeature.getNotes().add(currentNote);

        // set name
        final String putativeName = chunks[0].trim().substring(1);
        currentNote.setName(putativeName);

        // set value
        String value = chunks[1].trim();
        if (value.startsWith("\"")) {
            value = value.substring(1);
            currentNote.setQuoted(true);
        }

        if (value.endsWith("\""))
            value = value.substring(0, value.length() - 1);

        value = value.replaceAll("\\\\", " ");
        value = value.replaceAll("\"\"", "\"");

        currentNote.setValue(value);
        determineFeatureName();
    }

    /**
     * Tries to determine the feature name, from a list of possible qualifier keywords that might
     * contain it.
     */
    private void determineFeatureName() {
        final String LABEL_QUALIFIER = "label";
        final String APE_LABEL_QUALIFIER = "apeinfo_label";
        final String NOTE_QUALIFIER = "note";
        final String GENE_QUALIFIER = "gene";
        final String ORGANISM_QUALIFIER = "organism";
        final String NAME_QUALIFIER = "name";

        final List<DNAFeatureNote> notes = currentFeature.getNotes();
        final String[] QUALIFIERS = {APE_LABEL_QUALIFIER, NOTE_QUALIFIER, GENE_QUALIFIER,
                ORGANISM_QUALIFIER, NAME_QUALIFIER};
        String newLabel = null;

        if (dnaFeatureContains(notes, LABEL_QUALIFIER) == -1) {
            for (final String element : QUALIFIERS) {
                final int foundId = dnaFeatureContains(notes, element);
                if (foundId != -1) {
                    newLabel = notes.get(foundId).getValue();
                }
            }
            if (newLabel == null) {
                newLabel = currentFeature.getType();
            }
        } else {
            newLabel = notes.get(dnaFeatureContains(notes, LABEL_QUALIFIER)).getValue();
        }

        currentFeature.setName(newLabel);
    }

    private int dnaFeatureContains(final List<DNAFeatureNote> notes, final String key) {
        int result = -1;
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getName().equals(key)) {
                result = i;
                return result;
            }
        }
        return result;
    }
}
