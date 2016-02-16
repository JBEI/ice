package org.jbei.ice.lib.dto.group;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.web.RemoteUser;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * DTO for groups
 *
 * @author Hector Plahar
 */

public class UserGroup implements IDataTransferModel {

    private long id;
    private String uuid;
    private String label;
    private long parentId;
    private String description;
    private ArrayList<UserGroup> children;
    private ArrayList<AccountTransfer> members;
    private ArrayList<RemoteUser> remoteMembers;
    private long memberCount;
    private GroupType type;
    private String ownerEmail;
    private boolean autoJoin;
    private long creationTime;

    public UserGroup() {
        children = new ArrayList<>();
        members = new ArrayList<>();
        remoteMembers = new ArrayList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<UserGroup> getChildren() {
        return this.children;
    }

    public ArrayList<AccountTransfer> getMembers() {
        return members;
    }

    public ArrayList<RemoteUser> getRemoteMembers() {
        return remoteMembers;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public GroupType getType() {
        return type;
    }

    public void setType(GroupType type) {
        this.type = type;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
