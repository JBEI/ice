package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

public class FastaParser extends AbstractParser {
    private static final String FASTA_PARSER = "FASTA";

    @Override
    public FeaturedDNASequence parse(String textSequence) throws InvalidFormatParserException {
        BufferedReader br = new BufferedReader(new StringReader(textSequence));
        FeaturedDNASequence sequence = null;

        try {
            RichSequenceIterator richSequences = IOTools.readFastaDNA(br, null);

            if (richSequences.hasNext()) {
                RichSequence richSequence = richSequences.nextRichSequence();

                sequence = new FeaturedDNASequence(richSequence.seqString(),
                        new LinkedList<DNAFeature>());
            } else {
                throw new InvalidFormatParserException("None sequence found in sequence file!");
            }
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couln't parse FASTA sequence!", e);
        }

        return sequence;
    }

    @Override
    public String getName() {
        return FASTA_PARSER;
    }
}
