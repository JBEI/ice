package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.parsers.fasta.FastaParser;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Uses a heuristic to detect the type of sequence, based on the first few xters in the file
 * <br>
 * This assumes a single sequence so multi-fasta files are not supported
 *
 * @author Hector Plahar
 */
public class GeneralParser {

    public static final String GENBANK_DELIMITER = "//";
    public static final String FASTA_DELIMITER = ">";

    public static DNASequence parse(String sequence) {

        AbstractParser parser;
        try {
            SequenceFormat format = detectFormat(sequence);
            if (format == null) {
                parser = new PlainParser();
            } else {
                switch (format) {
                    case GENBANK:
                        parser = new GenBankParser();
                        break;

                    case FASTA:
                    default:
                        parser = new FastaParser();
                        break;
                }
            }
            return parser.parse(sequence);
        } catch (IOException | InvalidFormatParserException e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Attempts to detect the sequence format from the first line of a stream using the following heuristics
     * <ul>
     * <li>If line starts with <code>LOCUS</code> then assumed to be a genbank file</li>
     * <li>If line starts with <code>></code> then assumed to be a fasta file</li>
     * <li>If line starts with <code><</code> then assumed to be an sbol file</li>
     * <li>Anything else is assumed to be plain nucleotides</li>
     * </ul>
     *
     * @param sequenceString input sequence
     * @return detected format
     * @throws IOException
     */
    protected static SequenceFormat detectFormat(String sequenceString) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(sequenceString));
        String line = reader.readLine();
        if (line == null)
            throw new IOException("Could not obtain line from document");

        if (line.startsWith("LOCUS"))
            return SequenceFormat.GENBANK;

        if (line.startsWith(">"))
            return SequenceFormat.FASTA;

        return null;
    }
}
