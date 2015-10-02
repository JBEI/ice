package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class TraceSequenceAlignmentInfo implements IDataTransferModel {

    private int score;
    private int strand;
    private int queryStart;
    private int queryEnd;
    private int subjectStart;
    private int subjectEnd;
    private String queryAlignment;
    private String subjectAlignment;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
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

    public String getQueryAlignment() {
        return queryAlignment;
    }

    public void setQueryAlignment(String queryAlignment) {
        this.queryAlignment = queryAlignment;
    }

    public String getSubjectAlignment() {
        return subjectAlignment;
    }

    public void setSubjectAlignment(String subjectAlignment) {
        this.subjectAlignment = subjectAlignment;
    }
}
