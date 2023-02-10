package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.DataStorage;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.hibernate.DatabaseProperties;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for storage/database
 *
 * @author Hector Plahar
 */
public class StorageConfiguration {

    private final DatabaseProperties databaseProperties;
    private static final String ENV_DATA_HOME = "ICE_DATA_HOME";
    private static final String FOLDER_ICE_DEFAULT = ".ICEData";

    public StorageConfiguration() {
        databaseProperties = new DatabaseProperties();
    }

    // this should not create a home directory
    public Path initialize() {
        // get or initialize data directory
        Path dataDirectory = initializeDataDirectory();
        DataStorage storage = null;
        if (dataDirectory != null && Files.exists(dataDirectory)) {
            // initialize/load data properties
            storage = databaseProperties.initialize(dataDirectory);
        }

        // pass info on to hibernate to initialize data layer
        HibernateConfiguration.initialize(storage, dataDirectory);

        return dataDirectory;
    }

    public DataStorage get() {
        return null;
    }

    /**
     * Initializes the data directory location as follows:
     * 1. Checks for value of environment variable <code>ICE_DATA_HOME</code>. If set, then value is used as home dir
     * 2. If environment variable is not set, the home directory is used and a folder name <code>.ICEData</code>
     * is created and used as the home directory
     *
     * @return the resolved data directory/path or null
     */
    private Path initializeDataDirectory() {
        // check environ variable
        String propertyHome = System.getenv(ENV_DATA_HOME);
        Path iceHome;

        if (StringUtils.isBlank(propertyHome)) {
            // check system property (-D in startup script)
            propertyHome = System.getProperty(ENV_DATA_HOME);

            // still nothing, check home directory
            if (StringUtils.isNotEmpty(propertyHome)) {
                iceHome = Paths.get(propertyHome);
            } else {
                Logger.info("No ICE data directory set");
                return null;
            }
        } else {
            iceHome = Paths.get(propertyHome);
        }

        Logger.info("Using ICE data directory: " + iceHome);
        return iceHome;
    }
}
