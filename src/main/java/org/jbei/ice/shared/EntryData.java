package org.jbei.ice.shared;

import java.io.Serializable;

public abstract class EntryData implements Serializable {

    private static final long serialVersionUID = 1L;

    private long recordId;
    private String type;
    private String partId;
    private String name;
    private String alias;
    private String creator;
    private String status;
    private String ownerName;
    private String ownerId; // typically email
    private String keywords;
    private String summary;
    private String references;
    private String bioSafetyLevel;
    private String pI;
    private String ipInfo;
    private boolean hasAttachment;
    private boolean hasSample;
    private boolean hasSequence;
    private long created;
    private long modified;

    public EntryData() {
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public String getBioSafetyLevel() {
        return bioSafetyLevel;
    }

    public void setBioSafetyLevel(String bioSafetyLevel) {
        this.bioSafetyLevel = bioSafetyLevel;
    }

    public String getpI() {
        return pI;
    }

    public void setpI(String pI) {
        this.pI = pI;
    }

    public String getIpInfo() {
        return ipInfo;
    }

    public void setIpInfo(String ipInfo) {
        this.ipInfo = ipInfo;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOwner() {
        return ownerName;
    }

    public void setOwner(String owner) {
        this.ownerName = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public boolean isHasSample() {
        return hasSample;
    }

    public void setHasSample(boolean hasSample) {
        this.hasSample = hasSample;
    }

    public boolean isHasSequence() {
        return hasSequence;
    }

    public void setHasSequence(boolean hasSequence) {
        this.hasSequence = hasSequence;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setOwnerId(String id) {
        this.ownerId = id;
    }

    public String getOwnerId() {
        return this.ownerId;
    }
}
