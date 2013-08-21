package org.jbei.ice.lib.shared.dto.search;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Types of blast programs that this system supports for nucleotide search
 *
 * @author Hector Plahar
 */
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
}
