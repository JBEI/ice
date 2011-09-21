package org.jbei.ice.shared.dto;

public class BlastResultInfo extends HasEntryData {

    private static final long serialVersionUID = 1L;

    private float bitScore;
    private float eValue;
    private long id;

    public float getBitScore() {
        return bitScore;
    }

    public void setBitScore(float bitScore) {
        this.bitScore = bitScore;
    }

    public float geteValue() {
        return eValue;
    }

    public void seteValue(float eValue) {
        this.eValue = eValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
