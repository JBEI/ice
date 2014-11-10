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
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.GroupDAO;
import org.jbei.ice.lib.dto.AccountResults;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.entry.EntryController;
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

    /**
     * Can access a group if you are an admin or the owner of that group
     *
     * @param userId
     * @param id
     * @return
     */
    public UserGroup getGroupById(String userId, long id) {
        Group group = dao.get(id);
        if (group == null || !canAccessGroup(userId, group))
            return null;

        return group.toDataTransferObject();
    }

    private boolean canAccessGroup(String userId, Group group) {
        boolean isOwner = group.getOwner() != null && group.getOwner().getEmail().equalsIgnoreCase(userId);
        return isOwner || accountController.isAdministrator(userId);
    }

    public ArrayList<AccountTransfer> getGroupMembers(String userId, long id) {
        Group group = dao.get(id);
        if (group == null || !canAccessGroup(userId, group))
            return null;

        // todo : add paging
        ArrayList<AccountTransfer> list = new ArrayList<>();
        for (Account account : group.getMembers()) {
            list.add(account.toDataTransferObject());
        }

        return list;
    }

    /**
     * Retrieves groups that user is either a member of. Users are implicit members of the groups
     * that they create so call also returns those groups
     *
     * @param userIdString       user account  identifier of user making request
     * @param userId             id of account whose groups are being requested
     * @param includePublicGroup whether to include the public group that everyone is implicitly a member of
     * @return list of groups that user is a member of
     */
    public ArrayList<UserGroup> retrieveUserGroups(String userIdString, long userId, boolean includePublicGroup) {
        Account requester = accountController.getByEmail(userIdString);
        Account account = accountController.get(userId);
        // TODO : account authorization
        if (!accountController.isAdministrator(requester) && !account.equals(requester))
            return null;

        Set<Group> result = dao.retrieveMemberGroups(account);
        ArrayList<UserGroup> userGroups = new ArrayList<>();
        if (includePublicGroup) {
            Group publicGroup = createOrRetrievePublicGroup();
            userGroups.add(publicGroup.toDataTransferObject());
        }

        for (Group group : result) {
            UserGroup user = group.toDataTransferObject();
            long count = dao.getMemberCount(group.getUuid());
            user.setMemberCount(count);
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
    public UserGroup createGroup(String userId, UserGroup info) {
        if (info.getType() == GroupType.PUBLIC && !accountController.isAdministrator(userId)) {
            String errMsg = "Non admin " + userId + " attempting to create public group";
            Logger.error(errMsg);
            return null;
        }

        if (info.getType() == null)
            info.setType(GroupType.PRIVATE);

        Account account = accountController.getByEmail(userId);

        Group group = new Group();
        group.setLabel(info.getLabel());
        group.setDescription(info.getDescription() == null ? "" : info.getDescription());
        group.setType(info.getType());
        group.setOwner(account);
        group = save(group);

        for (AccountTransfer accountTransfer : info.getMembers()) {
            Account memberAccount = accountController.getByEmail(accountTransfer.getEmail());
            if (memberAccount == null)
                continue;
            memberAccount.getGroups().add(group);
            accountController.save(memberAccount);
        }

//        group.getMembers().addAll(accounts);
//        group = dao.update(group);

        info = group.toDataTransferObject();
        for (Account addedAccount : group.getMembers()) {
            info.getMembers().add(addedAccount.toDataTransferObject());
        }
        info.setMemberCount(info.getMembers().size());
        return info;
    }

    public boolean updateGroup(String userId, UserGroup user) {
        if (user.getType() == GroupType.PUBLIC && !accountController.isAdministrator(userId)) {
            String errMsg = "Non admin " + userId + " attempting to update public group";
            Logger.error(errMsg);
            return false;
        }

        Group group = dao.get(user.getId());
        if (group == null) {
            return false;
        }

        group.setLabel(user.getLabel());
        group.setDescription(user.getDescription());
        group = dao.update(group);

        setGroupMembers(group, user.getMembers());
        return group != null;
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
        Group publicGroup = dao.get(PUBLIC_GROUP_UUID);
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

    public Set<Group> getMatchingGroups(String userId, String query, int limit) {
        Account account = accountController.getByEmail(userId);
        if (account == null)
            return null;
        return dao.getMatchingGroups(account, query, limit);
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

    public ArrayList<Group> getAllPublicGroupsForAccount(Account account) {
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

    public ArrayList<AccountTransfer> retrieveGroupMembers(String uuid, boolean includeEntryCounts) {
        Set<AccountTransfer> result = new HashSet<>();
        Group group = dao.get(uuid);
        EntryController entryController = new EntryController();
        for (Account account : group.getMembers()) {
            AccountTransfer accountTransfer = account.toDataTransferObject();
            if (includeEntryCounts) {
                // TODO :
//                try {
//
//                    count = entryController.getNumberOfOwnerEntries(userAccount, userAccount.getEmail());
//                    info.setUserEntryCount(count);
//                } catch (Exception e) {
//                }
            }
            result.add(accountTransfer);
        }
        return new ArrayList<>(result);
    }

    public long retrieveGroupMemberCount(String uuid) throws ControllerException {
        return dao.getMemberCount(uuid);
    }

    public AccountResults getAvailableAccounts(String userId, int offset, int limit, boolean asc, String sort) {
        Account account = accountController.getByEmail(userId);
        Set<AccountTransfer> accounts = new HashSet<>();

        if (accountController.isAdministrator(account)) {
            return accountController.retrieveAccounts(userId, offset, limit, sort, asc);
        } else {
            Set<Group> groups = getAllGroups(account);
            AccountResults results = new AccountResults();

            for (Group group : groups) {
                if (group.getType() == GroupType.PRIVATE)
                    continue;

                ArrayList<AccountTransfer> members = retrieveGroupMembers(group.getUuid(), false);
                accounts.addAll(members);
            }

            results.getResults().addAll(accounts);
            return results;
        }
    }

    protected void setGroupMembers(Group group, ArrayList<AccountTransfer> members) {
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

        group.getMembers().clear();
        group.getMembers().addAll(accounts);
        dao.update(group);

        members.clear();
        for (Account addedAccount : accounts) {
            members.add(addedAccount.toDataTransferObject());
        }
    }
}
