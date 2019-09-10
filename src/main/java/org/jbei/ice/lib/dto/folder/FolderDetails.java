package org.jbei.ice.lib.dto.folder;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.folder.AbstractFolder;

import java.util.Objects;

/**
 * Folder Transfer Object
 *
 * @author Hector Plahar
 */
public class FolderDetails extends AbstractFolder {

    private String folderName;
    private long count;
    private String description;
    private boolean propagatePermission;
    private FolderType type;
    private AccountTransfer owner;    // owner or person sharing this folder
    private boolean publicReadAccess;
    private boolean canEdit;
    private FolderDetails parent;
    private RegistryPartner remotePartner;
    private SampleRequest sampleRequest;

    public FolderDetails() {
        super();
    }

    public FolderDetails(long id, String name) {
        super(id);
        this.folderName = name;
    }

    public String getName() {
        return this.folderName;
    }

    public void setName(String name) {
        this.folderName = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FolderType getType() {
        return type;
    }

    public void setType(FolderType type) {
        this.type = Objects.requireNonNullElse(type, FolderType.PRIVATE);
    }

    public AccountTransfer getOwner() {
        return owner;
    }

    public void setOwner(AccountTransfer owner) {
        this.owner = owner;
    }

    public boolean isPropagatePermission() {
        return propagatePermission;
    }

    public void setPropagatePermission(boolean propagatePermission) {
        this.propagatePermission = propagatePermission;
    }

    public boolean isPublicReadAccess() {
        return publicReadAccess;
    }

    public void setPublicReadAccess(boolean publicReadAccess) {
        this.publicReadAccess = publicReadAccess;
    }

    public FolderDetails getParent() {
        return parent;
    }

    public void setParent(FolderDetails parent) {
        this.parent = parent;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public RegistryPartner getRemotePartner() {
        return remotePartner;
    }

    public void setRemotePartner(RegistryPartner remotePartner) {
        this.remotePartner = remotePartner;
    }

    public SampleRequest getSampleRequest() {
        return sampleRequest;
    }

    public void setSampleRequest(SampleRequest sampleRequest) {
        this.sampleRequest = sampleRequest;
    }
}
