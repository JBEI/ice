package org.jbei.ice.lib.entry.sample.model;

import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

/**
 * Storage data model for sample requests
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REQUEST")
@SequenceGenerator(name = "sequence", sequenceName = "request_id_seq", allocationSize = 1)
public class Request implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    @Column(name = "requested")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requested;

    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @Column(name = "request_type")
    @Enumerated(value = EnumType.STRING)
    private SampleRequestType type;

    @Column(name = "request_status")
    @Enumerated(value = EnumType.STRING)
    private SampleRequestStatus status = SampleRequestStatus.IN_CART;

    public long getId() {
        return this.id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

    public SampleRequestType getType() {
        return type;
    }

    public void setType(SampleRequestType requestType) {
        this.type = requestType;
    }

    public SampleRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SampleRequestStatus requestStatus) {
        this.status = requestStatus;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public static SampleRequest toDTO(Request request) {
        SampleRequest sampleRequest = new SampleRequest();
        sampleRequest.setId(request.getId());
        sampleRequest.setRequestType(request.getType());
        sampleRequest.setStatus(request.getStatus());
        PartData data = new PartData();
        Entry entry = request.getEntry();
        data.setId(entry.getId());
        data.setPartId(entry.getPartNumber());
        sampleRequest.setPartData(data);
        sampleRequest.setRequester(Account.toDTO(request.getAccount()));
        sampleRequest.setRequestTime(request.getRequested().getTime());
        sampleRequest.setUpdateTime(request.getUpdated() == null
                                            ? sampleRequest.getRequestTime() : request.getUpdated().getTime());
        return sampleRequest;
    }
}
