package org.jbei.ice.lib.shared.dto;

import java.util.Date;

import org.jbei.ice.lib.shared.dto.entry.HasEntryInfo;

public class SampleInfo extends HasEntryInfo implements Comparable<SampleInfo> {

    private String sampleId;
    private String label;
    private String notes;
    private String location;
    private String locationId;
    private String depositor;
    private Date creationTime;

    public String getLabel() {
        return label;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String id) {
        this.sampleId = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public void setLocationId(String id) {
        this.locationId = id;
    }

    public String getLocationId() {
        return this.locationId;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getDepositor() {
        return this.depositor;
    }

    @Override
    public int compareTo(SampleInfo o) {
        return this.label.compareTo(o.getLabel());
    }
}
