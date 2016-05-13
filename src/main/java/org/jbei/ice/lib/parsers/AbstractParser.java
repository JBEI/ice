package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.dto.DNASequence;

/**
 * This class provides skeletal implementation of {@link IDNAParser} interface.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public abstract class AbstractParser implements IDNAParser {

    @Override
    public abstract DNASequence parse(String textSequence) throws InvalidFormatParserException;

    @Override
    public DNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
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
