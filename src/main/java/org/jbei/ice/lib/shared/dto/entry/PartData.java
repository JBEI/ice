package org.jbei.ice.lib.shared.dto.entry;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

@XmlSeeAlso({StrainData.class, PlasmidData.class, ArabidopsisSeedData.class})
public class PartData implements IDTOModel {

    private static final long serialVersionUID = 1l;

    private long id;
    private String recordId;
    private String name;
    private EntryType type;
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
    private String links; // comma separated list of links
    private String principalInvestigator;
    private String principalInvestigatorEmail;
    private String selectionMarkers;
    private String fundingSource;
    private boolean hasAttachment;
    private boolean hasSample;
    private boolean hasSequence;
    private boolean hasOriginalSequence;
    private ArrayList<AttachmentInfo> attachments;
    private ArrayList<SampleStorage> sampleStorage;
    private ArrayList<SequenceAnalysisInfo> sequenceAnalysis;
    private ArrayList<CustomField> parameters;
    private boolean canEdit; // whether current user that requested this entry info has write privs
    private Visibility visible;
    private ArrayList<AccessPermission> accessPermissions;
    private ArrayList<UserComment> comments;
    private String sbolVisualURL;
    private boolean publicRead;
    private ArrayList<PartData> linkedParts;

    private PartData info; // typically used with strain with plasmid

    public PartData() {
        this(EntryType.PART);
    }

    public PartData(EntryType type) {
        this.type = type;
        sampleStorage = new ArrayList<SampleStorage>();
        accessPermissions = new ArrayList<AccessPermission>();
        comments = new ArrayList<UserComment>();
        linkedParts = new ArrayList<PartData>();
        sequenceAnalysis = new ArrayList<SequenceAnalysisInfo>();
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

    public void setSelectionMarkers(String markers) {
        this.selectionMarkers = markers;
    }

    public String getSelectionMarkers() {
        return this.selectionMarkers;
    }

    public ArrayList<AttachmentInfo> getAttachments() {
        return this.attachments;
    }

    public void setAttachments(ArrayList<AttachmentInfo> attachments) {
        this.attachments = attachments;
    }

    public ArrayList<SequenceAnalysisInfo> getSequenceAnalysis() {
        return this.sequenceAnalysis;
    }

    public void setSequenceAnalysis(ArrayList<SequenceAnalysisInfo> analyses) {
        this.sequenceAnalysis = analyses;
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

    public String getLinks() {
        return this.links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public ArrayList<SampleStorage> getSampleStorage() {
        return sampleStorage;
    }

    public SampleStorage getOneSampleStorage() {
        if (sampleStorage.isEmpty()) {
            sampleStorage.add(new SampleStorage());
        }

        return sampleStorage.get(0);
    }

    public void setSampleMap(ArrayList<SampleStorage> sampleStorage) {
        this.sampleStorage.clear();
        this.sampleStorage.addAll(sampleStorage);
        setHasSample(!sampleStorage.isEmpty());
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

    public PartData getInfo() {
        return info;
    }

    public void setInfo(PartData info) {
        this.info = info;
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

    public ArrayList<AccessPermission> getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(ArrayList<AccessPermission> accessPermissions) {
        this.accessPermissions.clear();
        this.accessPermissions.addAll(accessPermissions);
    }

    public boolean isHasOriginalSequence() {
        return hasOriginalSequence;
    }

    public void setHasOriginalSequence(boolean hasOriginalSequence) {
        this.hasOriginalSequence = hasOriginalSequence;
    }

    public ArrayList<UserComment> getComments() {
        return comments;
    }

    public String getSbolVisualURL() {
        return sbolVisualURL;
    }

    public void setSbolVisualURL(String sbolVisualURL) {
        this.sbolVisualURL = sbolVisualURL;
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
}
