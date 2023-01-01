package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.sample.SampleRequest;
import org.jbei.ice.dto.sample.SampleRequestStatus;
import org.jbei.ice.dto.sample.SampleRequestType;
import org.jbei.ice.entry.EntryUtil;
import org.jbei.ice.storage.DataModel;

import java.util.Date;

/**
 * Storage data model for sample requests
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REQUEST")
@SequenceGenerator(name = "request_id", sequenceName = "request_id_seq", allocationSize = 1)
public class Request implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "request_id")
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_id", nullable = false)
    private AccountModel account;

    @Column(name = "requested")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requested;

    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private org.jbei.ice.storage.model.Entry entry;

    @Column(name = "request_type")
    @Enumerated(value = EnumType.STRING)
    private SampleRequestType type;

    @Column(name = "growth_temp")
    private Integer growthTemperature;

    @Column(name = "plate_description")
    private String plateDescription;

    @Column(name = "request_status")
    @Enumerated(value = EnumType.STRING)
    private SampleRequestStatus status = SampleRequestStatus.IN_CART;

    public long getId() {
        return this.id;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
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

    public void setGrowthTemperature(int growthTemperature) {
        this.growthTemperature = growthTemperature;
    }

    public Integer getGrowthTemperature() {
        return growthTemperature;
    }

    public String getPlateDescription() {
        return plateDescription;
    }

    public void setPlateDescription(String plateDescription) {
        this.plateDescription = plateDescription;
    }

    public org.jbei.ice.storage.model.Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public SampleRequest toDataTransferObject() {
        SampleRequest sampleRequest = new SampleRequest();
        sampleRequest.setId(getId());
        sampleRequest.setRequestType(getType());
        sampleRequest.setStatus(getStatus());
        if (growthTemperature != null)
            sampleRequest.setGrowthTemperature(growthTemperature);
        sampleRequest.setPlateDescription(plateDescription);
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData data = new PartData(type);
        data.setId(entry.getId());
        data.setPartId(entry.getPartNumber());
        data.setSelectionMarkers(EntryUtil.getSelectionMarkersAsList(entry.getSelectionMarkers()));
        data.setName(entry.getName());
        data.setBioSafetyLevel(entry.getBioSafetyLevel());
        sampleRequest.setPartData(data);
        sampleRequest.setRequester(getAccount().toDataTransferObject());
        sampleRequest.setRequestTime(getRequested().getTime());
        sampleRequest.setUpdateTime(getUpdated() == null ? sampleRequest.getRequestTime() : getUpdated().getTime());
        return sampleRequest;
    }
}
