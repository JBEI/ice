package org.jbei.ice.lib.dto;

import org.jbei.ice.lib.email.Type;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Types of configuration stored in the database. This acts as the base for
 * system configuration.
 *
 * @author Hector Plahar
 */
public enum ConfigurationKey implements IDataTransferModel {

    APPLICATION_VERSION("4.0.0"),
    TEMPORARY_DIRECTORY("/tmp"),
    DATA_DIRECTORY("data"),

    BULK_UPLOAD_APPROVER_EMAIL(""), //TODO this should be a role
    ADMIN_EMAIL(""),
    SMTP_HOST(""),
    ERROR_EMAIL_EXCEPTION_PREFIX("ERROR"),
    SEND_EMAIL_ON_ERRORS("NO"),
    PROJECT_NAME(""),
    URI_PREFIX("http://localhost:8443"),
    PART_NUMBER_PREFIX("TEST"),
    PART_NUMBER_DIGITAL_SUFFIX("000001"),
    PART_NUMBER_DELIMITER("_"),

    BLAST_INSTALL_DIR(""),

    NEW_REGISTRATION_ALLOWED("NO"),
    PASSWORD_CHANGE_ALLOWED("YES"),
    PROFILE_EDIT_ALLOWED("YES"),
    JOIN_WEB_OF_REGISTRIES("NO"),
    RESTRICT_PUBLIC_ENABLE("NO"),
    EMAILER(Type.CUSTOM.name()),
    GMAIL_APPLICATION_PASSWORD(""),
    WEB_OF_REGISTRIES_MASTER("registry.jbei.org");

    private String defaultValue;

    ConfigurationKey(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
