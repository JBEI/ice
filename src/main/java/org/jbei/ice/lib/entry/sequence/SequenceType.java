package org.jbei.ice.lib.entry.sequence;

/**
 * Type of sequence which will be used to determine how it is treated (e.g. whether it can be visualized etc)
 *
 * @author Hector Plahar
 */
public enum SequenceType {
    NO_SEQUENCE,        // no sequence available
    SBOL_DOWNLOAD,      // sbol sequence format
    CAN_VISUALIZE,      // sequence of size that can be visualized
    GENOME_SCALE        // genome scale sequence. visualization is not advised
}
