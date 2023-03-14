package org.jbei.ice.dto.entry;

import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for parts and associated meta-data
 * // todo : some of these fields need to be moved; list is too long
 *
 * @author Hector Plahar
 */
public class PartData implements IDataTransferModel {

    private long id;
    private EntryType type;
    private Visibility visible;
    private final ArrayList<PartData> parents;

    private int index;
    private String recordId;
    private String name;
    private String owner;
    private String ownerEmail;
    private long ownerId;
    private long creationTime;
    private long modificationTime;
    private String partId;
    private String basePairCount;
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
    private final ArrayList<PartData> linkedParts;
    private ArrayList<CustomEntryField> customFields;

    private final List<EntryField> fields;

    public PartData(EntryType type) {
        this.type = type;
        accessPermissions = new ArrayList<>();
        linkedParts = new ArrayList<>();
        parents = new ArrayList<>();
        customFields = new ArrayList<>();
        this.fields = new ArrayList<>();
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

    public ArrayList<CustomField> getParameters() {
        return this.parameters;
    }

    public void setCustomFields(ArrayList<CustomField> parameters) {
        this.parameters = parameters;
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

    public ArrayList<PartData> getParents() {
        return parents;
    }

    public List<CustomEntryField> getCustomEntryFields() {
        return customFields;
    }

    public void setCustomEntryFields(List<CustomEntryField> fields) {
        this.customFields = new ArrayList<>(fields);
    }

    public String getBasePairCount() {
        return basePairCount;
    }

    public void setBasePairCount(String basePairCount) {
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

    public List<EntryField> getFields() {
        return fields;
    }
}
