package org.jbei.ice.lib.group;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;

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
     * @param label       group label
     * @param description group description
     * @param parent      group parent
     * @return Saved Group object.
     * @throws ControllerException
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
     * @param account account whose groups are being retrieved
     * @return set of groups retrieved for account
     */
    public Set<Group> getAllGroups(Account account) throws ControllerException {

        Set<Long> groupIds = getAllAccountGroups(account);
        try {
            return dao.getByIdList(groupIds);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve all parent {@link Group}s of a given {@link org.jbei.ice.lib.account.model.Account}.
     *
     * @param account Account to query on.
     * @return Set of Group ids.
     */
    protected Set<Long> getAllAccountGroups(Account account) throws ControllerException {
        HashSet<Long> accountGroups = new HashSet<Long>();

        for (Group group : account.getGroups()) {
            accountGroups = getParentGroups(group, accountGroups);
        }

        // Everyone belongs to the everyone group
        GroupController controller = new GroupController();
        try {
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup.getId());
        } catch (ControllerException e) {
            Logger.warn("could not get everybody group: " + e.toString());
        }
        return accountGroups;
    }

    /**
     * Retrieve all parent {@link Group}s of a given group.
     *
     * @param group    Group to query on.
     * @param groupIds optional set of group ids. Can be empty.
     * @return Set of Parent group ids.
     */
    protected HashSet<Long> getParentGroups(Group group, HashSet<Long> groupIds) throws ControllerException {
        if (groupIds.contains(group.getId())) {
            return groupIds;
        } else {
            groupIds.add(group.getId());
            Group parentGroup = group.getParent();
            if (parentGroup != null) {
                getParentGroups(parentGroup, groupIds);
            }
        }

        return groupIds;
    }

}
