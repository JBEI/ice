package org.jbei.ice.dto;

import org.jbei.ice.storage.IDataTransferModel;
import org.jbei.ice.storage.hibernate.DatabaseType;

/**
 * Storage DTO for configuration database
 *
 * @author Hector Plahar
 */
public class DataStorage implements IDataTransferModel {
    private DatabaseType type;
    private String connectionUrl;
    private String databaseName;
    private String databasePassword;
    private String databaseUser;
    private String folder;

    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
