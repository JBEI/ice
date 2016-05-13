package org.jbei.ice.lib.parsers.fasta;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;

/**
 * Parse FASTA files.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class FastaParser extends AbstractParser {

    @Override
    public FeaturedDNASequence parse(String textSequence) throws InvalidFormatParserException {
        textSequence = cleanSequence(textSequence);

        BufferedReader br = new BufferedReader(new StringReader(textSequence));
        FeaturedDNASequence sequence;

        try {
            RichSequenceIterator richSequences = IOTools.readFastaDNA(br, null);

            if (richSequences.hasNext()) {
                RichSequence richSequence = richSequences.nextRichSequence();

                sequence = new FeaturedDNASequence(richSequence.seqString(), new LinkedList<>());
            } else {
                throw new InvalidFormatParserException("No sequence found in sequence file!");
            }
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couln't parse FASTA sequence!", e);
        }

        return sequence;
    }
}
