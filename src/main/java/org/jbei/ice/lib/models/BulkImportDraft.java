package org.jbei.ice.lib.models;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "bulk_import_draft")
@SequenceGenerator(name = "sequence", sequenceName = "bulk_import_draft_id_seq", allocationSize = 1)
public class BulkImportDraft implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_email", length = 255, nullable = false)
    private String ownerEmail;

    @Column(name = "type")
    private String type; // TODO : enum?

    @Column(name = "creation_time")
    private Date creationTime;

    @Column(name = "last_modified_time")
    private Date lastModifiedTime;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "bulk_import_draft_entry", joinColumns = { @JoinColumn(name = "bulk_import_draft_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "entry_id", nullable = false) })
    private Set<Entry> contents = new LinkedHashSet<Entry>();

    public BulkImportDraft() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Set<Entry> getContents() {
        return contents;
    }

    public void setContents(Set<Entry> contents) {
        this.contents = contents;
    }
}
