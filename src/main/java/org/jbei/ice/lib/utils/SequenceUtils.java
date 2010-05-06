package org.jbei.ice.lib.utils;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

public class SequenceUtils {
    public static String calculateSequenceHash(String sequence) {
        return Utils.encryptSHA(sequence.toLowerCase());
    }

    public static String calculateReverseComplementSequenceHash(String sequence) {
        return calculateSequenceHash(reverseComplement(sequence));
    }

    public static String reverseComplement(String sequence) {
        SymbolList symL = null;

        try {
            symL = DNATools.createDNA(sequence);
            symL = DNATools.reverseComplement(symL);
        } catch (IllegalSymbolException e) {
            e.printStackTrace(); // TODO: Zinovii; Handle this properly
        } catch (IllegalAlphabetException e) {
            e.printStackTrace(); // TODO: Zinovii; Handle this properly
        }

        return symL.seqString();
    }

    public static String translateToProtein(String dnaSequence) {
        SymbolList symL = null;

        try {
            symL = DNATools.createDNA(dnaSequence);
            symL = DNATools.toProtein(symL);
        } catch (IllegalSymbolException e) {
            e.printStackTrace(); // TODO: Zinovii; Handle this properly
        } catch (IllegalAlphabetException e) {
            e.printStackTrace(); // TODO: Zinovii; Handle this properly
        }

        return symL.seqString();
    }
}
