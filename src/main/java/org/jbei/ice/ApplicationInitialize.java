package org.jbei.ice;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AdminAccount;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationSettings;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.sequence.annotation.AutoAnnotationBlastDbBuildTask;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.DbType;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.hibernate.dao.ConfigurationDAO;
import org.jbei.ice.storage.model.ConfigurationModel;

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

    private static final String SERVER_PROPERTY_NAME = "ice-server.properties";

    /**
     * Responsible for initializing the system and checking for the existence of needed
     * data (such as settings) and creating as needed
     */
    public static Path configure() {
        try {
            // get or initialize data directory
            Path dataDirectory = initializeDataDirectory();
            if (dataDirectory == null || !Files.exists(dataDirectory))
                return null;

            // check if there is a ice-server.properties in the config directory inside the data directory
            // and use it to connect to the database if so
            if (Files.exists(Paths.get(dataDirectory.toString(), "config", SERVER_PROPERTY_NAME))) {
                loadServerProperties(dataDirectory);
            } else {
                // todo : do not initialize hibernate unless "ice-server.properties is available
                // todo : this will force a redirect on the ui and let user create/select a supported database

                // unless there is already a db directory (check if using for built-in database)
                // since using the built in doesn't require a config file
                Path dbFolder = Paths.get(dataDirectory.toString(), "db");
                if (Files.exists(dbFolder) && Files.isDirectory(dbFolder)) {
                    // check if there is a database with the name ice-h2db
                    Iterator<Path> files = Files.list(dbFolder).iterator();
                    if (files.hasNext()) {
                        Path file = files.next();
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".db") && fileName.startsWith("ice-h2db")) {
                            HibernateConfiguration.initialize(DbType.H2DB, null, dataDirectory);
                            return dataDirectory;
                        } else {
                            // db folder located but no database
                            return null;
                        }
                    }
                } else if (Files.isWritable(dataDirectory)) {
                    // create the db directory for the file database
                    Files.createDirectory(dbFolder);
                    // todo : create database config file template
                    // todo : create ldap-config.properties.template
                    HibernateConfiguration.initialize(DbType.H2DB, null, dataDirectory); // this creates a directory "/db"
                }
            }

            return dataDirectory;
        } catch (Exception e) {
            Logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public static void loadAuthentication() {

    }

    public static void start(Path dataDirectory) {

        IceExecutorService.getInstance().startService();

        // check for and create public group
        GroupController groupController = new GroupController();
        groupController.createOrRetrievePublicGroup();

        // check for and create admin account
        AdminAccount adminAccount = new AdminAccount();
        adminAccount.resetPassword();

        // check for and create default settings
        ConfigurationSettings settings = new ConfigurationSettings();
        settings.initPropertyValues();

        // check data directory
        checkDataDirectory(dataDirectory);

        try {
            // check blast database exists and build if it doesn't
            RebuildBlastIndexTask task = new RebuildBlastIndexTask();
            IceExecutorService.getInstance().runTask(task);

            AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask();
            IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private static void checkDataDirectory(Path dataDirectory) {
        ConfigurationKey dataKey = ConfigurationKey.DATA_DIRECTORY;
        ConfigurationDAO dao = DAOFactory.getConfigurationDAO();

        ConfigurationModel model = dao.get(dataKey);
        if (model != null && !StringUtils.isEmpty(model.getValue()))
            return;

        model = new ConfigurationModel();
        model.setKey(dataKey.name());
        model.setValue(dataDirectory.toString());
        dao.create(model);
    }

    // this should not create a home directory
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

        Logger.info("Using ICE data directory: " + iceHome.toString());
        return iceHome;
    }

    // initialize the database using the database configuration
    private static void loadServerProperties(Path dataDirectory) throws IOException {
        Path serverPropertiesPath = Paths.get(dataDirectory.toString(), "config", SERVER_PROPERTY_NAME);
        Properties properties = new Properties();
        properties.load(new FileInputStream(serverPropertiesPath.toFile()));

        // get type of data base
        String dbTypeString = properties.getProperty("connectionType");
        DbType type = DbType.valueOf(dbTypeString.toUpperCase());

        // get type of database etc
        HibernateConfiguration.initialize(type, properties, dataDirectory);
    }
}
