package org.jbei.ice.lib.dto.search;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Types of blast programs that this system supports for nucleotide search
 *
 * @author Hector Plahar
 */
public enum BlastProgram implements IDataTransferModel {

    BLAST_N("blastn"),
    TBLAST_X("tblastx");

    private String name;

    BlastProgram(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
