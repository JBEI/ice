package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.parsers.abi.ABITrace;
import org.jbei.ice.lib.vo.DNASequence;

import org.biojava.bio.symbol.SymbolList;

/**
 * Parse ABI sequence trace file by wrappying BioJava.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class ABIParser extends AbstractParser {

    private static final String ABI_PARSER = "ABI";

    @Override
    public String getName() {
        return ABI_PARSER;
    }

    @Override
    public Boolean hasErrors() {
        // This parser cannot succeed with errors, so always return false, or fail.
        return false;
    }

    @Override
    public DNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        DNASequence DNASequence = null;

        try {
            ABITrace abiTrace = new ABITrace(bytes);
            SymbolList symbolList = abiTrace.getSequence();
            if (symbolList != null) {
                DNASequence = new DNASequence(symbolList.seqString().toLowerCase());
            }
        } catch (Exception e) {
            throw new InvalidFormatParserException(e);
        }

        return DNASequence;
    }

    @Override
    public DNASequence parse(String textSequence) throws InvalidFormatParserException {
        throw new NoSuchMethodError("ABI file can't be presented as string!");
    }
}
