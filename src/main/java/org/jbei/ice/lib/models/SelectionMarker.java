package org.jbei.ice.lib.models;

import org.jbei.ice.lib.value_objects.SelectionMarkerValueObject;

public class SelectionMarker implements SelectionMarkerValueObject {
	private int id;
	private String name;
	private Entry entry;

	public SelectionMarker() {
	}

	public SelectionMarker(String name, Entry entry) {
		this.name = name;
		this.entry = entry;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
}
