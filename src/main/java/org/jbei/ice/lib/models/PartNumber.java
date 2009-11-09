package org.jbei.ice.lib.models;

import java.io.Serializable;

import org.jbei.ice.lib.value_objects.PartNumberValueObject;

public class PartNumber implements PartNumberValueObject, Serializable {
	private int id;
	private String partNumber;
	private Entry entry;

	public PartNumber() {
	}

	public PartNumber(String partNumber, Entry entry) {
		this.partNumber = partNumber;
		this.entry = entry;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
}
