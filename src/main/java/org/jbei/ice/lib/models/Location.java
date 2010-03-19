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
import org.jbei.ice.lib.vo.ILocationValueObject;

@Entity
@Table(name = "locations")
@SequenceGenerator(name = "sequence", sequenceName = "locations_id_seq", allocationSize = 1)
public class Location implements ILocationValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWells() {
        return wells;
    }

    public void setWells(String wells) {
        this.wells = wells;
    }

    public int getnColumns() {
        return nColumns;
    }

    public void setnColumns(int nColumns) {
        this.nColumns = nColumns;
    }

    public int getnRows() {
        return nRows;
    }

    public void setnRows(int nRows) {
        this.nRows = nRows;
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

    public String toOneLineString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getLocation()).append(",");
        sb.append(getnColumns()).append("x").append(getnRows()).append(",");
        sb.append(getWells()).append(",");
        sb.append(getNotes());

        return sb.toString();
    }
}
