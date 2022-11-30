package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.config.ConfigurationSettings;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.DataStorage;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.hibernate.DbType;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class StorageConfiguration {

    private final String userId;
    private static final String SERVER_PROPERTY_NAME = "ice-server.properties";

    public StorageConfiguration(String userId) {
        this.userId = userId;
    }

    public DataStorage get() {


        // database properties
        Path path = initializeDataDirectory();
        if (path == null)
            return null;
        try {
            DataStorage storage = loadServerProperties(path);
            String dataDir = new ConfigurationSettings().getPropertyValue(ConfigurationKey.DATA_DIRECTORY);
            storage.setFolder(dataDir);
            return storage;
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

    }

    private DataStorage loadServerProperties(Path dataDirectory) throws IOException {
        Path serverPropertiesPath = Paths.get(dataDirectory.toString(), "config", SERVER_PROPERTY_NAME);
        Properties properties = new Properties();
        properties.load(new FileInputStream(serverPropertiesPath.toFile()));

        DataStorage storage = new DataStorage();

        String password = properties.getProperty("password");
        String databaseName = properties.getProperty("dbName");
        String connectionUrl = properties.getProperty("connectionUrl");

        // type of database
        String dbTypeString = properties.getProperty("connectionType");
        DbType type;
        if (StringUtils.isBlank(dbTypeString)) {
            Logger.error("Property \"connectionType\" not found. Defaulting to value of " + DbType.POSTGRESQL);
            type = DbType.POSTGRESQL;
        } else {
            type = DbType.valueOf(dbTypeString.toUpperCase());
        }
        String username = properties.getProperty("username");

        storage.setDatabasePassword(password);
        storage.setType(type);
        storage.setConnectionUrl(connectionUrl);
        storage.setDatabaseName(databaseName);
        storage.setDatabaseUser(username);

        return storage;
    }

    private static Path initializeDataDirectory() {
        // check environ variable
        String propertyHome = System.getenv("ICE_DATA_HOME");
        Path iceHome;

        if (StringUtils.isBlank(propertyHome)) {
            // check system property (-D in startup script)
            propertyHome = System.getProperty("ICE_DATA_HOME");

            // still nothing, check home directory
            if (StringUtils.isBlank(propertyHome)) {
                String userHome = System.getProperty("user.home");
                iceHome = Paths.get(userHome, ".ICEData");
                if (!Files.exists(iceHome)) {
                    // create home directory
                    try {
                        if (Files.isWritable(Paths.get(userHome)))
                            return Files.createDirectory(iceHome);
                        else
                            return null;
                    } catch (IOException e) {
                        Logger.error(e);
                        return null;
                    }
                }
            } else {
                iceHome = Paths.get(propertyHome);
            }
        } else {
            iceHome = Paths.get(propertyHome);
        }

        Logger.info("Using ICE data directory: " + iceHome);
        return iceHome;
    }
}
