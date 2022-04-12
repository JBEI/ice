package org.jbei.ice.entry.sequence;

/**
 * Different sequence formats supported by ICE
 *
 * @author Hector Plahar
 */
public enum SequenceFormat {

    ORIGINAL("txt"),
    FASTA(".fa"),
    SBOL1(".sbol"),
    SBOL2(".sbol"),
    PLAIN(".txt"),
    GFF3(".gff"),
    GENBANK(".gb");

    private final String extension;

    SequenceFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }

    public static SequenceFormat fromString(String type) {
        return SequenceFormat.valueOf(type.toUpperCase());
    }
}
