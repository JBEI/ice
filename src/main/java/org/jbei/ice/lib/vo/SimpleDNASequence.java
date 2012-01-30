package org.jbei.ice.lib.vo;

/**
 * Value object to hold DNA sequence.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class SimpleDNASequence implements IDNASequence {
    private static final long serialVersionUID = 1L;

    private String sequence = "";

    public SimpleDNASequence() {
    }

    public SimpleDNASequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
