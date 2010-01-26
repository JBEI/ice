package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;

public class PopulateInitialDatabase {

    public static void main(String[] args) {
        createFirstGroup();

    }

    public static void createFirstGroup() {
        Group group1 = null;
        try {
            group1 = GroupManager.get(1);
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        if (group1 == null) {
            Group group = new Group();
            group.setLabel("Everyone");
            group.setDescription("Everyone");
            group.setParent(null);
            // This is a global "everyone" uuid
            group.setUuid("8746a64b-abd5-4838-a332-02c356bbeac0");
            try {
                GroupManager.save(group);
            } catch (ManagerException e) {

                e.printStackTrace();
            }
        }
    }
}
