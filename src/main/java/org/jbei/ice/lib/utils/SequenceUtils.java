package org.jbei.ice.lib.utils;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

/**
 * Utility methods for sequences.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class SequenceUtils {
    /**
     * Calculate the SHA-1 sequence hash of the given sequence.
     * <p/>
     * Normalize by trimming and converting to lower case. Does not verify valid symbol.
     *
     * @param sequence sequence to hash.
     * @return Hex digest of SHA-1 hash.
     */
    public static String calculateSequenceHash(String sequence) {
        return Utils.encryptSHA(sequence.trim().toLowerCase());
    }

    /**
     * Calculate the SHA-1 sequence hash of the reverse complement of the given sequence.
     * <p/>
     * Normalize by trimming and converting to lower case. Does not verify valid symbol.
     *
     * @param sequence sequence to hash.
     * @return Hex digest of SHA-1 hash.
     * @throws UtilityException
     */
    public static String calculateReverseComplementSequenceHash(String sequence)
            throws UtilityException {
        return calculateSequenceHash(reverseComplement(sequence));
    }

    /**
     * Calculate the reverse complement of the given DNA sequence.
     *
     * @param sequence DNA sequence to reverse complement.
     * @return Reversed, complemented sequence.
     * @throws UtilityException
     */
    public static String reverseComplement(String sequence) throws UtilityException {
        SymbolList symL;

        try {
            symL = DNATools.createDNA(sequence);
            symL = DNATools.reverseComplement(symL);
        } catch (IllegalSymbolException | IllegalAlphabetException e) {
            throw new UtilityException(e);
        }

        return symL.seqString();
    }

    /**
     * Calculate the amino acid translation of the given dnaSequence string.
     *
     * @param dnaSequence DNA sequence to translate.
     * @return String of amino acid symbols.
     * @throws UtilityException
     */
    public static String translateToProtein(String dnaSequence) throws UtilityException {
        SymbolList symL = null;

        try {
            symL = DNATools.createDNA(dnaSequence);
            symL = DNATools.toProtein(symL);
        } catch (IllegalSymbolException | IllegalAlphabetException e) {
            throw new UtilityException(e);
        }

        return symL.seqString();
    }

    /**
     * Format into 6 column, 10 basepairs per column display.
     *
     * @param input sequence string.
     * @return Formatted sequence output.
     */
    public static String breakUpLines(String input) {
        StringBuilder result = new StringBuilder();

        int counter = 0;
        int index = 0;
        int end = input.length();
        while (index < end) {
            result = result.append(input.substring(index, index + 1));
            counter = counter + 1;
            index = index + 1;

            if (counter == 59) {
                result = result.append("\n");
                counter = 0;
            }
        }
        return result.toString();
    }
}
