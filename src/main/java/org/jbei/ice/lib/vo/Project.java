package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * Value object to store user projects
 * 
 * @author Zinovii Dmytriv
 * 
 */
public abstract class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String uuid;
    private String ownerName;
    private String ownerEmail;
    private Date creationTime;
    private Date modificationTime;

    public Project() {
        super();
    }

    public Project(String name, String description, String uuid, String ownerEmail,
            String ownerName, Date creationTime, Date modificationTime) {
        super();

        this.name = name;
        this.description = description;
        this.uuid = uuid;
        this.ownerEmail = ownerEmail;
        this.ownerName = ownerName;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    public abstract String typeName();
}
