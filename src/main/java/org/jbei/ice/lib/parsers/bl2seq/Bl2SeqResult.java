package org.jbei.ice.lib.parsers.bl2seq;

public class Bl2SeqResult {
    private int rawScore;
    private int queryStart;
    private int queryEnd;
    private String querySequence;
    private int subjectStart;
    private int subjectEnd;
    private String subjectSequence;
    private int orientation; //0 for +/+, 1 for +/-

    public Bl2SeqResult(int rawScore, int queryStart, int queryEnd, String querySequence,
            int subjectStart, int subjectEnd, String subjectSequence, int orientation) {
        this.rawScore = rawScore;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.querySequence = querySequence;
        this.subjectStart = subjectStart;
        this.subjectEnd = subjectEnd;
        this.subjectSequence = subjectSequence;
        this.orientation = orientation;
    }

    public int getRawScore() {
        return rawScore;
    }

    public void setRawScore(int rawScore) {
        this.rawScore = rawScore;
    }

    public int getQueryStart() {
        return queryStart;
    }

    public void setQueryStart(int queryStart) {
        this.queryStart = queryStart;
    }

    public int getQueryEnd() {
        return queryEnd;
    }

    public void setQueryEnd(int queryEnd) {
        this.queryEnd = queryEnd;
    }

    public String getQuerySequence() {
        return querySequence;
    }

    public void setQuerySequence(String querySequence) {
        this.querySequence = querySequence;
    }

    public int getSubjectStart() {
        return subjectStart;
    }

    public void setSubjectStart(int subjectStart) {
        this.subjectStart = subjectStart;
    }

    public int getSubjectEnd() {
        return subjectEnd;
    }

    public void setSubjectEnd(int subjectEnd) {
        this.subjectEnd = subjectEnd;
    }

    public String getSubjectSequence() {
        return subjectSequence;
    }

    public void setSubjectSequence(String subjectSequence) {
        this.subjectSequence = subjectSequence;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
