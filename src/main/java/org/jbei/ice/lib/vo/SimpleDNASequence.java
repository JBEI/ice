package org.jbei.ice.lib.vo;

public class SimpleDNASequence implements IDNASequence {
    private static final long serialVersionUID = 1L;

    private String sequence;

    @Override
    public String getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
