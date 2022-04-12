package org.jbei.ice.parsers;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.dto.FeaturedDNASequence;
import org.jbei.ice.entry.sequence.SequenceFormat;
import org.jbei.ice.entry.sequence.SequenceUtil;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.parsers.fasta.FastaParser;
import org.jbei.ice.parsers.genbank.GenBankParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Uses a heuristic to detect the type of sequence, based on the first few xters in the file
 * <br>
 * This assumes a single sequence so multi-fasta files are not supported
 *
 * @author Hector Plahar
 */
public class GeneralParser {

    public static FeaturedDNASequence parse(String sequence) {

        AbstractParser parser;
        try {
            SequenceFormat format = SequenceUtil.detectFormat(sequence);
            switch (format) {
                case PLAIN:
                    parser = new PlainParser();
                    break;

                case GENBANK:
                    parser = new GenBankParser();
                    break;

                case FASTA:
                default:
                    parser = new FastaParser();
                    break;
            }
            return parser.parse(IOUtils.lineIterator(new ByteArrayInputStream(sequence.getBytes()), Charset.defaultCharset()));
        } catch (IOException | InvalidFormatParserException e) {
            Logger.error(e);
            return null;
        }
    }
}
