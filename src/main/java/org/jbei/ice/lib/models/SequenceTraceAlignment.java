package org.jbei.ice.lib.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "sequence_trace_alignments")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_trace_alignments_id_seq", allocationSize = 1)
public class SequenceTraceAlignment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "depositor", length = 255)
    private String depositor;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "score")
    private int score;

    @Column(name = "bitmatch")
    @Lob
    private String bitmatch;

    @Column(name = "query_start")
    private int queryStart;

    @Column(name = "query_end")
    private int queryEnd;

    @Column(name = "subject_start")
    private int subjectStart;

    @Column(name = "subject_end")
    private int subjectEnd;

    @Column(name = "query_alignment")
    @Lob
    private String queryAlignment;

    @Column(name = "subject_alignment")
    @Lob
    private String subjectAlignment;

    public SequenceTraceAlignment() {
        super();
    }

    public SequenceTraceAlignment(Entry entry, String depositor, String name, int score,
            String bitmatch, int queryStart, int queryEnd, int subjectStart, int subjectEnd,
            String queryAlignment, String subjectAlignment) {
        super();

        this.depositor = depositor;
        this.entry = entry;
        this.name = name;
        this.score = score;
        this.bitmatch = bitmatch;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.subjectStart = subjectStart;
        this.subjectEnd = subjectEnd;
        this.queryAlignment = queryAlignment;
        this.subjectAlignment = subjectAlignment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getBitmatch() {
        return bitmatch;
    }

    public void setBitmatch(String bitmatch) {
        this.bitmatch = bitmatch;
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
