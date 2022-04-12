package org.jbei.ice.lib.parsers;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.entry.sequence.SequenceUtil;
import org.jbei.ice.lib.parsers.fasta.FastaParser;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;

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
