package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.dto.sample.SampleRequestStatus;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.Date;

@Entity
@Table(name = "sample_create")
@SequenceGenerator(name = "sample_create_id", sequenceName = "sample_create_id_seq", allocationSize = 1)
public class SampleCreateModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sample_create_id")
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_id", nullable = false)
    private AccountModel account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private org.jbei.ice.storage.model.Folder folder;

    @Column(name = "requested")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requested;

    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private SampleRequestStatus status = SampleRequestStatus.PENDING;

    @Override
    public long getId() {
        return id;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    public org.jbei.ice.storage.model.Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Date getRequested() {
        return requested;
    }

    public void setRequested(Date requested) {
        this.requested = requested;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public SampleRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SampleRequestStatus status) {
        this.status = status;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;
    }
}
