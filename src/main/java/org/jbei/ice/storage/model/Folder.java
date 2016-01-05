package org.jbei.ice.storage.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Encapsulates the notion of a collection of {@link Entry}s
 * Each folder has an owner.
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "folder")
@SequenceGenerator(name = "sequence", sequenceName = "folder_id_seq", allocationSize = 1)
public class Folder implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "owner_email", length = 255, nullable = false)
    private String ownerEmail;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private FolderType type;

    @Column(name = "propagate_permissions")
    private Boolean propagatePermissions = Boolean.FALSE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "folder_entry", joinColumns = {@JoinColumn(name = "folder_id", nullable = false)},
               inverseJoinColumns = {@JoinColumn(name = "entry_id", nullable = false)})
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Entry> contents = new LinkedHashSet<>();

    public Folder() {
    }

    public Folder(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public Set<Entry> getContents() {
        return contents;
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

    public FolderType getType() {
        return type;
    }

    public void setType(FolderType type) {
        this.type = type;
    }

    public boolean isPropagatePermissions() {
        if (propagatePermissions == null)
            return false;
        return propagatePermissions;
    }

    public void setPropagatePermissions(boolean propagatePermissions) {
        this.propagatePermissions = propagatePermissions;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    @Override
    public FolderDetails toDataTransferObject() {
        FolderDetails details = new FolderDetails(id, name);
        details.setType(type);
        details.setDescription(description);
        if (parent != null) {
            details.setParent(parent.toDataTransferObject());
        }
        if (getCreationTime() != null)
            details.setCreationTime(getCreationTime().getTime());
        details.setPropagatePermission(this.isPropagatePermissions());
        return details;
    }
}
