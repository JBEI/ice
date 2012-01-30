package org.jbei.ice.lib.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbei.ice.lib.vo.IDNASequence;

/**
 * An object that parser sequences and generates an annotated {@link IDNASequence} object.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public interface IDNAParser {
    /**
     * Return the name of the parser.
     * 
     * @return Name of parser.
     */
    String getName();

    /**
     * Return true if parsing was completed, but there maybe errors in the parsing.
     * 
     * @return True if parsing has errors.
     */
    Boolean hasErrors();

    /**
     * Parse the given bytes to {@link IDNASequence} annotated sequence.
     * 
     * @param bytes
     * @return Annotated sequence.
     * @throws InvalidFormatParserException
     */
    IDNASequence parse(byte[] bytes) throws InvalidFormatParserException;

    /**
     * Parse the given file to {@link IDNASequence} annotated sequence.
     * 
     * @param file
     *            File to parse.
     * @return Annotated sequence.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InvalidFormatParserException
     */
    IDNASequence parse(File file) throws FileNotFoundException, IOException,
            InvalidFormatParserException;

    /**
     * Parse the given string to {@link IDNASequence} annotated sequence.
     * 
     * @param textSequence
     * @return parsed IDNASequence.
     * @throws InvalidFormatParserException
     */
    IDNASequence parse(String textSequence) throws InvalidFormatParserException;
}
