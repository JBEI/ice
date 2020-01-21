package org.jbei.ice.lib.parsers.genbank;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Genbank parser and generator. The Genbank file format is defined in gbrel.txt located at
 * ftp://ftp.ncbi.nlm.nih.gov/genbank/gbrel.txt
 * <p>
 * This parser also handles some incorrectly formatted and obsolete genbank files.
 *
 * @author Timothy Ham
 */
public class GenBankParser extends AbstractParser {

    private static final String END_TAG = "//";

    private List<String> errors = new ArrayList<>();
    private FeaturedDNASequence sequence;

    public GenBankParser() {
    }

    public List<String> getErrors() {
        return errors;
    }

    private String getFirstWordFromLine(String line) {
        if (StringUtils.isBlank(line))
            return "";

        String[] chunks = line.split("\\s+");
        if (chunks.length == 0)
            return "";

        return chunks[0];
    }

    private Tag process(GenbankTag genbankTag) {
        switch (genbankTag) {
            case LOCUS:
                return new LocusTag(sequence);

            case ORIGIN:
                return new OriginTag(sequence);

            case ACCESSION:
                return new Accession(sequence);

            case FEATURES:
                return new FeaturesTag(sequence);

            case REFERENCE:
                return new ReferenceTag(sequence);
        }
        return null;
    }

    @Override
    public FeaturedDNASequence parse(InputStream stream, String... entryType) throws InvalidFormatParserException {
        sequence = new FeaturedDNASequence();

        try (LineIterator iterator = IOUtils.lineIterator(stream, StandardCharsets.UTF_8)) {
            Tag currentTag = null;

            while (iterator.hasNext()) {
                String line = iterator.nextLine();
//                if (line.trim().equalsIgnoreCase(END_TAG)) // end tag ends the sequence
//                    break;

                String firstWord = getFirstWordFromLine(line);
                GenbankTag genbankTag = GenbankTag.getTagForString(firstWord);

                // encountered new tag
                if (genbankTag != null) {
                    currentTag = process(genbankTag);
                }

                if (currentTag != null)
                    currentTag.process(line);
            }

            return sequence;
        } catch (IOException e) {
            throw new InvalidFormatParserException(e);
        }
    }

    // TODO
    private void parseReferenceTag(String text) throws InvalidFormatParserException {

    }
}
