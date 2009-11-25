package org.jbei.ice.lib.permissions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class WorkSpace implements Serializable {

	private static final long serialVersionUID = 1L;

	private LinkedHashMap<String, HashMap<String, Object>> entries;
	/* This linked hashmap uses recordId as keys.
	 * 
	 * Currently known hashmap fields:
	 * "starred": Boolean
	 */

	public void setEntriesMap(LinkedHashMap<String, HashMap<String, Object>> entriesMap) {
		this.entries = entriesMap;
	}

	public LinkedHashMap<String, HashMap<String, Object>> getEntriesMap() {
		return entries;
	}
	
	
}
