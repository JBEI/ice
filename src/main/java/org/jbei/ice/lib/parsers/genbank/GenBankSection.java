package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.FeaturedDNASequence;

/**
 * Parent class for a section of a GenBank document
 *
 * @author Hector Plahar
 */
public abstract class GenBankSection {

    protected FeaturedDNASequence sequence;

    public GenBankSection(FeaturedDNASequence sequence) {
        this.sequence = sequence;
    }

    public abstract void process(String line);

    public FeaturedDNASequence getSequence() {
        return this.sequence;
    }

    /**
     * Replace different line termination characters with the newline character (\n).
     *
     * @param sequence Text to clean.
     * @return String with only newline character (\n).
     */
    protected String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replace("\n\n", "\n");      // *nix
        sequence = sequence.replace("\n\r\n\r", "\n");  // win
        sequence = sequence.replace("\r\r", "\n");      // mac
        sequence = sequence.replace("\n\r", "\n");      // *win
        return sequence;
    }
}
