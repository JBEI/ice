package org.jbei.ice.storage.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.DataStorage;
import org.jbei.ice.logging.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Wrapper around a properties file for database
 *
 * @author Hector Plahar
 */
public class DatabaseProperties {

    private static final String URL = "connectionUrl";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String DATABASE_NAME = "dbName";
    private static final String CONNECTION_TYPE = "connectionType";
    private static final String SERVER_PROPERTY_NAME = "ice-server.properties";
    private static final String CONFIG_FOLDER = "config";


    public DatabaseProperties() {
    }

    /**
     * @param propertiesFile
     * @throws IllegalArgumentException
     */
    public DataStorage initialize(Path propertiesFile) {
        if (Files.notExists(propertiesFile))
            throw new IllegalArgumentException("Invalid properties path: " + propertiesFile);

        Properties properties = new Properties();
        Path serverPropertiesPath = Paths.get(propertiesFile.toString(), CONFIG_FOLDER, SERVER_PROPERTY_NAME);
        try {
            properties.load(new FileInputStream(serverPropertiesPath.toFile()));
            DataStorage dataStorage = new DataStorage();

            // check database type
            String dbTypeString = properties.getProperty(CONNECTION_TYPE);
            DatabaseType type;
            if (StringUtils.isBlank(dbTypeString)) {
                Logger.error("Property \"connectionType\" not found. Defaulting to value of " + DatabaseType.H2DB);
                type = DatabaseType.H2DB;
            } else {
                type = DatabaseType.valueOf(dbTypeString.toUpperCase());
            }
            dataStorage.setType(type);

            // connection properties
            dataStorage.setDatabasePassword(properties.getProperty(PASSWORD));
            dataStorage.setConnectionUrl(properties.getProperty(URL));
            dataStorage.setDatabaseName(properties.getProperty(DATABASE_NAME));
            dataStorage.setDatabaseUser(properties.getProperty(USERNAME));
            return dataStorage;
        } catch (IOException ioException) {
            throw new IllegalArgumentException(ioException.getMessage());
        }
    }
}
