package org.jbei.ice.lib.shared.dto.group;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.user.User;

/**
 * DTO for groups
 *
 * @author Hector Plahar
 */

public class UserGroup implements IDTOModel {

    private long id;
    private String uuid;
    private String label;
    private long parentId;
    private String description;
    private ArrayList<UserGroup> children;
    private ArrayList<User> members;
    private long memberCount;
    private GroupType type;

    public UserGroup() {
        children = new ArrayList<UserGroup>();
        members = new ArrayList<User>();
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

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
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
