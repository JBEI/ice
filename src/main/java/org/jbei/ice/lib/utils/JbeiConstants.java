package org.jbei.ice.lib.utils;

public class JbeiConstants {
	public final static String getVisibility(Integer id) {
		String result = "";
		if (id > 8) {
			result = "Public";
		} else if (id > 4 ) {
			result = "Hidden";
		} else if (id > 1 ) {
			result = "Private";
		}
		return result;
	}
	
	public final static String getStatus(String key) {
		String result = "";
		if (key.equals("complete")) {
			result = "Complete";
		} else if (key.equals("in progress")) {
			result = "In Progress";
		} else if (key.equals("planned")) {
			result = "Planned";
		}
		return result;
	}
}
