package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.StringReader;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.SequenceUtils;

public class FastaParser extends AbstractParser {
    private static final String FASTA_PARSER = "FASTA";

    @Override
    public Sequence parse(String textSequence) throws InvalidFormatParserException {
        BufferedReader br = new BufferedReader(new StringReader(textSequence));
        Sequence sequence = new Sequence();

        try {
            RichSequenceIterator richSequences = IOTools.readFastaDNA(br, null);

            if (richSequences.hasNext()) {
                RichSequence richSequence = richSequences.nextRichSequence();

                sequence = new Sequence(richSequence.seqString(), textSequence, "", "", null, null);
            }

            sequence.setFwdHash(SequenceUtils.calculateSequenceHash(sequence.getSequence()));
            sequence.setRevHash(SequenceUtils.calculateSequenceHash(SequenceUtils
                    .reverseComplement(sequence.getSequence())));
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
