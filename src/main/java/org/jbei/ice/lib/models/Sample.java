package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;

import org.jbei.ice.lib.value_objects.ISampleValueObject;

public class Sample implements ISampleValueObject, Serializable {
	private int id;
	private String uuid;
	private String depositor;
	private String label;
	private String notes;
	private Entry entry;
	private Date creationTime;
	private Date modificationTime;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUuid() {
		return uuid;
	}
	public String getDepositor() {
		return depositor;
	}
	public void setDepositor(String depositor) {
		this.depositor = depositor;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
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
