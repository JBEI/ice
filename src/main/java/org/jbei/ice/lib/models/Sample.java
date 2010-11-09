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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.ISampleValueObject;

@Entity
@Table(name = "samples")
@SequenceGenerator(name = "sequence", sequenceName = "samples_id_seq", allocationSize = 1)
public class Sample implements ISampleValueObject, IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "uuid", length = 36)
    private String uuid;

    @Column(name = "depositor", length = 127)
    private String depositor;

    @Column(name = "label", length = 127)
    private String label;

    @Column(name = "notes")
    @Lob
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false, unique = false)
    private Entry entry;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "sample")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "samples_id")
    @OrderBy("id DESC")
    private Set<Location> locations = new LinkedHashSet<Location>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationNew locationNew;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getDepositor() {
        return depositor;
    }

    @Override
    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public Entry getEntry() {
        return entry;
    }

    @Override
    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public Date getModificationTime() {
        return modificationTime;
    }

    @Override
    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public LocationNew getLocationNew() {
        return locationNew;
    }

    public void setLocationNew(LocationNew locationNew) {
        this.locationNew = locationNew;
    }

}
