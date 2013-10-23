package org.jbei.ice.lib.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.shared.dto.user.User;
import org.jbei.ice.lib.utils.Utils;

public class GroupController {

    public static final String PUBLIC_GROUP_NAME = "Public";
    public static final String PUBLIC_GROUP_DESCRIPTION = "All users are members of this group";
    public static final String PUBLIC_GROUP_UUID = "8746a64b-abd5-4838-a332-02c356bbeac0";

    private final AccountController accountController;
    private final GroupDAO dao;

    public GroupController() {
        dao = new GroupDAO();
        accountController = ControllerFactory.getAccountController();
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

    /**
     * Retrieves groups that user is either a member of. Users are implicit members of the groups
     * that they create so call also returns those groups
     *
     * @param account user account
     * @return list of groups that user is a member of
     * @throws ControllerException
     */
    public ArrayList<UserGroup> retrieveUserGroups(Account account) throws ControllerException {
        try {
            Set<Group> result = dao.retrieveMemberGroups(account);
            ArrayList<UserGroup> userGroups = new ArrayList<>();
            for (Group group : result) {
                UserGroup user = Group.toDTO(group);
                user.setMemberCount(retrieveGroupMemberCount(group.getUuid()));
                userGroups.add(user);
            }
            return userGroups;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<UserGroup> retrieveGroups(Account account, GroupType type) throws ControllerException {
        ArrayList<UserGroup> userGroups = new ArrayList<>();

        try {
            ArrayList<Group> result;
            switch (type) {
                default:
                case PRIVATE:
                    result = dao.retrieveGroups(account, type);
                    break;

                case PUBLIC:
                    if (account.getType() != AccountType.ADMIN)
                        throw new ControllerException("Cannot retrieve public groups without admin privileges");

                    result = dao.retrievePublicGroups();
                    break;
            }

            for (Group group : result) {
                UserGroup user = Group.toDTO(group);
                user.setMemberCount(retrieveGroupMemberCount(group.getUuid()));
                userGroups.add(user);
            }
            return userGroups;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public Set<String> retrieveAccountGroupUUIDs(Account account) throws ControllerException {
        Set<String> uuids = new HashSet<>();
        if (account != null) {
            for (Group group : account.getGroups()) {
                uuids.add(group.getUuid());
            }
        }
        uuids.add(PUBLIC_GROUP_UUID);
        return uuids;
    }

    public Group save(Group group) throws ControllerException {
        try {
            if (group.getUuid() == null || group.getUuid().isEmpty())
                group.setUuid(Utils.generateUUID());

            if (group.getType() == GroupType.PRIVATE)
                group.setAutoJoin(false);

            return dao.save(group);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    // create group without parent
    public UserGroup createGroup(Account account, UserGroup info) throws ControllerException {
        if (info.getType() == GroupType.PUBLIC && !accountController.isAdministrator(account)) {
            String errMsg = "Non admin " + account.getEmail() + " attempting to create public group";
            Logger.error(errMsg);
            throw new ControllerException(errMsg);
        }

        if (info.getType() == null)
            info.setType(GroupType.PRIVATE);

        Group group = new Group();
        group.setLabel(info.getLabel());
        group.setDescription(info.getDescription() == null ? "" : info.getDescription());
        group.setType(info.getType());
        if (info.getType() == GroupType.PRIVATE)
            group.setOwner(account);
        else {
            Account systemAccount = accountController.getSystemAccount();
            group.setOwner(systemAccount);
            group.setParent(this.createOrRetrievePublicGroup());
        }

        group = save(group);

        ArrayList<Account> accounts = new ArrayList<>();
        for (User user : info.getMembers()) {
            Account memberAccount = accountController.getByEmail(user.getEmail());
            if (memberAccount == null)
                continue;
            memberAccount.getGroups().add(group);
            accountController.save(memberAccount);
            accounts.add(memberAccount);
        }

        try {
            group.getMembers().addAll(accounts);
            group = dao.update(group);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        info = Group.toDTO(group);
        for (Account addedAccount : group.getMembers()) {
            info.getMembers().add(Account.toDTO(addedAccount));
        }
        info.setMemberCount(info.getMembers().size());
        return info;
    }

    public UserGroup updateGroup(Account account, UserGroup user) throws ControllerException {
        if (user.getType() == GroupType.PUBLIC && !accountController.isAdministrator(account)) {
            String errMsg = "Non admin " + account.getEmail() + " attempting to update public group";
            Logger.error(errMsg);
            throw new ControllerException(errMsg);
        }

        Group group;
        try {
            group = dao.get(user.getId());
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        if (group == null) {
            throw new ControllerException("Could not find group to update");
        }

        try {
            group.setLabel(user.getLabel());
            group.setDescription(user.getDescription());
            group = dao.update(group);
            return Group.toDTO(group);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public UserGroup deleteGroup(Account account, UserGroup user) throws ControllerException {
        if (user.getType() == GroupType.PUBLIC && account.getType() != AccountType.ADMIN) {
            String errMsg = "Non admin " + account.getEmail() + " attempting to delete public group";
            Logger.error(errMsg);
            throw new ControllerException(errMsg);
        }

        Group group;
        try {
            group = dao.get(user.getId());
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        if (group == null) {
            throw new ControllerException("Could not find group to delete");
        }

        try {
            if (group.getMembers() != null) {
                for (Account member : group.getMembers()) {
                    accountController.removeMemberFromGroup(group.getId(), member.getEmail());
                }
            }
            ControllerFactory.getPermissionController().clearGroupPermissions(account, group);
            UserGroup userGroup = Group.toDTO(group);
            dao.delete(group);
            return userGroup;
        } catch (DAOException e) {
            throw new ControllerException(e);
        } catch (PermissionException e) {
            Logger.warn(e.getMessage());
            throw new ControllerException(e);
        }
    }

    public Group createOrRetrievePublicGroup() throws ControllerException {
        Group publicGroup = this.getGroupByUUID(PUBLIC_GROUP_UUID);
        if (publicGroup != null)
            return publicGroup;

        publicGroup = new Group();
        publicGroup.setLabel(PUBLIC_GROUP_NAME);
        publicGroup.setDescription(PUBLIC_GROUP_DESCRIPTION);
        publicGroup.setType(GroupType.SYSTEM);
        publicGroup.setParent(null);
        publicGroup.setUuid(PUBLIC_GROUP_UUID);
        return save(publicGroup);
    }

    public Set<Group> getMatchingGroups(Account account, String query, int limit) throws ControllerException {
        try {
            return dao.getMatchingGroups(account, query, limit);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * retrieves all parent groups for any group in the set. if account is null, then the everyone group
     * is returned
     *
     * @param account account whose groups are being retrieved
     * @return set of groups retrieved for account
     */
    public Set<Group> getAllGroups(Account account) throws ControllerException {
        if (account == null) {
            Set<Group> groups = new HashSet<>();
            groups.add(createOrRetrievePublicGroup());
            return groups;
        }

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
        HashSet<Long> accountGroups = new HashSet<>();

        for (Group group : account.getGroups()) {
            accountGroups = getParentGroups(group, accountGroups);
        }

        // Everyone belongs to the everyone group
        try {
            Group everybodyGroup = createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup.getId());
        } catch (ControllerException e) {
            Logger.warn("could not get everybody group: " + e.toString());
        }
        return accountGroups;
    }

    public ArrayList<Group> getAllPublicGroupsForAccount(Account account) throws ControllerException {
        ArrayList<Group> groups = new ArrayList<>();
        for (Group group : account.getGroups()) {
            if (group.getType() == GroupType.PUBLIC)
                groups.add(group);
        }
        return groups;
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

    public ArrayList<User> retrieveGroupMembers(String uuid) throws ControllerException {
        try {
            ArrayList<User> result = new ArrayList<>();
            Group group = dao.get(uuid);
            for (Account account : group.getMembers()) {
                User user = Account.toDTO(account);
                result.add(user);
            }
            return result;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public long retrieveGroupMemberCount(String uuid) throws ControllerException {
        try {
            return dao.getMemberCount(uuid);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public List<Group> getAutoJoinGroups() throws ControllerException {
        try {
            return dao.getAutoJoinGroups();
        } catch (DAOException de) {
            throw new ControllerException();
        }
    }

    public ArrayList<User> retrieveAccountsForGroupCreation(Account account) throws ControllerException {
        Set<Group> groups = getAllGroups(account);
        Set<User> accounts = new HashSet<>();

        for (Group group : groups) {
            if (group.getType() == GroupType.PRIVATE)
                continue;

            ArrayList<User> members = retrieveGroupMembers(group.getUuid());
            accounts.addAll(members);
        }

        return new ArrayList<>(accounts);
    }

    public ArrayList<User> setGroupMembers(Account account, UserGroup info, ArrayList<User> members)
            throws ControllerException {
        Group group = getGroupById(info.getId());
        if (group == null) {
            String errMsg = "Could retrieve group with id " + info.getId();
            Logger.error(errMsg);
            throw new ControllerException(errMsg);
        }

        if (group.getUuid().equalsIgnoreCase(PUBLIC_GROUP_UUID))
            return new ArrayList<>();

        // check permissions
        if (!account.getEmail().equalsIgnoreCase(group.getOwner().getEmail())) {
            if (!accountController.isAdministrator(account)) {
                String errMsg = account.getEmail() + " does not have permissions to modify group";
                Logger.error(errMsg);
                throw new ControllerException(errMsg);
            }
        }

        // is there an easier way to do this?
        // remove
        for (Account member : group.getMembers()) {
            Account memberAccount = accountController.getByEmail(member.getEmail());
            if (memberAccount == null)
                continue;
            memberAccount.getGroups().remove(group);
            accountController.save(memberAccount);
        }

        // add
        ArrayList<Account> accounts = new ArrayList<>();
        for (User user : members) {
            Account memberAccount = accountController.getByEmail(user.getEmail());
            if (memberAccount == null)
                continue;
            memberAccount.getGroups().add(group);
            accountController.save(memberAccount);
            accounts.add(memberAccount);
        }

        try {
            group.getMembers().clear();
            group.getMembers().addAll(accounts);
            dao.update(group);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        members.clear();
        for (Account addedAccount : accounts) {
            members.add(Account.toDTO(addedAccount));
        }
        return members;
    }
}
