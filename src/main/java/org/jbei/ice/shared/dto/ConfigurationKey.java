package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Set configuration types
 *
 * @author Hector Plahar
 */
public enum ConfigurationKey implements IsSerializable {
    PLASMID_STORAGE_ROOT(Type.PROPERTY, ""),
    STRAIN_STORAGE_ROOT(Type.PROPERTY, ""),
    PART_STORAGE_ROOT(Type.PROPERTY, ""),
    ARABIDOPSIS_STORAGE_ROOT(Type.PROPERTY, ""),
    PLASMID_STORAGE_DEFAULT(Type.PROPERTY, ""),
    STRAIN_STORAGE_DEFAULT(Type.PROPERTY, ""),
    PART_STORAGE_DEFAULT(Type.PROPERTY, ""),
    ARABIDOPSIS_STORAGE_DEFAULT(Type.PROPERTY, ""),
    DATABASE_SCHEMA_VERSION(Type.PROPERTY, ""),
    TEMPORARY_DIRECTORY(Type.PROPERTY, ""),
    DATA_DIRECTORY(Type.PROPERTY, ""),
    ATTACHMENTS_DIRECTORY(Type.PROPERTY, ""),
    TRACE_FILES_DIRECTORY(Type.PROPERTY, ""),
    BLAST_DIRECTORY(Type.PROPERTY, ""),
    SITE_SECRET(Type.PROPERTY, ""),
    SECRET_KEY(Type.PROPERTY, ""),
    COOKIE_NAME(Type.PROPERTY, ""),
    BULK_UPLOAD_APPROVER_EMAIL(Type.PROPERTY, ""),
    ADMIN_EMAIL(Type.PROPERTY, ""),
    SMTP_HOST(Type.PROPERTY, ""),
    ERROR_EMAIL_EXCEPTION_PREFIX(Type.PROPERTY, ""),
    SEND_EMAIL_ON_ERRORS(Type.PROPERTY, ""),
    PROJECT_NAME(Type.PROPERTY, ""),
    URI_PREFIX(Type.PROPERTY, ""),
    PART_NUMBER_PREFIX(Type.PROPERTY, ""),
    PART_NUMBER_DIGITAL_SUFFIX(Type.PROPERTY, ""),
    PART_NUMBER_DELIMITER(Type.PROPERTY, ""),
    WIKILINK_PREFIX(Type.PROPERTY, ""),
    BLAST_BLASTALL(Type.PROPERTY, ""),
    BLAST_BL2SEQ(Type.PROPERTY, ""),
    BLAST_FORMATDB(Type.PROPERTY, ""),
    BLAST_DATABASE_NAME(Type.PROPERTY, ""),
    AUTHENTICATION_BACKEND(Type.PROPERTY, "Authentication Backend"),
    LDAP_SEARCH_URL(Type.PROPERTY, ""),
    LDAP_AUTHENTICATION_URL(Type.PROPERTY, ""),
    LDAP_QUERY(Type.PROPERTY, ""),
    NEW_REGISTRATION_ALLOWED(Type.PROPERTY, ""),
    PASSWORD_CHANGE_ALLOWED(Type.PROPERTY, ""),
    PROFILE_EDIT_ALLOWED(Type.PROPERTY, "");

    private Type type;
    private String display;

    ConfigurationKey() {
    }

    ConfigurationKey(Type type, String display) {
        this.type = type;
        this.display = display;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.display;
    }

    ConfigurationKey stringToEnum(String value) {
        for (ConfigurationKey key : ConfigurationKey.values()) {
            if (key.name().equalsIgnoreCase(value))
                return key;
        }
        return null;
    }

    public enum Type implements IsSerializable {
        FILE, PROPERTY, DIRECTORY, BOOLEAN
    }
}
