package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Set configuration types
 *
 * @author Hector Plahar
 */
public enum ConfigurationKey implements IsSerializable {

    PLASMID_STORAGE_ROOT(Type.PROPERTY, Category.HIDDEN, ""),
    STRAIN_STORAGE_ROOT(Type.PROPERTY, Category.HIDDEN, ""),
    PART_STORAGE_ROOT(Type.PROPERTY, Category.HIDDEN, ""),
    ARABIDOPSIS_STORAGE_ROOT(Type.PROPERTY, Category.HIDDEN, ""),
    PLASMID_STORAGE_DEFAULT(Type.PROPERTY, Category.HIDDEN, ""),
    STRAIN_STORAGE_DEFAULT(Type.PROPERTY, Category.HIDDEN, ""),
    PART_STORAGE_DEFAULT(Type.PROPERTY, Category.HIDDEN, ""),
    ARABIDOPSIS_STORAGE_DEFAULT(Type.PROPERTY, Category.HIDDEN, ""),

    DATABASE_SCHEMA_VERSION(Type.PROPERTY, Category.HIDDEN, ""),
    TEMPORARY_DIRECTORY(Type.PROPERTY, Category.HIDDEN, ""),
    DATA_DIRECTORY(Type.PROPERTY, Category.HIDDEN, ""),
    ATTACHMENTS_DIRECTORY(Type.PROPERTY, Category.HIDDEN, ""),
    TRACE_FILES_DIRECTORY(Type.PROPERTY, Category.HIDDEN, ""),
    BLAST_DIRECTORY(Type.PROPERTY, Category.HIDDEN, ""),
    SITE_SECRET(Type.PROPERTY, Category.HIDDEN, ""),
    SECRET_KEY(Type.PROPERTY, Category.HIDDEN, ""),
    COOKIE_NAME(Type.PROPERTY, Category.HIDDEN, ""),
    BULK_UPLOAD_APPROVER_EMAIL(Type.PROPERTY, Category.HIDDEN, ""),
    ADMIN_EMAIL(Type.PROPERTY, Category.HIDDEN, ""),
    SMTP_HOST(Type.PROPERTY, Category.HIDDEN, ""),
    ERROR_EMAIL_EXCEPTION_PREFIX(Type.PROPERTY, Category.HIDDEN, ""),
    SEND_EMAIL_ON_ERRORS(Type.PROPERTY, Category.HIDDEN, ""),
    PROJECT_NAME(Type.PROPERTY, Category.HIDDEN, ""),
    URI_PREFIX(Type.PROPERTY, Category.HIDDEN, ""),
    PART_NUMBER_PREFIX(Type.PROPERTY, Category.HIDDEN, ""),
    PART_NUMBER_DIGITAL_SUFFIX(Type.PROPERTY, Category.HIDDEN, ""),
    PART_NUMBER_DELIMITER(Type.PROPERTY, Category.HIDDEN, ""),
    WIKILINK_PREFIX(Type.PROPERTY, Category.HIDDEN, ""),
    BLAST_BLASTALL(Type.PROPERTY, Category.HIDDEN, ""),
    BLAST_BL2SEQ(Type.PROPERTY, Category.HIDDEN, ""),
    BLAST_FORMATDB(Type.PROPERTY, Category.HIDDEN, ""),
    BLAST_DATABASE_NAME(Type.PROPERTY, Category.HIDDEN, ""),
    AUTHENTICATION_BACKEND(Type.PROPERTY, Category.HIDDEN, "Authentication Module"),
    LDAP_SEARCH_URL(Type.PROPERTY, Category.HIDDEN, ""),
    LDAP_AUTHENTICATION_URL(Type.PROPERTY, Category.HIDDEN, ""),
    LDAP_QUERY(Type.PROPERTY, Category.HIDDEN, ""),
    NEW_REGISTRATION_ALLOWED(Type.PROPERTY, Category.HIDDEN, ""),
    PASSWORD_CHANGE_ALLOWED(Type.PROPERTY, Category.HIDDEN, ""),
    PROFILE_EDIT_ALLOWED(Type.PROPERTY, Category.HIDDEN, "");

    private Type type;
    private String display;
    private Category category;

    ConfigurationKey() {
    }

    ConfigurationKey(Type type, Category category, String display) {
        this.type = type;
        this.display = display;
        this.category = category;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.display;
    }

    public Category getCategory() {
        return category;
    }

    public static ConfigurationKey stringToEnum(String value) {
        for (ConfigurationKey key : ConfigurationKey.values()) {
            if (key.name().equalsIgnoreCase(value))
                return key;
        }
        return null;
    }

    public enum Type implements IsSerializable {
        FILE, PROPERTY, DIRECTORY, BOOLEAN
    }

    public enum Category implements IsSerializable {
        HIDDEN, // not intended for display to the world
        BLAST
    }
}
