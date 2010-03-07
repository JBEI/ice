package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "trace_sequence")
@SequenceGenerator(name = "sequence", sequenceName = "trace_sequence_id_seq", allocationSize = 1)
public class TraceSequence implements Serializable {
    private static final long serialVersionUID = -850409542887009114L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @OneToOne
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "depositor", length = 255, nullable = false)
    private String depositor;

    @Column(name = "sequence", nullable = false)
    @Lob
    private String sequence;

    @Column(name = "sequence_user", nullable = false)
    @Lob
    private String sequenceUser;

    @OneToOne(optional = true, mappedBy = "traceSequence")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private TraceSequenceAlignment alignment;

    @Column(name = "creation_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    public TraceSequence() {
    }

    public TraceSequence(Entry entry, String name, String depositor, String sequence,
            String sequenceUser, TraceSequenceAlignment alignment, Date creationTime) {
        this.entry = entry;
        this.name = name;
        this.depositor = depositor;
        this.sequence = sequence;
        this.sequenceUser = sequenceUser;
        this.alignment = alignment;
        this.creationTime = creationTime;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSequenceUser() {
        return sequenceUser;
    }

    public void setSequenceUser(String sequenceUser) {
        this.sequenceUser = sequenceUser;
    }

    public TraceSequenceAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(TraceSequenceAlignment alignment) {
        this.alignment = alignment;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
