package org.jbei.ice.shared.dto;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EntryInfo implements IsSerializable {

    public enum EntryType implements IsSerializable {
        STRAIN("Strain", "strain"), PLASMID("Plasmid", "plasmid"), PART("Part", "part"), ARABIDOPSIS(
                "Arabidopsis Seed", "arabidopsis");

        private String name;
        private String display;

        EntryType(String display, String name) {
            this.display = display;
            this.name = name;
        }

        public static EntryType nameToType(String name) {
            for (EntryType type : EntryType.values()) {
                if (name.equals(type.getName()))
                    return type;
            }

            return null;
        }

        public String getName() {
            return this.name;
        }

        public String getDisplay() {
            return this.display;
        }
    }

    private String recordId;
    private String versionId;
    private String name;
    private EntryType type;
    private String owner;
    private String ownerEmail;
    private String creator;
    private String creatorEmail;
    private String alias;
    private String keywords;
    private String status;
    private String shortDescription;
    private String longDescription;
    private String longDescriptionType;
    private String references;
    private Date creationTime;
    private Date modificationTime;
    private Integer bioSafetyLevel;
    private String intellectualProperty;
    private String partId;
    private String link; // comma separated list of links
    private String principalInvestigator;

    //    private Set<SelectionMarker> selectionMarkers = new LinkedHashSet<SelectionMarker>();
    //    private final Set<Link> links = new LinkedHashSet<Link>();
    //    private final Set<Name> names = new LinkedHashSet<Name>();
    //    private final Set<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
    //    private final Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
    //    private final List<Parameter> parameters = new ArrayList<Parameter>();

    public EntryInfo() {
    }

    //    public Entry(String recordId, String versionId, String recordType, String owner,
    //            String ownerEmail, String creator, String creatorEmail, String status, String alias,
    //            String keywords, String shortDescription, String longDescription,
    //            String longDescriptionType, String references, Date creationTime, Date modificationTime) {
    //        this.recordId = recordId;
    //        this.versionId = versionId;
    //        this.recordType = recordType;
    //        this.owner = owner;
    //        this.ownerEmail = ownerEmail;
    //        this.creator = creator;
    //        this.creatorEmail = creatorEmail;
    //        this.status = status;
    //        this.alias = alias;
    //        this.keywords = keywords;
    //        this.shortDescription = shortDescription;
    //        this.longDescription = longDescription;
    //        this.longDescriptionType = longDescriptionType;
    //        this.references = references;
    //        this.creationTime = creationTime;
    //        this.modificationTime = modificationTime;
    //    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getLongDescriptionType() {
        return longDescriptionType;
    }

    public void setLongDescriptionType(String longDescriptionType) {
        this.longDescriptionType = longDescriptionType;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setBioSafetyLevel(Integer bioSafetyLevel) {
        this.bioSafetyLevel = bioSafetyLevel;
    }

    public Integer getBioSafetyLevel() {
        return bioSafetyLevel;
    }

    public void setIntellectualProperty(String intellectualProperty) {
        this.intellectualProperty = intellectualProperty;
    }

    public String getIntellectualProperty() {
        return intellectualProperty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }
}
