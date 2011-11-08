package org.jbei.ice.lib.parsers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;

/**
 * Parse genbank style file with a non-standard LOCUS line.
 * 
 * @author Zinovii Dmytriv
 * 
 */
@Deprecated
public class GenbankLocusFriendlyParser extends GenbankParser {
    private static final String LOCUS_FRIENDLY_GenBank_PARSER = "GenBank-NoLocus";

    @Override
    public String getName() {
        return LOCUS_FRIENDLY_GenBank_PARSER;
    }

    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        if (textSequence == null || textSequence.isEmpty()) {
            return null;
        }

        textSequence = cleanSequence(textSequence);

        IDNASequence dnaSequence = null;

        try {
            dnaSequence = super.parse(textSequence);
        } catch (InvalidFormatParserException e) {
            // it's ok, will parse it again with adjusted LOCUS
        }

        if (dnaSequence != null) {
            return dnaSequence;
        }

        List<String> strings = Arrays.asList(textSequence.split("\n"));

        Pattern locusLineStartPattern = Pattern.compile("^LOCUS(.*)");

        String locusLine = null;
        int locusLineIndex = -1;
        for (int i = 0; i < strings.size(); i++) {
            String currentLine = strings.get(i);

            Matcher locusLineMatch = locusLineStartPattern.matcher(currentLine);
            if (locusLineMatch.matches()) {
                locusLine = currentLine; // found Locus line
                locusLineIndex = i;

                break;
            }
        }

        if (locusLine == null) {
            throw new InvalidFormatParserException("Failed to find LOCUS field!");
        }

        String newLocus = adjustLocusFormat(locusLine);

        if (newLocus == null || newLocus.isEmpty()) {
            throw new InvalidFormatParserException("Failed to parse LOCUS field!");
        }

        strings.set(locusLineIndex, newLocus);

        String newTextSequence = Utils.join("\n", strings);

        return super.parse(newTextSequence);
    }

    private String adjustLocusFormat(String locusLine) {
        // generate random locus so real genbank parser can parse it
        return "LOCUS       Unknown                111 bp    DNA     linear   CON 25-MAY-2007";
    }
}
