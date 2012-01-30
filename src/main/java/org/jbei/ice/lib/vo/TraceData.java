package org.jbei.ice.lib.vo;

import java.io.Serializable;

/**
 * Value object for sequence trace data.
 * 
 * @author Ziovii Dmytriv
 * 
 */
public class TraceData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filename;
    private String sequence;
    private int score;
    private int strand;
    private int queryStart;
    private int queryEnd;
    private int subjectStart;
    private int subjectEnd;
    private String queryAlignment;
    private String subjectAlignment;

    public TraceData() {
        super();
    }

    public TraceData(String filename, String sequence, int score, int strand, int queryStart,
            int queryEnd, int subjectStart, int subjectEnd, String queryAlignment,
            String subjectAlignment) {
        super();

        this.filename = filename;
        this.sequence = sequence;
        this.score = score;
        this.strand = strand;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.subjectStart = subjectStart;
        this.subjectEnd = subjectEnd;
        this.queryAlignment = queryAlignment;
        this.subjectAlignment = subjectAlignment;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

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