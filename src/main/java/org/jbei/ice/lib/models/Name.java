package org.jbei.ice.lib.models;

import org.jbei.ice.lib.value_objects.NameValueObject;

public class Name implements NameValueObject {
	private int id;
	private String name;
	private Entry entry;

	public Name() {
	}

	public Name(String name, Entry entry) {
		this.name = name;
		this.entry = entry;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
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
