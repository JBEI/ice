package org.jbei.ice.shared.dto;

import java.util.Date;

public class SampleInfo extends HasEntryData {

    private static final long serialVersionUID = 1L;

    private long id;
    private String label;
    private String notes;
    private String location;
    private long locationId;
    private Date creationTime;

    public String getLabel() {
        return label;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public void setLocationId(long id) {
        this.locationId = id;
    }

    public long getLocationId() {
        return this.locationId;
    }
}
