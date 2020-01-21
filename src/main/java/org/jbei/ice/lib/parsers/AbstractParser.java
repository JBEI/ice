package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.dto.FeaturedDNASequence;

import java.io.InputStream;

public abstract class AbstractParser {

    protected String fileName;

    public FeaturedDNASequence parse(InputStream stream, String... entryType) throws InvalidFormatParserException {
        throw new UnsupportedOperationException("Not implemented for this parser");
    }

    /**
     * Replace different line termination characters with the newline character (\n).
     *
     * @param sequence Text to clean.
     * @return String with only newline character (\n).
     */
    protected String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replace("\n\n", "\n"); // *nix
        sequence = sequence.replace("\n\r\n\r", "\n"); // win
        sequence = sequence.replace("\r\r", "\n"); // mac
        sequence = sequence.replace("\n\r", "\n"); // *win
        return sequence;
    }
}
