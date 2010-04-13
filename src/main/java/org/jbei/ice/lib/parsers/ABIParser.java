package org.jbei.ice.lib.parsers;

import org.apache.commons.lang.NotImplementedException;
import org.biojava.bio.program.abi.ABITrace;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.SimpleDNASequence;

public class ABIParser extends AbstractParser {
    private static final String ABI_PARSER = "ABI";

    @Override
    public String getName() {
        return ABI_PARSER;
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        SimpleDNASequence simpleDNASequence = null;

        try {
            ABITrace abiTrace = new ABITrace(bytes);

            SymbolList symbolList = abiTrace.getSequence();

            if (symbolList != null) {
                simpleDNASequence = new SimpleDNASequence(symbolList.seqString().toLowerCase());
            }
        } catch (Exception e) {
            throw new InvalidFormatParserException(e);
        }

        return simpleDNASequence;
    }

    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        throw new NotImplementedException("ABI file can't be presented as string!");
    }
}
