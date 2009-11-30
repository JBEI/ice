package org.jbei.ice.lib.permissions;

import java.io.Serializable;

import org.jbei.ice.lib.models.Entry;

public class WorkSpaceItem implements Serializable {

	private static final long serialVersionUID = 1L;
	private Entry entry;
	private boolean starred;
	
	//getters and setters
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	public void setStarred(boolean starred) {
		this.starred = starred;
	}	
	
	public boolean getStarred() {
		return starred;
	}
	
}