package org.jbei.ice.lib.models;

import java.io.Serializable;

public class EntryLabel implements Serializable {
	private int id;
	private Label label;
	private Entry entry;
	
	public int getId() {
		return id;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
	public Label getLabel() {
		return label;
	}public void setId(int id) {
		this.id = id;
	}
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	 
}
