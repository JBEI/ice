package org.jbei.ice.shared.dto.search;

import org.jbei.ice.shared.dto.IDTOModel;

/**
 * @author Hector Plahar
 */
public class BlastQuery implements IDTOModel {

    public static final long serialVersionUID = 1L;

    private BlastProgram blastProgram;
    private String sequence;

    // required no arg constructor
    public BlastQuery() {
    }

    public BlastQuery(BlastProgram program, String sequence) {
        setBlastProgram(program);
        setSequence(sequence);
    }

    public BlastProgram getBlastProgram() {
        return blastProgram;
    }

    public void setBlastProgram(BlastProgram blastProgram) {
        this.blastProgram = blastProgram;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
