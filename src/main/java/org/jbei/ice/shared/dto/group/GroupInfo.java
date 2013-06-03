package org.jbei.ice.shared.dto.group;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.IDTOModel;

/**
 * DTO for groups
 *
 * @author Hector Plahar
 */

public class GroupInfo implements IDTOModel {

    private long id;
    private String uuid;
    private String label;
    private long parentId;
    private String description;
    private ArrayList<GroupInfo> children;
    private ArrayList<AccountInfo> members;
    private long memberCount;
    private GroupType type;

    public GroupInfo() {
        children = new ArrayList<GroupInfo>();
        members = new ArrayList<AccountInfo>();
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

    public ArrayList<GroupInfo> getChildren() {
        return this.children;
    }

    public ArrayList<AccountInfo> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<AccountInfo> members) {
        this.members = members;
        if (members == null)
            setMemberCount(0);
        else
            setMemberCount(members.size());
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
}
