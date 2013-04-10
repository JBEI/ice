package org.jbei.ice.lib.utils;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

/**
 * Populate an empty database with necessary objects and values.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class PopulateInitialDatabase {

    // This is a global "everyone" uuid
    public static String PUBLIC_GROUP_UUID = "8746a64b-abd5-4838-a332-02c356bbeac0";

    /**
     * Populate an empty database with necessary objects and values.
     * <p/>
     * <ul> <li>Create the everyone group.</li> <li>Create the System account.</li> <li>Create the Admin account.</li>
     * <li>Create default storage schemes.</li> <li>Update the database schema, if necessary.</li> </ul>
     *
     * @throws UtilityException
     */
    public static void initializeDatabase() throws UtilityException {
        GroupController groupController = ControllerFactory.getGroupController();
        Group group1;
        try {
            group1 = groupController.getGroupByUUID(PUBLIC_GROUP_UUID);
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
        StorageController storageController = ControllerFactory.getStorageController();

        try {
            storageController.createStrainStorageRoot();
            storageController.createPlasmidStorageRoot();
            storageController.createPartStorageRoot();
            storageController.createSeedStorageRoot();
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Check for, and create first admin account
     *
     * @throws UtilityException
     */
    private static void createAdminAccount() throws UtilityException {
        try {
            AccountController controller = ControllerFactory.getAccountController();
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
        AccountController controller = ControllerFactory.getAccountController();
        try {
            controller.createSystemAccount();
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }
}
