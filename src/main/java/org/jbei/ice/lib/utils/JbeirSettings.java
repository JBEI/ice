package org.jbei.ice.lib.utils;

import java.util.HashMap;

public class JbeirSettings {
	
	public String TEST_VARIABLE = "this is a test variable";
	
	public final static HashMap<String, Object> settings = new HashMap();
	static {
		//settings.put("TEST_VARIABLE", "test_variable");
		settings.put("DATA_DIRECTORY", "/var/lib/jbeiregistry");
		settings.put("ATTACHMENTS_DIRECTORY", settings.get("DATA_DIRECTORY") + "/attachments");
		settings.put("LOGS_DIRECTORY", settings.get("DATA_DIRECTORY") + "/logs");
		settings.put("LUCENE_DIRECTORY", settings.get("DATA_DIRECTORY"));
		settings.put("BLAST_DIRECTORY", settings.get("DATA_DIRECTORY"));
		
	}
		
	
	public static Object getSetting(String key) {
		Object result = null;
		if (settings.containsKey(key)) {
			result = settings.get(key);
		} else {
			result = "";
		}
		
		return result; 
			
	}
	
}
