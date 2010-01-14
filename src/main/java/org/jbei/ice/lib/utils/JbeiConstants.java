package org.jbei.ice.lib.utils;

public class JbeiConstants {
	public final static String getVisibility(Integer id) {
		String result = "";
		if (id > 8) {
			result = "Public";
		} else if (id > 4 ) {
			result = "Hidden";
		} else if (id >= 0 ) {
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
	
	public final static String getPackageFormat(String key) {
		String result = "";
		if (key.equals("biobricka")) {
			result = "Biobrick A";
		} else if (key.equals("biobrickb")) {
			result = "BioBrick Berkeley";
		} else if (key.equals("")) {
			result = "None";
		} else {
			result = "Unrecognized Format";
		}
		return result;
	}
	
	public final static String getRecordType(String key) {
		String result = "";
		if (key.equals("part")) {
			result = "Part";
		} else if (key.equals("plasmid")) {
			result = "Plasmid";
		} else if (key.equals("strain")) {
			result = "Strain";
		}
		return result;
	}
}
