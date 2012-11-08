package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Set configuration types
 *
 * @author Hector Plahar
 */
public enum ConfigurationKey implements IsSerializable {

    PLASMID_STORAGE_ROOT(""),
    STRAIN_STORAGE_ROOT(""),
    PART_STORAGE_ROOT(""),
    ARABIDOPSIS_STORAGE_ROOT(""),
    PLASMID_STORAGE_DEFAULT(""),
    STRAIN_STORAGE_DEFAULT(""),
    PART_STORAGE_DEFAULT(""),
    ARABIDOPSIS_STORAGE_DEFAULT(""),

    DATABASE_SCHEMA_VERSION(""),
    TEMPORARY_DIRECTORY(""),
    DATA_DIRECTORY(""),
    ATTACHMENTS_DIRECTORY("/tmp/attachments"),
    TRACE_FILES_DIRECTORY("/tmp/traces"),
    BLAST_DIRECTORY("/tmp/blast"),
    SITE_SECRET("Site Secret"),
    SECRET_KEY("o6-v(yay5w@0!64e6-+ylbhcd9g03rv#@ezqh7axchds=q=$n+"),
    COOKIE_NAME("jbei-ice"),
    BULK_UPLOAD_APPROVER_EMAIL(""),
    ADMIN_EMAIL(""),
    SMTP_HOST("SMTP Host"),
    ERROR_EMAIL_EXCEPTION_PREFIX("ERROR"),
    SEND_EMAIL_ON_ERRORS("NO"),
    PROJECT_NAME(""),
    URI_PREFIX(""),
    PART_NUMBER_PREFIX("TEST"),
    PART_NUMBER_DIGITAL_SUFFIX("000001"),
    PART_NUMBER_DELIMITER("_"),
    WIKILINK_PREFIX(""),
    BLAST_BLASTALL(""),
    BLAST_BL2SEQ(""),
    BLAST_FORMATDB(""),
    BLAST_DATABASE_NAME(""),
    AUTHENTICATION_BACKEND("Authentication Module"),
    LDAP_SEARCH_URL(""),
    LDAP_AUTHENTICATION_URL(""),
    LDAP_QUERY(""),
    NEW_REGISTRATION_ALLOWED(""),
    PASSWORD_CHANGE_ALLOWED(""),
    PROFILE_EDIT_ALLOWED(""),
    WEB_PARTNERS("Web of Registry Partners");

    private String defaultValue;

    ConfigurationKey() {
    }

    ConfigurationKey(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static ConfigurationKey stringToEnum(String value) {
        for (ConfigurationKey key : ConfigurationKey.values()) {
            if (key.name().equalsIgnoreCase(value))
                return key;
        }
        return null;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
