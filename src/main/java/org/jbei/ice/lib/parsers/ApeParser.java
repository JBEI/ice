package org.jbei.ice.lib.parsers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;

public class ApeParser extends GenbankParser {
    private static final String APE_PARSER = "ApE";

    @Override
    public String getName() {
        return APE_PARSER;
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

        List<String> strings = (List<String>) Arrays.asList(textSequence.split("\n"));

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

        String newLocus = adjustOldApeLocusFormat(locusLine);

        if (newLocus == null || newLocus.isEmpty()) {
            throw new InvalidFormatParserException("Failed to parse LOCUS field!");
        }

        strings.set(locusLineIndex, newLocus);

        String newTextSequence = Utils.join("\n", strings);

        return super.parse(newTextSequence);
    }

    private String adjustOldApeLocusFormat(String locusLine) {
        // Old ApE file format misses sequence name, so it's replaced by UNKNOWN
        String result = null;

        Pattern oldApeLocusLinePattern = Pattern
                .compile("^LOCUS\\s+\\d+\\s+(bp|aa)\\s{1,4}([dms]s-)?(\\S+)?\\s+(circular|linear)?\\s*(\\S+)?\\s*(\\S+)?$");

        Matcher locusLineMatch = oldApeLocusLinePattern.matcher(locusLine);
        if (locusLineMatch.matches()) {
            result = locusLine.replace("LOCUS", "LOCUS       uknown");
        }

        return result;
    }
}
