package org.jbei.ice.shared.dto;

import java.util.Date;

public class SampleInfo extends HasEntryInfo {

    private String id;
    private String label;
    private String notes;
    private String location;
    private String locationId;
    private String depositor;
    private Date creationTime;

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
