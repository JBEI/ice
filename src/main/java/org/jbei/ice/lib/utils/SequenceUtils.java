package org.jbei.ice.lib.utils;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

public class SequenceUtils {
    public static String calculateSequenceHash(String sequence) {
        return Utils.encryptSHA(sequence.toLowerCase());
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

    public static String translateToProtein(String dnaSequence) throws Exception {
        SymbolList symL = null;
        try {
            symL = DNATools.createDNA(dnaSequence);
            symL = DNATools.toProtein(symL);

        } catch (BioException e) {
            throw e;
        }
        return symL.seqString();
    }
}
