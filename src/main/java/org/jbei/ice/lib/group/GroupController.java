package org.jbei.ice.lib.group;

import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;

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

    public Group createOrRetrievePublicGroup() throws ControllerException {
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

    /**
     * retrieves all parent groups for any group in the set.
     *
     * @param groups
     * @return
     */
    public Set<Group> getAllGroups(Set<Group> groups) throws ControllerException {
        return null;
    }
}
