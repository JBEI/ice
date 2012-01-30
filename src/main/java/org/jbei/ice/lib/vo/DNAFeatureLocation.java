package org.jbei.ice.lib.vo;

import java.io.Serializable;

import org.jbei.ice.lib.models.AnnotationLocation;

/**
 * Value object to hold {@link AnnotationLocation} data.
 * 
 * @author Timothy Ham
 * 
 */
public class DNAFeatureLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    private int genbankStart;
    private int end;
    private boolean singleResidue;
    private boolean inBetween;

    public DNAFeatureLocation() {
        super();
    }

    public DNAFeatureLocation(int genbankStart, int end) {
        this.genbankStart = genbankStart;
        this.end = end;
    }

    public int getGenbankStart() {
        return genbankStart;
    }

    public void setGenbankStart(int genbankStart) {
        this.genbankStart = genbankStart;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isSingleResidue() {
        return singleResidue;
    }

    public void setSingleResidue(boolean singleResidue) {
        this.singleResidue = singleResidue;
    }

    public boolean isInBetween() {
        return inBetween;
    }

    public void setInBetween(boolean inBetween) {
        this.inBetween = inBetween;
    }

}
