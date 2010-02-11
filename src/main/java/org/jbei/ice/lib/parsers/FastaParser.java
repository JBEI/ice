package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.SequenceUtils;

public class FastaParser extends AbstractParser {
    @Override
    public Sequence parse(BufferedReader br) {
        Sequence sequence = new Sequence();

        try {
            RichSequenceIterator richSequences = IOTools.readFastaDNA(br, null);

            if (richSequences.hasNext()) {
                RichSequence richSequence = richSequences.nextRichSequence();

                sequence = new Sequence(richSequence.seqString(), "", "", "", null, null);
            }

            sequence.setFwdHash(SequenceUtils.calculateSequenceHash(sequence.getSequence()));
            sequence.setRevHash(SequenceUtils.calculateSequenceHash(SequenceUtils
                    .reverseComplement(sequence.getSequence())));
        } catch (BioException e) {
            return null;
        }

        return sequence;
    }
}
