package org.jbei.ice.shared.dto.search;

import org.jbei.ice.shared.dto.IDTOModel;

public enum BlastProgram implements IDTOModel {

    BLAST_N("blastn", "Nucleotide Search (blastn)"),
    TBLAST_X("tblastx", "Nucleotide Translated Search (tblastx)");

    private String name;
    private String details;

    BlastProgram(String name, String details) {
        this.name = name;
        this.details = details;
    }

    private BlastProgram() {
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
            for (BlastProgram program : BlastProgram.values()) {
                if (program.getName().equalsIgnoreCase(value))
                    return program;
            }
            return null;
        }
    }
}
