package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.dto.DNASequence;

/**
 * An object that parser sequences and generates an annotated {@link DNASequence} object.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public interface IDNAParser {

    /**
     * Parse the given bytes to {@link DNASequence} annotated sequence.
     *
     * @param bytes
     * @return Annotated sequence.
     * @throws InvalidFormatParserException
     */
    DNASequence parse(byte[] bytes) throws InvalidFormatParserException;

    /**
     * Parse the given string to {@link DNASequence} annotated sequence.
     *
     * @param textSequence
     * @return parsed DNASequence.
     * @throws InvalidFormatParserException
     */
    DNASequence parse(String textSequence) throws InvalidFormatParserException;
}
