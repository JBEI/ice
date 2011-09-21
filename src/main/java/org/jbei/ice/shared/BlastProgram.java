package org.jbei.ice.shared;

import java.io.Serializable;

public enum BlastProgram implements Serializable {

    BLAST_N("blastn", "nucleotide search"), TBLAST_X("tblastx", "translated search");

    private String name;
    private String details;

    BlastProgram(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public String getName() {
        return this.name;
    }

    public String getDetails() {
        return this.details;
    }
}
