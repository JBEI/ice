package org.jbei.ice.lib.utils;

import java.util.HashMap;

public class JbeirSettings {
    public final static HashMap<String, String> settings = new HashMap<String, String>();

    static {
        settings.put("DATA_DIRECTORY", "/tmp");
        settings.put("ATTACHMENTS_DIRECTORY", settings.get("DATA_DIRECTORY") + "/attachments");
        settings.put("LOGS_DIRECTORY", settings.get("DATA_DIRECTORY") + "/logs");
        settings.put("LUCENE_DIRECTORY", settings.get("DATA_DIRECTORY"));
        settings.put("BLAST_DIRECTORY", settings.get("DATA_DIRECTORY"));
        settings.put("SITE_SECRET", "Secret Sauce");
        settings.put("COOKIE_NAME", "gd-ice");
        settings.put("SEARCH_INDEX_FILE", "/tmp/ice_search_index");
        settings.put("ADMIN_EMAIL", "zdmytriv@lbl.gov");
        settings.put("MODERATOR_EMAIL", "zdmytriv@lbl.gov");
        settings.put("SMTP_HOST", "baracuda.dhcp.lbl.gov");
        settings.put("ERROR_EMAIL_EXCEPTION_PREFIX", "[ERROR] ");
        settings.put("PROJECT_NAME", "JBEI Registry");
        settings.put("SECRET_KEY", "o6-v(yay5w@0!64e6-+ylbhcd9g03rv#@ezqh7axchds=q=$n+");
        settings.put("PART_NUMBER_PREFIX", "TEST");
        settings.put("PART_NUMBER_DIGITAL_SUFFIX", "000001");
        settings.put("PART_NUMBER_DELIMITER", "_");
        settings.put("JOB_CUE_DELAY", "300000"); // 300000ms = 5 minutes"
        settings.put("SERVER_SUBDIR", "/"); // "/" for root

        // LocalBackend, NullAuthenticationBackend, LblLdapAuthenticationBackend are built-in
        settings.put("AUTHENTICATION_BACKEND",
                "org.jbei.ice.lib.authentication.LblLdapAuthenticationBackend");
    }

    public static String getSetting(String key) {
        String result = null;

        if (settings.containsKey(key)) {
            result = settings.get(key);
        } else {
            result = "";
        }

        return result;
    }
}
