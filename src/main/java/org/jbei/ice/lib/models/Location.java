package org.jbei.ice.lib.models;

import java.util.Date;

import org.jbei.ice.lib.value_objects.LocationValueObject;

public class Location implements LocationValueObject {
	
	private int id;
	private Sample sample;
	private String location;
	private String barcode;
	private String notes;
	private String wells;
	private int nColumns;
	private int nRows;
	private Date creationTime;
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
	
	
}
