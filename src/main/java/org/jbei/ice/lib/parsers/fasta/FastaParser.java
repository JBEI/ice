package org.jbei.ice.lib.parsers.fasta;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedList;

/**
 * Parse FASTA files.
 *
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */
public class FastaParser extends AbstractParser {

    @Override
    public FeaturedDNASequence parse(InputStream stream, String... entryType) throws InvalidFormatParserException {
        try {
            String textSequence = Utils.getString(stream);
            textSequence = cleanSequence(textSequence);
            textSequence = textSequence.replaceAll("\t", "\n");
            try (BufferedReader br = new BufferedReader(new StringReader(textSequence))) {
                FeaturedDNASequence sequence;
                RichSequenceIterator richSequences;
                if (entryType.length > 0 && entryType[0].equals("protein")) {
                    richSequences = IOTools.readFastaProtein(br, null);
                } else {
                    richSequences = IOTools.readFastaDNA(br, null);
                }

                if (richSequences.hasNext()) {
                    RichSequence richSequence = richSequences.nextRichSequence();
                    sequence = new FeaturedDNASequence(richSequence.seqString(), new LinkedList<>());
                } else {
                    throw new InvalidFormatParserException("No sequence found in sequence file!");
                }
                return sequence;
            }
        } catch (BioException | IOException e) {
            throw new InvalidFormatParserException("Couldn't parse FASTA sequence!", e);
        }
    }
}
