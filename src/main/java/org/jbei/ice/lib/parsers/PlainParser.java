package org.jbei.ice.lib.parsers;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.dto.FeaturedDNASequence;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parser to handle file with simply nucleotide sequences. Technically these files are not FASTA
 * files, even though some people think they are.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class PlainParser extends AbstractParser {

    @Override
    public FeaturedDNASequence parse(Iterator<String> iterator, String... entryType) throws InvalidFormatParserException {
        SymbolList sl;
        try {
            String textSequence = getSequence(iterator);
            textSequence = cleanSequence(textSequence);
            sl = new SimpleSymbolList(DNATools.getDNA().getTokenization("token"), textSequence
                    .replaceAll("\\s+", "").replaceAll("[\\.|~]", "-").replaceAll("[0-9]", ""));
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couldn't parse Plain sequence!", e);
        }
        return new FeaturedDNASequence(sl.seqString(), new ArrayList<>());
    }
}
