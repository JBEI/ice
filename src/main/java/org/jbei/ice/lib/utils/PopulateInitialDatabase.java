package org.jbei.ice.lib.utils;

import java.util.ArrayList;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.config.ConfigurationDAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;

/**
 * Populate an empty database with necessary objects and values.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class PopulateInitialDatabase {
    public static final String DEFAULT_PLASMID_STORAGE_SCHEME_NAME = "Plasmid Storage (Default)";
    public static final String DEFAULT_STRAIN_STORAGE_SCHEME_NAME = "Strain Storage (Default)";
    public static final String DEFAULT_PART_STORAGE_SCHEME_NAME = "Part Storage (Default)";
    public static final String DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME = "Arabidopsis Storage (Default)";

    // Database schema version.
    // If you are extending the existing schema to suit your needs, we suggest using the
    // naming scheme "custom-[your institute]-[your version]", as
    // the system will try to upgrade schemas of known older versions.
    // Setting the correct parent schema version may help you in the future.
    public static final String DATABASE_SCHEMA_VERSION = "3.3.0";
    public static final String PARENT_DATABASE_SCHEMA_VERSION = "3.1.0";

    // This is a global "everyone" uuid
    public static String everyoneGroup = "8746a64b-abd5-4838-a332-02c356bbeac0";

    /**
     * Populate an empty database with necessary objects and values.
     * <p/>
     * <ul> <li>Create the everyone group.</li> <li>Create the System account.</li> <li>Create the Admin account.</li>
     * <li>Create default storage schemes.</li> <li>Update the database schema, if necessary.</li> </ul>
     *
     * @throws UtilityException
     */
    public static void initializeDatabase() throws UtilityException {
        GroupController groupController = new GroupController();
        Group group1;
        try {
            group1 = groupController.getGroupByUUID(everyoneGroup);
            if (group1 == null) {
                // Since everyone group doesn't exist, assume database is new
                // Put all other db initialization below.
                groupController.createOrRetrievePublicGroup();

                createSystemAccount();
                createAdminAccount();

                populateDefaultStorageLocationsAndSchemes();
            }
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Create default root node Storage for each part types
     */
    private static void populateDefaultStorageLocationsAndSchemes() throws UtilityException {
        ConfigurationDAO dao = new ConfigurationDAO();
        Configuration strainRootConfig;
        Configuration plasmidRootConfig;
        Configuration partRootConfig;
        Configuration arabidopsisRootConfig;
        Storage strainRoot;
        Storage plasmidRoot;
        Storage partRoot;
        Storage arabidopsisSeedRoot;

        try {
            // read configuration
            strainRootConfig = dao.get(ConfigurationKey.STRAIN_STORAGE_ROOT);
            plasmidRootConfig = dao.get(ConfigurationKey.PLASMID_STORAGE_ROOT);
            partRootConfig = dao.get(ConfigurationKey.PART_STORAGE_ROOT);
            arabidopsisRootConfig = dao.get(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT);
        } catch (DAOException e1) {
            throw new UtilityException(e1);
        }

        // if null, create root storage and config for entry types
        StorageController storageController = new StorageController();
        try {
            if (strainRootConfig == null) {
                strainRoot = new Storage("Strain Storage Root", "Default Strain Storage Root",
                                         StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
                strainRoot = storageController.save(strainRoot);
                strainRootConfig = new Configuration(ConfigurationKey.STRAIN_STORAGE_ROOT, strainRoot.getUuid());
                dao.save(strainRootConfig);

                Storage defaultStrain = new Storage(DEFAULT_STRAIN_STORAGE_SCHEME_NAME,
                                                    DEFAULT_STRAIN_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                                                    AccountController.SYSTEM_ACCOUNT_EMAIL, strainRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultStrain.setSchemes(schemes);
                defaultStrain = storageController.save(defaultStrain);
                dao.save(new Configuration(
                        ConfigurationKey.STRAIN_STORAGE_DEFAULT, defaultStrain.getUuid()));
            }

            if (plasmidRootConfig == null) {
                plasmidRoot = new Storage("Plasmid Storage Root", "Default Plasmid Storage Root",
                                          StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
                plasmidRoot = storageController.save(plasmidRoot);
                plasmidRootConfig = new Configuration(ConfigurationKey.PLASMID_STORAGE_ROOT,
                                                      plasmidRoot.getUuid());
                dao.save(plasmidRootConfig);

                Storage defaultPlasmid = new Storage(DEFAULT_PLASMID_STORAGE_SCHEME_NAME,
                                                     DEFAULT_PLASMID_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                                                     AccountController.SYSTEM_ACCOUNT_EMAIL, plasmidRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultPlasmid.setSchemes(schemes);
                defaultPlasmid = storageController.save(defaultPlasmid);
                dao.save(new Configuration(
                        ConfigurationKey.PLASMID_STORAGE_DEFAULT, defaultPlasmid.getUuid()));
            }

            if (partRootConfig == null) {
                partRoot = new Storage("Part Storage Root", "Default Part Storage Root",
                                       StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
                partRoot = storageController.save(partRoot);
                partRoot = storageController.save(partRoot);
                partRootConfig = new Configuration(ConfigurationKey.PART_STORAGE_ROOT,
                                                   partRoot.getUuid());
                dao.save(partRootConfig);

                Storage defaultPart = new Storage(DEFAULT_PART_STORAGE_SCHEME_NAME,
                                                  DEFAULT_PART_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                                                  AccountController.SYSTEM_ACCOUNT_EMAIL, partRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultPart.setSchemes(schemes);
                defaultPart = storageController.save(defaultPart);
                dao.save(new Configuration(ConfigurationKey.PART_STORAGE_DEFAULT,
                                           defaultPart.getUuid()));
            }
            if (arabidopsisRootConfig == null) {
                arabidopsisSeedRoot = new Storage("Arabidopsis Storage Root",
                                                  "Default Arabidopsis Seed Storage Root", StorageType.GENERIC,
                                                  AccountController.SYSTEM_ACCOUNT_EMAIL, null);
                arabidopsisSeedRoot = storageController.save(arabidopsisSeedRoot);
                arabidopsisRootConfig = new Configuration(
                        ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT, arabidopsisSeedRoot.getUuid());
                dao.save(arabidopsisRootConfig);

                Storage defaultArabidopsis = new Storage(DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME,
                                                         DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                                                         AccountController.SYSTEM_ACCOUNT_EMAIL, arabidopsisSeedRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultArabidopsis.setSchemes(schemes);
                defaultArabidopsis = storageController.save(defaultArabidopsis);
                dao.save(new Configuration(ConfigurationKey.ARABIDOPSIS_STORAGE_DEFAULT, defaultArabidopsis.getUuid()));
            }
        } catch (DAOException e) {
            throw new UtilityException(e);
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Update the database schema.
     *
     * @throws UtilityException
     */
//    private static void updateDatabaseSchema(ConfigurationDAO dao) throws UtilityException {
//        Configuration databaseSchema;
//
//        try {
//            databaseSchema = dao.get(ConfigurationKey.DATABASE_SCHEMA_VERSION);
//            if (databaseSchema == null) {
//                databaseSchema = new Configuration(ConfigurationKey.DATABASE_SCHEMA_VERSION, DATABASE_SCHEMA_VERSION);
//                dao.save(databaseSchema);
//            }
//
//            // TODO
//            if (databaseSchema.getValue().equals(PARENT_DATABASE_SCHEMA_VERSION)) {
//                // do schema upgrade from version 3.0 to 3.1, does not capture upgrading from ice2 to ice3.1
//                // (ICE_2_DATABASE_SCHEMA_VERSION)
//                databaseSchema.setValue(DATABASE_SCHEMA_VERSION);
//                dao.save(databaseSchema);
////                Logger.error("Could not upgrade database schema. No Code");
////                boolean error = migrateFrom081To090();
////                if (!error) {
////                    databaseSchema.setValue(DATABASE_SCHEMA_VERSION);
////                    dao.save(databaseSchema);
////                }
//            }
//
//        } catch (DAOException e) {
//            throw new UtilityException(e);
//        }
//    }

    /**
     * Check for, and create first admin account
     *
     * @throws UtilityException
     */
    private static void createAdminAccount() throws UtilityException {
        try {
            AccountController controller = new AccountController();
            controller.createAdminAccount();
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Check for and create the System account.
     *
     * @throws UtilityException
     */
    private static void createSystemAccount() throws UtilityException {
        AccountController controller = new AccountController();
        try {
            controller.createSystemAccount();
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }
}
