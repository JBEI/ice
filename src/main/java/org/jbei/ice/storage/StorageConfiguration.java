package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.DataStorage;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.hibernate.DatabaseProperties;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

import java.io.IOException;
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

    public StorageConfiguration() {
        databaseProperties = new DatabaseProperties();
    }

    // this should not create a home directory
    public Path initialize() {
        // get or initialize data directory
        Path dataDirectory = initializeDataDirectory();
        if (dataDirectory == null || Files.notExists(dataDirectory))
            return null;

        // initialize/load data properties
        DataStorage storage = databaseProperties.initialize(dataDirectory);

        // pass info on to hibernate
        HibernateConfiguration.initialize(storage, dataDirectory);

        return dataDirectory;
    }

    public DataStorage get() {
        return null;
    }

    private Path initializeDataDirectory() {
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
