package org.jbei.ice.lib.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "trace_sequence_alignments")
@SequenceGenerator(name = "sequence", sequenceName = "trace_sequence_alignments_id_seq", allocationSize = 1)
public class TraceSequenceAlignment implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trace_sequence_id", nullable = false, unique = true)
    private TraceSequence traceSequence;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "query_start", nullable = false)
    private int queryStart;

    @Column(name = "query_end", nullable = false)
    private int queryEnd;

    @Column(name = "subject_start", nullable = false)
    private int subjectStart;

    @Column(name = "subject_end", nullable = false)
    private int subjectEnd;

    @Column(name = "query_alignment", nullable = false)
    @Lob
    private String queryAlignment;

    @Column(name = "subject_alignment", nullable = false)
    @Lob
    private String subjectAlignment;

    @Column(name = "modification_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    public TraceSequenceAlignment() {
        super();
    }

    public TraceSequenceAlignment(TraceSequence traceSequence, int score, int queryStart,
            int queryEnd, int subjectStart, int subjectEnd, String queryAlignment,
            String subjectAlignment, Date modificationTime) {
        this.traceSequence = traceSequence;
        this.score = score;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.subjectStart = subjectStart;
        this.subjectEnd = subjectEnd;
        this.queryAlignment = queryAlignment;
        this.subjectAlignment = subjectAlignment;
        this.modificationTime = modificationTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TraceSequence getTraceSequence() {
        return traceSequence;
    }

    public void setTraceSequence(TraceSequence traceSequence) {
        this.traceSequence = traceSequence;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }
}
