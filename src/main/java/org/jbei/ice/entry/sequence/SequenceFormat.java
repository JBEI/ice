package org.jbei.ice.entry.sequence;

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
    PLAIN,
    GFF3,
    GENBANK;

    public static SequenceFormat fromString(String type) {
        return SequenceFormat.valueOf(type.toUpperCase());
    }
}
