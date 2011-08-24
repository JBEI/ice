package org.jbei.ice.client;

public enum EntryMenu {
	
	MINE("My Entries"), ALL("All Entries"), RECENTLY_VIEWED("Recently Viewed"), SAMPLES("Samples"), WORKSPACE("Workspace");
	
	private String display;
	
	EntryMenu(String display ) {
		this.display = display;
	}
	
	public String getDisplay() {
		return this.display;
	}
}
