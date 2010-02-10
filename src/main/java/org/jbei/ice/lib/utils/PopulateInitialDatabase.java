package org.jbei.ice.lib.utils;

import java.util.Set;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.PermissionManager;

public class PopulateInitialDatabase {
    // This is a global "everyone" uuid
    public static String everyoneGroup = "8746a64b-abd5-4838-a332-02c356bbeac0";

    public static void main(String[] args) {
        createFirstGroup();
        populatePermissionReadGroup();

    }

    public static Group createFirstGroup() {
        Group group1 = null;
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
                group1 = group;
            } catch (ManagerException e) {
                String msg = "Could not save everyone group: " + e.toString();
                Logger.error(msg);
            }
        }
        return group1;

    }

    public static void populatePermissionReadGroup() {
        Group group1 = null;
        try {
            group1 = GroupManager.get(everyoneGroup);
        } catch (ManagerException e) {
            // nothing happens
            Logger.debug(e.toString());
        }
        if (group1 != null) {
            Set<Entry> allEntries = EntryManager.getAll();
            for (Entry entry : allEntries) {
                try {
                    Set<Group> groups = PermissionManager.getReadGroup(entry);
                    int originalSize = groups.size();
                    groups.add(group1);
                    PermissionManager.setReadGroup(entry, groups);

                    String msg = "updated id:" + entry.getId() + " from " + originalSize + " to "
                            + groups.size() + ".";
                    Logger.info(msg);
                } catch (ManagerException e) {
                    // skip
                    Logger.debug(e.toString());
                }

            }
        }
    }
}
