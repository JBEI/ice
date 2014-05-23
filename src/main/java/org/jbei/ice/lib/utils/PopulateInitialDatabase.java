package org.jbei.ice.lib.utils;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

/**
 * Populate an empty database with necessary objects and values.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class PopulateInitialDatabase {

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
        group1 = groupController.getGroupByUUID(GroupController.PUBLIC_GROUP_UUID);
        if (group1 == null) {
            groupController.createOrRetrievePublicGroup();
            createAdminAccount();
        }
    }

    /**
     * Check for, and create first admin account
     *
     * @throws UtilityException
     */
    private static void createAdminAccount() throws UtilityException {
        try {
            new AccountController().createAdminAccount();
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }
}
