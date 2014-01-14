package org.jbei.ice.lib.vo;

import java.io.Serializable;

/**
 * Value object to hold DNA sequence.
 *
 * @author Hector Plahar
 */
public class DNASequence implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sequence = "";

    public DNASequence() {
    }

    public DNASequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
