package org.jbei.ice.parsers;

import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.dto.DNASequence;
import org.jbei.ice.parsers.abi.ABITrace;

/**
 * Parse ABI sequence trace file by wrapping BioJava.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class ABIParser {

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
}
