package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for groups
 *
 * @author Hector Plahar
 */

public class GroupInfo implements IsSerializable {

    private long id;
    private String uuid;
    private String label;
    private long parentId;
    private String description;

    public GroupInfo() {
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
}
