package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum BlastProgram implements IsSerializable {

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

    public static BlastProgram filterValueOf(String value) {
        try {
            return BlastProgram.valueOf(value);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
