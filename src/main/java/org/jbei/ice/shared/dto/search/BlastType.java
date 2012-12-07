package org.jbei.ice.shared.dto.search;

import org.jbei.ice.shared.dto.IDTOModel;

/**
 * Type of blast searches the system supports
 *
 * @author Hector Plahar
 */
public enum BlastType implements IDTOModel {

    BLAST_N("Nucleotide Search (blastn)"),
    TBLAST_X("Nucleotide Translated Search (tblastx)");

    private String display;

    BlastType(String display) {
        this.display = display;
    }

    private BlastType() {
    }

    @Override
    public String toString() {
        return display;
    }
}
