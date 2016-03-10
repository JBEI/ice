package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Data transfer object for parts and associated meta-data
 *
 * @author Hector Plahar
 */
public class PartData implements IDataTransferModel {

    private static final long serialVersionUID = 1l;

    private long id;
    private EntryType type;
    private Visibility visible;
    private ArrayList<PartData> parents;

    private int index;
    private String recordId;
    private String name;
    private String owner;
    private String ownerEmail;
    private long ownerId;
    private String creator;
    private String creatorEmail;
    private long creatorId;
    private String alias;
    private String keywords;
    private String status;
    private String shortDescription;
    private String longDescription;
    private String references;
    private long creationTime;
    private long modificationTime;
    private Integer bioSafetyLevel;
    private String intellectualProperty;
    private String partId;
    private ArrayList<String> links; // comma separated list of links
    private String principalInvestigator;
    private String principalInvestigatorEmail;
    private long principalInvestigatorId;
    private ArrayList<String> selectionMarkers;
    private String fundingSource;
    private long basePairCount;
    private long featureCount;
    private long viewCount;

    private boolean hasAttachment;
    private boolean hasSample;
    private boolean hasSequence;
    private String sequenceFileName;
    private boolean hasOriginalSequence;
    private ArrayList<AttachmentInfo> attachments;
    private ArrayList<CustomField> parameters;
    private boolean canEdit; // whether current user that requested this entry info has write privs
    private ArrayList<AccessPermission> accessPermissions;
    private boolean publicRead;
    private ArrayList<PartData> linkedParts;

    private StrainData strainData;
    private PlasmidData plasmidData;
    private ArabidopsisSeedData arabidopsisSeedData;

    public PartData(EntryType type) {
        this.type = type;
        accessPermissions = new ArrayList<>();
        linkedParts = new ArrayList<>();
        parents = new ArrayList<>();
        status = "";
        bioSafetyLevel = 1;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
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

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    /**
     * @return number of millisecs since the epoch when part was recorded
     */
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return number of millisecs since epoch when part was updated
     */
    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(long modificationTime) {
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

    public ArrayList<CustomField> getCustomFields() {
        return this.parameters;
    }

    public void setCustomFields(ArrayList<CustomField> parameters) {
        this.parameters = parameters;
    }

    public String getFundingSource() {
        return this.fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Visibility getVisibility() {
        return visible;
    }

    public void setVisibility(Visibility visible) {
        this.visible = visible;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public boolean isHasOriginalSequence() {
        return hasOriginalSequence;
    }

    public void setHasOriginalSequence(boolean hasOriginalSequence) {
        this.hasOriginalSequence = hasOriginalSequence;
    }

    public boolean isPublicRead() {
        return publicRead;
    }

    public void setPublicRead(boolean publicRead) {
        this.publicRead = publicRead;
    }

    /**
     * @return the list of parts that this part links to.
     */
    public ArrayList<PartData> getLinkedParts() {
        return linkedParts;
    }

    public String getPrincipalInvestigatorEmail() {
        return principalInvestigatorEmail;
    }

    public void setPrincipalInvestigatorEmail(String principalInvestigatorEmail) {
        this.principalInvestigatorEmail = principalInvestigatorEmail;
    }

    public long getPrincipalInvestigatorId() {
        return principalInvestigatorId;
    }

    public void setPrincipalInvestigatorId(long principalInvestigatorId) {
        this.principalInvestigatorId = principalInvestigatorId;
    }

    public ArrayList<AccessPermission> getAccessPermissions() {
        return accessPermissions;
    }

    public ArrayList<AttachmentInfo> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<AttachmentInfo> attachments) {
        this.attachments = attachments;
    }

    public void setAccessPermissions(ArrayList<AccessPermission> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public ArrayList<String> getSelectionMarkers() {
        return selectionMarkers;
    }

    public void setSelectionMarkers(ArrayList<String> selectionMarkers) {
        this.selectionMarkers = selectionMarkers;
    }

    public ArrayList<String> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }

    public ArrayList<PartData> getParents() {
        return parents;
    }

    public StrainData getStrainData() {
        return strainData;
    }

    public void setStrainData(StrainData strainData) {
        this.strainData = strainData;
    }

    public PlasmidData getPlasmidData() {
        return plasmidData;
    }

    public void setPlasmidData(PlasmidData plasmidData) {
        this.plasmidData = plasmidData;
    }

    public ArabidopsisSeedData getArabidopsisSeedData() {
        return arabidopsisSeedData;
    }

    public void setArabidopsisSeedData(ArabidopsisSeedData arabidopsisSeedData) {
        this.arabidopsisSeedData = arabidopsisSeedData;
    }

    public long getBasePairCount() {
        return basePairCount;
    }

    public void setBasePairCount(long basePairCount) {
        this.basePairCount = basePairCount;
    }

    public long getFeatureCount() {
        return featureCount;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public void setFeatureCount(long featureCount) {
        this.featureCount = featureCount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSequenceFileName() {
        return sequenceFileName;
    }

    public void setSequenceFileName(String sequenceFileName) {
        this.sequenceFileName = sequenceFileName;
    }
}
