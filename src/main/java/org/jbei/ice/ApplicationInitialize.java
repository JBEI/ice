package org.jbei.ice;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.hibernate.DbType;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

/**
 * Responsible for initializing the ICE application
 * on startup
 *
 * @author Hector Plahar
 */
public class ApplicationInitialize {

    /**
     * Responsible for initializing the system and checking for the existence of needed
     * data (such as settings) and creating as needed
     */
    public static void startUp() {
        try {
            // get or initialize data directory
            Path dataDirectory = initializeDataDirectory();
            if (dataDirectory == null || !Files.exists(dataDirectory))
                return;

            // check if there is a ice-server.properties
            if (Files.exists(Paths.get(dataDirectory.toString(), "ice-server.properties"))) {
                // parse
                Path serverPropertiesPath = Paths.get(dataDirectory.toString(), "ice-server.properties");
                Properties properties = new Properties();
                properties.load(new FileInputStream(serverPropertiesPath.toFile()));

                // get type of database etc
                HibernateConfiguration.initialize(DbType.H2DB, properties, dataDirectory);
            } else {
                // todo : do not initialize hibernate unless "ice-server.properties is available
                // todo : this will force a redirect on the ui and let user create/select a supported database

                // unless there is already a db directory (check if using for built-in database)
                Path dbFolder = Paths.get(dataDirectory.toString(), "db");
                if (Files.exists(dbFolder) && Files.isDirectory(dbFolder)) {
                    // check if there is a database with the name ice-h2db
                    Iterator<Path> files = Files.list(dbFolder).iterator();
                    while (files.hasNext()) {
                        Path file = files.next();
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".db") && fileName.startsWith("ice-h2db")) {
                            HibernateConfiguration.initialize(DbType.H2DB, null, dataDirectory);
                            break;
                        }
                    }
                }


            }
        } catch (Exception e) {
            Logger.error(e);
            throw new RuntimeException(e);
        }

//        IceExecutorService.getInstance().startService();
//
//        // check for and create public group
//        GroupController groupController = new GroupController();
//        groupController.createOrRetrievePublicGroup();
//
//        // check for and create admin account
//        AccountController accountController = new AccountController();
//        accountController.createAdminAccount();
//
//        // check for and create default settings
//        ConfigurationSettings settings = new ConfigurationSettings();
//        settings.initPropertyValues();
//
//        try {
//            // check blast database exists and build if it doesn't
//            RebuildBlastIndexTask task = new RebuildBlastIndexTask();
//            IceExecutorService.getInstance().runTask(task);
//
//            AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask();
//            IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
//        } catch (Exception e) {
//            Logger.error(e);
//        }
    }

    // this should not create a home directory
    private static Path initializeDataDirectory() throws IOException {
        // check environ variable
        String propertyHome = System.getenv("ICE_DATA_PATH");
        Path iceHome;

        if (StringUtils.isBlank(propertyHome)) {
            // check system property (-D in startup script)
            propertyHome = System.getProperty("ICE_DATA_PATH");

            // still nothing, check home directory
            if (StringUtils.isBlank(propertyHome)) {
                String userHome = System.getProperty("user.home");
                iceHome = Paths.get(userHome, ".ICEData");
                if (!Files.exists(iceHome)) {
                    // create home directory
                    return null;
                }
            } else {
                iceHome = Paths.get(propertyHome);
            }
        } else {
            iceHome = Paths.get(propertyHome);
        }

        Logger.info("Using ICE data directory: " + iceHome.toString());
        return iceHome;
    }
}
