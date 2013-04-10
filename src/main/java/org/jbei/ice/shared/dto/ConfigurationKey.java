package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Set configuration types
 *
 * @author Hector Plahar
 */
public enum ConfigurationKey implements IsSerializable {
    PLASMID_STORAGE_ROOT("", "", true),
    STRAIN_STORAGE_ROOT("", "", true),
    PART_STORAGE_ROOT("", "", true),
    ARABIDOPSIS_STORAGE_ROOT("", "", true),
    PLASMID_STORAGE_DEFAULT("", "", true),
    STRAIN_STORAGE_DEFAULT("", "", true),
    PART_STORAGE_DEFAULT("", "", true),
    ARABIDOPSIS_STORAGE_DEFAULT("", "", true),

    DATABASE_SCHEMA_VERSION("", "", false),
    TEMPORARY_DIRECTORY("", "temp directory", true),
    DATA_DIRECTORY("data", "data directory", true),
    ATTACHMENTS_DIRECTORY("data/attachments", "attachment directory", true),
    TRACE_FILES_DIRECTORY("data/traces", "sequence trace file directory", true),
    BLAST_DIRECTORY("blast", "blast directory", true),
    SITE_SECRET("Site Secret", "", false),
    // deprecated. use the user account salt instead.
    SECRET_KEY("o6-v(yay5w@0!64e6-+ylbhcd9g03rv#@ezqh7axchds=q=$n+", "", false),
    COOKIE_NAME("jbei-ice", "Cookie Name", true),
    BULK_UPLOAD_APPROVER_EMAIL("", "bulk upload approver email", true), //TODO this should be a role
    ADMIN_EMAIL("", "administrative email", true), // ditto
    SMTP_HOST("", "SMTP Host", true),
    ERROR_EMAIL_EXCEPTION_PREFIX("ERROR", "Error email title prefix", true),
    SEND_EMAIL_ON_ERRORS("NO", "send email on error", true),
    PROJECT_NAME("JBEI Registry", "Project Name", true),
    URI_PREFIX("localhost:8443", "", true),
    PART_NUMBER_PREFIX("TEST", "", true),
    PART_NUMBER_DIGITAL_SUFFIX("000001", "", true),
    PART_NUMBER_DELIMITER("_", "", true),
    WIKILINK_PREFIX("", "Wikilink Prefix", true),
    BLAST_BLASTALL("blast-2.2.26/bin/blastall", "", true),
    BLAST_BL2SEQ("blast-2.2.26/bin/bl2seq", "", true),
    BLAST_FORMATDB("blast-2.2.26/bin/formatdb", "", true),
    BLAST_DATABASE_NAME("jbeiblast", "", true),

    NEW_REGISTRATION_ALLOWED("NO", "Allow user registration", true),
    PASSWORD_CHANGE_ALLOWED("YES", "allow password change", true),
    PROFILE_EDIT_ALLOWED("YES", "Allow Profile Edit", true),
    WEB_PARTNERS("", "Web of Registry Partners", true),
    JOIN_WEB_OF_REGISTRIES("NO", "Join Web of Registries", true);

    private String displayName;
    private String defaultValue;
    private boolean editable;

    ConfigurationKey() {}

    ConfigurationKey(String defaultValue, String display, boolean isEditable) {
        this.displayName = display;
        this.defaultValue = defaultValue;
        this.editable = isEditable;
    }

    @Override
    public String toString() {
        if (displayName.isEmpty())
            return name();
        return displayName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isEditable() {
        return this.editable;
    }
}
