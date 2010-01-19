package org.jbei.ice.lib.utils;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

public class SequenceUtils {
    public static String calculateSequenceHash(String sequence) {
        return Utils.encryptMD5(sequence);
    }

    public static String reverseComplement(String sequence) throws Exception {
        SymbolList symL = null;
        try {
            symL = DNATools.createDNA(sequence);
            symL = DNATools.reverseComplement(symL);
        } catch (BioException e) {
            throw new Exception(e);
        }

        return symL.seqString();
    }
}
