package org.jbei.ice.shared;

public enum BlastOption {

    BLAST_N("blastn (nucleotide search)"), BLAST_X("tblastx (translated search)");

    private String display;

    BlastOption(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }

}
