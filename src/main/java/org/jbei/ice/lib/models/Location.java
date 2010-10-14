package org.jbei.ice.lib.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.ILocationValueObject;

@Entity
@Table(name = "locations")
@SequenceGenerator(name = "sequence", sequenceName = "locations_id_seq", allocationSize = 1)
public class Location implements ILocationValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "samples_id", nullable = false, unique = false)
    private Sample sample;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "barcode", length = 255)
    private String barcode;

    @Column(name = "notes")
    @Lob
    private String notes;

    @Column(name = "wells", length = 255)
    private String wells;

    @Column(name = "n_columns")
    private int nColumns;

    @Column(name = "n_rows")
    private int nRows;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Sample getSample() {
        return sample;
    }

    @Override
    public void setSample(Sample sample) {
        this.sample = sample;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getBarcode() {
        return barcode;
    }

    @Override
    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
    public String getWells() {
        return wells;
    }

    @Override
    public void setWells(String wells) {
        this.wells = wells;
    }

    @Override
    public int getnColumns() {
        return nColumns;
    }

    @Override
    public void setnColumns(int nColumns) {
        this.nColumns = nColumns;
    }

    @Override
    public int getnRows() {
        return nRows;
    }

    @Override
    public void setnRows(int nRows) {
        this.nRows = nRows;
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

    public String toOneLineString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getLocation()).append(",");
        sb.append(getnColumns()).append("x").append(getnRows()).append(",");
        sb.append(getWells()).append(",");
        sb.append(getNotes());

        return sb.toString();
    }
}
