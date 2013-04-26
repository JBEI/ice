package org.jbei.ice.lib.vo;

import java.io.Serializable;

import org.jbei.ice.lib.models.AnnotationLocation;

/**
 * Value object to hold {@link AnnotationLocation} data.
 *
 * @author Timothy Ham
 */
public class DNAFeatureLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    private int genbankStart;
    private int end;
    private boolean singleResidue;
    private boolean inBetween;
    private String uri;

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

    public void setSingleResidue(boolean singleResidue) {
        this.singleResidue = singleResidue;
    }

    public void setInBetween(boolean inBetween) {
        this.inBetween = inBetween;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
