package org.jbei.ice.lib.search;

import java.io.Serializable;

import org.jbei.ice.lib.models.Entry;

public class BlastResult implements Serializable, Comparable<BlastResult> {

    private static final long serialVersionUID = 1L;

    private Entry entry;
    private float score;
    private float relativeScore;
    private String queryId;
    private String subjectId;
    private Float percentId;
    private Integer alignmentLength;
    private Integer mismatches;
    private Integer gapOpenings;
    private Integer qStart;
    private Integer qEnd;
    private Integer sStart;
    private Integer sEnd;
    private Float eValue;
    private Float bitScore;

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setRelativeScore(float relativeScore) {
        this.relativeScore = relativeScore;
    }

    public float getRelativeScore() {
        return relativeScore;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Float getPercentId() {
        return percentId;
    }

    public void setPercentId(Float percentId) {
        this.percentId = percentId;
    }

    public Integer getAlignmentLength() {
        return alignmentLength;
    }

    public void setAlignmentLength(Integer alignmentLength) {
        this.alignmentLength = alignmentLength;
    }

    public Integer getMismatches() {
        return mismatches;
    }

    public void setMismatches(Integer mismatches) {
        this.mismatches = mismatches;
    }

    public Integer getGapOpenings() {
        return gapOpenings;
    }

    public void setGapOpenings(Integer gapOpenings) {
        this.gapOpenings = gapOpenings;
    }

    public Integer getqStart() {
        return qStart;
    }

    public void setqStart(Integer qStart) {
        this.qStart = qStart;
    }

    public Integer getqEnd() {
        return qEnd;
    }

    public void setqEnd(Integer qEnd) {
        this.qEnd = qEnd;
    }

    public Integer getsStart() {
        return sStart;
    }

    public void setsStart(Integer sStart) {
        this.sStart = sStart;
    }

    public Integer getsEnd() {
        return sEnd;
    }

    public void setsEnd(Integer sEnd) {
        this.sEnd = sEnd;
    }

    public Float geteValue() {
        return eValue;
    }

    public void seteValue(Float eValue) {
        this.eValue = eValue;
    }

    public Float getBitScore() {
        return bitScore;
    }

    public void setBitScore(Float bitScore) {
        this.bitScore = bitScore;
    }

    public int compareTo(BlastResult o) {
        // FindBugs recommend that compareTo(...) return zero iff equals(...) return true.
        // However, in this particular case we are only sorting by relativeScore, so this method is 
        // acceptable. 
        Float temp = o.relativeScore - this.getRelativeScore();
        return temp.intValue();
    }
}
