package org.jbei.ice.lib.models;

import org.hibernate.annotations.Type;
import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.entry.model.Entry;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

@Entity
@Table(name = "shotgun_sequence")
@SequenceGenerator(name = "sequence", sequenceName = "shotgun_sequence_id_seq", allocationSize = 1)
public class ShotgunSequence implements IDataModel, IDataTransferModel {
    private static final long serialVersionUID = -850409542887009114L;

    @XmlTransient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "file_id", nullable = false, unique = true)
    private String fileId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "depositor", nullable = false)
    private String depositor;

    @Column(name = "creation_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    public ShotgunSequence() {
    }

    public ShotgunSequence(Entry entry, String fileId, String filename, String depositor, Date creationTime) {
        super();

        this.entry = entry;
        this.fileId = fileId;
        this.filename = filename;
        this.depositor = depositor;
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

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public ShotgunSequence toDataTransferObject() {
        ShotgunSequence info = new ShotgunSequence();
        info.setId(this.getId());
        info.setCreationTime(this.getCreationTime());
        info.setFilename(this.getFilename());

        info.setFileId(this.getFileId());
        return info;
    }
}
