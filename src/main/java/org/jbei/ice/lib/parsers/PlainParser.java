package org.jbei.ice.lib.parsers;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.SimpleDNASequence;

public class PlainParser extends AbstractParser {
    private static final String PLAIN_PARSER = "Plain";

    /**
     * This parser cannot succeed with errors, so always return false, or fail.
     */
    @Override
    public Boolean hasErrors() {
        return false;
    }

    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        textSequence = cleanSequence(textSequence);

        SymbolList sl = null;
        try {
            sl = new SimpleSymbolList(DNATools.getDNA().getTokenization("token"), textSequence
                    .replaceAll("\\s+", "").replaceAll("[\\.|~]", "-").replaceAll("[0-9]", ""));
        } catch (IllegalSymbolException e) {
            throw new InvalidFormatParserException("Couldn't parse Plain sequence!", e);
        } catch (BioException e) {
            throw new InvalidFormatParserException("Couldn't parse Plain sequence!", e);
        }
        return new SimpleDNASequence(sl.seqString());
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return null;
    }

    @Override
    public String getName() {
        return PLAIN_PARSER;
    }
}
