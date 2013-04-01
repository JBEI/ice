package org.jbei.ice.lib.vo;

import java.io.Serializable;

/**
 * Interface for object holding sequence.
 *
 * @author Zinovii Dmytriv
 */
public interface IDNASequence extends Serializable {
    /**
     * Return sequence string.
     *
     * @return Sequence string.
     */
    String getSequence();

    /**
     * Set the sequence string.
     *
     * @param sequence
     */
    void setSequence(String sequence);
}
