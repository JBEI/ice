package org.jbei.ice.lib.parsers;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.dto.DNASequence;

/**
 * Parser to handle file with simply nucleotide sequences. Technically these files are not FASTA
 * files, even though some people think they are.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class PlainParser extends AbstractParser {

    @Override
    public DNASequence parse(String textSequence) throws InvalidFormatParserException {
        textSequence = cleanSequence(textSequence);

        SymbolList sl;
        try {
            sl = new SimpleSymbolList(DNATools.getDNA().getTokenization("token"), textSequence
                    .replaceAll("\\s+", "").replaceAll("[\\.|~]", "-").replaceAll("[0-9]", ""));
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couldn't parse Plain sequence!", e);
        }
        return new DNASequence(sl.seqString());
    }
}
