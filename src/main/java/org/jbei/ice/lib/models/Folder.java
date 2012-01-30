package org.jbei.ice.lib.models;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jbei.ice.lib.dao.IModel;

/**
 * Encapsulates the notion of a collection of {@link org.jbei.ice.lib.models.Entry}s
 * Each folder has an owner.
 * 
 * @author Hector Plahar
 */
@Entity
@Table(name = "folder")
@SequenceGenerator(name = "sequence", sequenceName = "folder_id_seq", allocationSize = 1)
public class Folder implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
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

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "folder_entry", joinColumns = { @JoinColumn(name = "folder_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "entry_id", nullable = false) })
    private Set<Entry> contents = new LinkedHashSet<Entry>();

    public Folder() {
    }

    public Folder(String name) {
        this.name = name;
    }

    public Folder(String name, Set<Entry> contents) {
        this.name = name;
        this.contents = contents;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
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

    public void setContents(Set<Entry> contents) {
        this.contents = contents;
    }

    public long getId() {
        return id;
    }

    public Folder getParent() {
        return parent;
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
}
