package org.jbei.ice.lib.entry.sequence;

/**
 * Different sequence formats supported by ICE
 *
 * @author Hector Plahar
 */
public enum SequenceFormat {
    ORIGINAL,
    FASTA,
    SBOL1,
    SBOL2,
    GENBANK;

    public static SequenceFormat fromString(String type) {
        return SequenceFormat.valueOf(type.toUpperCase());
    }
}