package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;

public class PopulateInitialDatabase {
    // This is a global "everyone" uuid
    public static String everyoneGroup = "8746a64b-abd5-4838-a332-02c356bbeac0";

    public static void main(String[] args) {
        createFirstGroup();

    }

    public static Group createFirstGroup() {
        Group group1 = null;
        Group result = null;
        try {
            group1 = GroupManager.get(everyoneGroup);
        } catch (ManagerException e) {
            String msg = "Could not get everyone group " + e.toString();
            Logger.info(msg);
        }

        if (group1 == null) {
            Group group = new Group();
            group.setLabel("Everyone");
            group.setDescription("Everyone");
            group.setParent(null);

            group.setUuid(everyoneGroup);
            try {
                GroupManager.save(group);
                Logger.info("Creating everyone group");
                result = group;
            } catch (ManagerException e) {
                String msg = "Could not save everyone group: " + e.toString();
                Logger.error(msg);
            }
        }
        return result;

    }
}
