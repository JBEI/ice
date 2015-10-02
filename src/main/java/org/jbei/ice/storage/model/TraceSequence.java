package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Store sequence trace file information.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
@Entity
@Table(name = "trace_sequence")
@SequenceGenerator(name = "sequence", sequenceName = "trace_sequence_id_seq", allocationSize = 1)
public class TraceSequence implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "file_id", length = 36, nullable = false, unique = true)
    private String fileId;

    @Column(name = "filename", length = 255, nullable = false)
    private String filename;

    @Column(name = "depositor", length = 255, nullable = false)
    private String depositor;

    @Column(name = "sequence", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequence;

    @Column(name = "creation_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @OneToOne(mappedBy = "traceSequence", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private TraceSequenceAlignment traceSequenceAlignment;

    public TraceSequence() {
    }

    public TraceSequence(Entry entry, String fileId, String filename, String depositor,
            String sequence, Date creationTime) {
        super();

        this.entry = entry;
        this.fileId = fileId;
        this.filename = filename;
        this.depositor = depositor;
        this.sequence = sequence;
        this.creationTime = creationTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public TraceSequenceAlignment getTraceSequenceAlignment() {
        return traceSequenceAlignment;
    }

    public void setTraceSequenceAlignment(TraceSequenceAlignment traceSequenceAlignment) {
        this.traceSequenceAlignment = traceSequenceAlignment;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public TraceSequenceAnalysis toDataTransferObject() {
        TraceSequenceAnalysis info = new TraceSequenceAnalysis();
        info.setId(this.getId());
        info.setCreated(this.getCreationTime());
        info.setFilename(this.getFilename());
        info.setSequence(this.getSequence());
        if (this.getTraceSequenceAlignment() != null) {
            info.setTraceSequenceAlignment(this.getTraceSequenceAlignment().toDataTransferObject());
        }

        info.setFileId(this.getFileId());
        return info;
    }
}
