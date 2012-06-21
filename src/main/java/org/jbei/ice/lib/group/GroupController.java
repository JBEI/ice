package org.jbei.ice.lib.group;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;

import java.util.Set;

public class GroupController {

    private final GroupDAO dao;
    private final String publicGroupName = "Public";
    private final String publicGroupDescription = "Provides global public access";
    private final String publicGroupUUID = "8746a64b-abd5-4838-a332-02c356bbeac0";

    public GroupController() {
        dao = new GroupDAO();
    }

    public Group getGroupByUUID(String uuid) throws ControllerException {
        try {
            return dao.get(uuid);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Group getGroupById(long id) throws ControllerException {
        try {
            return dao.get(id);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Group save(Group group) throws ControllerException {
        try {
            return dao.save(group);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * Create new {@link Group} object in the database, using parameters.
     *
     * @param uuid
     * @param label
     * @param description
     * @param parent
     * @return Saved Group object.
     * @throws ManagerException
     */
    public Group create(String label, String description, Group parent) throws ControllerException {

        String uuid = java.util.UUID.randomUUID().toString();
        Group newGroup = new Group();
        newGroup.setUuid(uuid);
        newGroup.setLabel(label);
        newGroup.setDescription(description);
        newGroup.setParent(parent);
        return save(newGroup);
    }

    public Group createOrRetrievePublicGroup() throws ControllerException {
        //        String uuid = java.util.UUID.fromString(publicGroupName).toString();
        Group publicGroup = this.getGroupByUUID(publicGroupUUID);
        if (publicGroup != null)
            return publicGroup;

        publicGroup = new Group();
        publicGroup.setLabel(publicGroupName);
        publicGroup.setDescription(publicGroupDescription);
        publicGroup.setParent(null);
        publicGroup.setUuid(publicGroupUUID);
        return save(publicGroup);
    }

    public Set<Group> getMatchingGroups(String query, int limit) throws ControllerException {
        try {
            return dao.getMatchingGroups(query, limit);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Set<Group> getAllGroups() throws ControllerException {
        try {
            return dao.getAll();
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }
}
