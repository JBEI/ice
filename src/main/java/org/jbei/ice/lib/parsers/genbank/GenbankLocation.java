package org.jbei.ice.lib.parsers.genbank;

/**
 * Represent a contiguous Genbank location, including a single base pair.
 *
 * @author Timothy Ham, Hector Plahar
 */
public class GenbankLocation {

    private int genbankStart = -1;
    private int end = -1;
    private boolean inbetween = false;
    private boolean singleResidue = false;

    public GenbankLocation(int genbankStart, int end) {
        super();
        setGenbankStart(genbankStart);
        setEnd(end);
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

    public void setInbetween(boolean inbetween) {
        this.inbetween = inbetween;
    }

    public boolean isInbetween() {
        return inbetween;
    }

    public void setSingleResidue(boolean singleResidue) {
        this.singleResidue = singleResidue;
    }

    public boolean isSingleResidue() {
        return singleResidue;
    }
}
