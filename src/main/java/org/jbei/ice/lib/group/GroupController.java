package org.jbei.ice.lib.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.GroupDAO;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.utils.Utils;

public class GroupController {

    public static final String PUBLIC_GROUP_NAME = "Public";
    public static final String PUBLIC_GROUP_DESCRIPTION = "All users are members of this group";
    public static final String PUBLIC_GROUP_UUID = "8746a64b-abd5-4838-a332-02c356bbeac0";

    private final AccountController accountController;
    private final GroupDAO dao;

    public GroupController() {
        dao = new GroupDAO();
        accountController = new AccountController();
    }

    public Group getGroupByUUID(String uuid) {
        return dao.get(uuid);
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
     * @param account            user account
     * @param includePublicGroup whether to include the public group that everyone is implicitly a member of
     * @return list of groups that user is a member of
     * @throws ControllerException
     */
    public ArrayList<UserGroup> retrieveUserGroups(Account account, boolean includePublicGroup)
            throws ControllerException {
        try {
            Set<Group> result = dao.retrieveMemberGroups(account);
            ArrayList<UserGroup> userGroups = new ArrayList<>();
            if (includePublicGroup) {
                Group publicGroup = createOrRetrievePublicGroup();
                userGroups.add(publicGroup.toDataTransferObject());
            }

            for (Group group : result) {
                UserGroup user = group.toDataTransferObject();
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
            UserGroup user = group.toDataTransferObject();
            user.setMemberCount(retrieveGroupMemberCount(group.getUuid()));
            userGroups.add(user);
        }
        return userGroups;
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

    public Group save(Group group) {
        if (group.getUuid() == null || group.getUuid().isEmpty())
            group.setUuid(Utils.generateUUID());

        return dao.create(group);
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
        group.setOwner(account);
        group = save(group);

        ArrayList<Account> accounts = new ArrayList<>();
        for (AccountTransfer accountTransfer : info.getMembers()) {
            Account memberAccount = accountController.getByEmail(accountTransfer.getEmail());
            if (memberAccount == null)
                continue;
            memberAccount.getGroups().add(group);
            accountController.save(memberAccount);
            accounts.add(memberAccount);
        }

        group.getMembers().addAll(accounts);
        group = dao.update(group);

        info = group.toDataTransferObject();
        for (Account addedAccount : group.getMembers()) {
            info.getMembers().add(addedAccount.toDataTransferObject());
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

        Group group = dao.get(user.getId());
        if (group == null) {
            throw new ControllerException("Could not find group to update");
        }

        group.setLabel(user.getLabel());
        group.setDescription(user.getDescription());
        group = dao.update(group);
        return group.toDataTransferObject();
    }

    public UserGroup deleteGroup(Account account, UserGroup user) throws ControllerException {
        if (user.getType() == GroupType.PUBLIC && account.getType() != AccountType.ADMIN) {
            String errMsg = "Non admin " + account.getEmail() + " attempting to delete public group";
            Logger.error(errMsg);
            throw new ControllerException(errMsg);
        }

        Group group = dao.get(user.getId());
        if (group == null) {
            throw new ControllerException("Could not find group to delete");
        }

        if (group.getMembers() != null) {
            for (Account member : group.getMembers()) {
                accountController.removeMemberFromGroup(group.getId(), member.getEmail());
            }
        }
        DAOFactory.getPermissionDAO().clearPermissions(group);
        UserGroup userGroup = group.toDataTransferObject();
        dao.delete(group);
        return userGroup;
    }

    public Group createOrRetrievePublicGroup() {
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
    public Set<Group> getAllGroups(Account account) {
        if (account == null) {
            Set<Group> groups = new HashSet<>();
            groups.add(createOrRetrievePublicGroup());
            return groups;
        }

        Set<Long> groupIds = getAllAccountGroups(account);
        return dao.getByIdList(groupIds);
    }

    /**
     * Retrieve all parent {@link Group}s of a given {@link org.jbei.ice.lib.account.model.Account}.
     *
     * @param account Account to query on.
     * @return Set of Group ids.
     */
    protected Set<Long> getAllAccountGroups(Account account) {
        HashSet<Long> accountGroups = new HashSet<>();

        for (Group group : account.getGroups()) {
            accountGroups = getParentGroups(group, accountGroups);
        }

        // Everyone belongs to the everyone group
        Group everybodyGroup = createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup.getId());
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
    protected HashSet<Long> getParentGroups(Group group, HashSet<Long> groupIds) {
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

    public ArrayList<AccountTransfer> retrieveGroupMembers(String uuid) throws ControllerException {
        try {
            ArrayList<AccountTransfer> result = new ArrayList<>();
            Group group = dao.get(uuid);
            for (Account account : group.getMembers()) {
                AccountTransfer accountTransfer = account.toDataTransferObject();
                result.add(accountTransfer);
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

    public ArrayList<AccountTransfer> retrieveAccountsForGroupCreation(Account account) throws ControllerException {
        Set<Group> groups = getAllGroups(account);
        Set<AccountTransfer> accounts = new HashSet<>();

        for (Group group : groups) {
            if (group.getType() == GroupType.PRIVATE)
                continue;

            ArrayList<AccountTransfer> members = retrieveGroupMembers(group.getUuid());
            accounts.addAll(members);
        }

        return new ArrayList<>(accounts);
    }

    public ArrayList<AccountTransfer> setGroupMembers(Account account, UserGroup info,
            ArrayList<AccountTransfer> members) throws ControllerException {
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
        for (AccountTransfer accountTransfer : members) {
            Account memberAccount = accountController.getByEmail(accountTransfer.getEmail());
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
            members.add(addedAccount.toDataTransferObject());
        }
        return members;
    }
}
