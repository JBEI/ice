package org.jbei.ice.lib.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountUtils;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.group.GroupType;

public class GroupController {

    public static final String PUBLIC_GROUP_NAME = "Global";
    public static final String PUBLIC_GROUP_DESCRIPTION = "All users are members of this group";
    public static final String PUBLIC_GROUP_UUID = "8746a64b-abd5-4838-a332-02c356bbeac0";

    private final AccountController accountController;
    private final GroupDAO dao;

    public GroupController() {
        dao = new GroupDAO();
        accountController = new AccountController();
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
     * @param parentId    group parent
     * @param groupType   type of group to create
     * @return Saved Group object.
     * @throws ControllerException
     */
    public Group createGroup(String label, String description, long parentId, GroupType groupType)
            throws ControllerException {

        Group parent = getGroupById(parentId);
        if (parent != null && parent.getType() != groupType)
            throw new ControllerException("Parent child groups must be the same type");

        String uuid = java.util.UUID.randomUUID().toString();
        Group newGroup = new Group();
        newGroup.setUuid(uuid);
        newGroup.setLabel(label);
        newGroup.setDescription(description);
        if (parent != null)
            newGroup.setParent(parent);
        newGroup.setType(groupType);
        return save(newGroup);
    }

    public Group createOrRetrievePublicGroup() throws ControllerException {
        Group publicGroup = this.getGroupByUUID(PUBLIC_GROUP_UUID);
        if (publicGroup != null)
            return publicGroup;

        publicGroup = new Group();
        publicGroup.setLabel(PUBLIC_GROUP_NAME);
        publicGroup.setDescription(PUBLIC_GROUP_DESCRIPTION);
        publicGroup.setType(GroupType.PUBLIC);
        publicGroup.setParent(null);
        publicGroup.setUuid(PUBLIC_GROUP_UUID);
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

    public ArrayList<AccountInfo> retrieveGroupMembers(String uuid) throws ControllerException {
        try {
            ArrayList<AccountInfo> result = new ArrayList<AccountInfo>();
            Group group = dao.get(uuid);
            for (Account account : group.getMembers()) {
                AccountInfo accountInfo = AccountUtils.accountToInfo(account);
                result.add(accountInfo);
            }
            return result;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public void addMemberToGroup(long groupId, String email) throws ControllerException {
        try {
            Group group = dao.get(groupId);
            if (group == null)
                throw new ControllerException("Could not retrieve group with id " + groupId);

            Account account = accountController.getByEmail(email);
            if (account == null)
                throw new ControllerException("Could not retrieve account " + email);

            group.getMembers().add(account);
            dao.update(group);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public void removeMemberFromGroup(long groupId, String email) throws ControllerException {
        try {
            Group group = dao.get(groupId);
            if (group == null)
                throw new ControllerException("Could not retrieve group with id " + groupId);

            Account account = accountController.getByEmail(email);
            if (account == null)
                throw new ControllerException("Could not retrieve account with email " + email);

            group.getMembers().remove(account);
            dao.update(group);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
