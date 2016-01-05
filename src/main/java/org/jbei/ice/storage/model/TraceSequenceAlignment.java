package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.lib.dto.entry.TraceSequenceAlignmentInfo;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Store computed trace alignment information.
 *
 * @author Zinovii Dmytrv, Timothy Ham
 */
@Entity
@Table(name = "trace_sequence_alignments")
@SequenceGenerator(name = "sequence", sequenceName = "trace_sequence_alignments_id_seq", allocationSize = 1)
public class TraceSequenceAlignment implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trace_sequence_id", nullable = false, unique = true)
    private TraceSequence traceSequence;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "strand", nullable = false)
    private int strand;

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
    @Type(type = "org.hibernate.type.TextType")
    private String queryAlignment;

    @Column(name = "subject_alignment", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String subjectAlignment;

    @Column(name = "sequence_hash", length = 40)
    private String sequenceHash;

    @Column(name = "modification_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    public TraceSequenceAlignment() {
    }

    public TraceSequenceAlignment(TraceSequence traceSequence, int score, int strand,
            int queryStart, int queryEnd, int subjectStart, int subjectEnd, String queryAlignment,
            String subjectAlignment, String sequenceHash, Date modificationTime) {
        this.traceSequence = traceSequence;
        this.strand = strand;
        this.score = score;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.subjectStart = subjectStart;
        this.subjectEnd = subjectEnd;
        this.queryAlignment = queryAlignment;
        this.subjectAlignment = subjectAlignment;
        this.sequenceHash = sequenceHash;
        this.modificationTime = modificationTime;
    }

    public TraceSequence getTraceSequence() {
        return traceSequence;
    }

    public void setTraceSequence(TraceSequence traceSequence) {
        this.traceSequence = traceSequence;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
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

    public String getSequenceHash() {
        return sequenceHash;
    }

    public void setSequenceHash(String sequenceHash) {
        this.sequenceHash = sequenceHash;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    @Override
    public TraceSequenceAlignmentInfo toDataTransferObject() {
        TraceSequenceAlignmentInfo alignmentInfo = new TraceSequenceAlignmentInfo();
        alignmentInfo.setScore(score);
        alignmentInfo.setStrand(strand);
        alignmentInfo.setQueryStart(queryStart);
        alignmentInfo.setQueryEnd(queryEnd);
        alignmentInfo.setSubjectStart(subjectStart);
        alignmentInfo.setSubjectEnd(subjectEnd);
        alignmentInfo.setQueryAlignment(queryAlignment);
        alignmentInfo.setSubjectAlignment(subjectAlignment);
        return alignmentInfo;
    }
}
