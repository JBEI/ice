package org.jbei.ice.lib.shared.dto;

/**
 * Types of configuration stored in the database
 *
 * @author Hector Plahar
 */
public enum ConfigurationKey implements IDTOModel {

    PLASMID_STORAGE_ROOT("", "", true),
    STRAIN_STORAGE_ROOT("", "", true),
    PART_STORAGE_ROOT("", "", true),
    ARABIDOPSIS_STORAGE_ROOT("", "", true),
    PLASMID_STORAGE_DEFAULT("", "", true),
    STRAIN_STORAGE_DEFAULT("", "", true),
    PART_STORAGE_DEFAULT("", "", true),
    ARABIDOPSIS_STORAGE_DEFAULT("", "", true),

    DATABASE_SCHEMA_VERSION("", "", false),
    TEMPORARY_DIRECTORY("/tmp", "temp directory", true),
    DATA_DIRECTORY("data", "data directory", true),

    SITE_SECRET("Site Secret", "", false),
    // deprecated. use the user account salt instead.
    SECRET_KEY("o6-v(yay5w@0!64e6-+ylbhcd9g03rv#@ezqh7axchds=q=$n+", "", false),
    BULK_UPLOAD_APPROVER_EMAIL("", "bulk upload approver email", true), //TODO this should be a role
    ADMIN_EMAIL("", "administrative email", true), // ditto
    SMTP_HOST("", "SMTP Host", true),
    ERROR_EMAIL_EXCEPTION_PREFIX("ERROR", "Error email title prefix", true),
    SEND_EMAIL_ON_ERRORS("NO", "send email on error", true),
    PROJECT_NAME("JBEI Registry", "Registry Name", true),
    URI_PREFIX("localhost:8443", "", false),
    PART_NUMBER_PREFIX("TEST", "", true),
    PART_NUMBER_DIGITAL_SUFFIX("000001", "", true),
    PART_NUMBER_DELIMITER("_", "", true),
    WIKILINK_PREFIX("jbei", "Wikilink Prefix", true),

    BLAST_INSTALL_DIR("blast-2.2.26/bin", "Blast installation", true),

    NEW_REGISTRATION_ALLOWED("NO", "Allow user registration", true),
    PASSWORD_CHANGE_ALLOWED("YES", "allow password change", true),
    PROFILE_EDIT_ALLOWED("YES", "Allow Profile Edit", true),
    JOIN_WEB_OF_REGISTRIES("NO", "Join Web of Registries", true);

    private String displayName;
    private String defaultValue;
    private boolean editable;

    ConfigurationKey() {
    }

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
