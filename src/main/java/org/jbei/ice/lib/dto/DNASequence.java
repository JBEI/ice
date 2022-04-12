package org.jbei.ice.lib.dto;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Value object to hold DNA sequence.
 *
 * @author Hector Plahar
 */
public class DNASequence implements IDataTransferModel {

    private static final long serialVersionUID = 1L;

    private String sequence;

    public DNASequence() {
        sequence = "";
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
