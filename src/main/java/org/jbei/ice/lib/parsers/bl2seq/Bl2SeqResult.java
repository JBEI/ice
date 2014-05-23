package org.jbei.ice.lib.parsers.bl2seq;

/**
 * Stores information about the bl2seq output.
 *
 * @author Zinovii Dmytriv
 */
public class Bl2SeqResult {

    private final int score;
    private final int queryStart;
    private final int queryEnd;
    private final String querySequence;
    private final int subjectStart;
    private final int subjectEnd;
    private final String subjectSequence;
    private final int orientation; //0 for +/+, 1 for +/-

    public Bl2SeqResult(int score, int queryStart, int queryEnd, String querySequence,
            int subjectStart, int subjectEnd, String subjectSequence, int orientation) {
        this.score = score;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.querySequence = querySequence;
        this.subjectStart = subjectStart;
        this.subjectEnd = subjectEnd;
        this.subjectSequence = subjectSequence;
        this.orientation = orientation;
    }

    public int getScore() {
        return score;
    }

    public int getQueryStart() {
        return queryStart;
    }

    public int getQueryEnd() {
        return queryEnd;
    }

    public String getQuerySequence() {
        return querySequence;
    }

    public int getSubjectStart() {
        return subjectStart;
    }

    public int getSubjectEnd() {
        return subjectEnd;
    }

    public String getSubjectSequence() {
        return subjectSequence;
    }

    public int getOrientation() {
        return orientation;
    }
}
