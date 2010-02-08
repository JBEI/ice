package org.jbei.ice.lib.utils;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

public class SequenceUtils {
    public static String calculateSequenceHash(String sequence) {
        return Utils.encryptSHA(sequence.toLowerCase());
    }

    public static String reverseComplement(String sequence) throws BioException {
        SymbolList symL = null;

        symL = DNATools.createDNA(sequence);
        symL = DNATools.reverseComplement(symL);

        return symL.seqString();
    }

    public static String translateToProtein(String dnaSequence) throws BioException {
        SymbolList symL = null;

        symL = DNATools.createDNA(dnaSequence);
        symL = DNATools.toProtein(symL);

        return symL.seqString();
    }
}
