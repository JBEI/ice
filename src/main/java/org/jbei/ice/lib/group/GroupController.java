package org.jbei.ice.lib.group;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @param userId user identifier; typically email
     * @param id     unique identifier for group
     * @return group referenced by id if it exists and user has access to it
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

    public Set<String> retrieveAccountGroupUUIDs(String userId) {
        Account account = accountController.getByEmail(userId);
        Set<String> uuids = new HashSet<>();
        if (account != null) {
            uuids.addAll(dao.getMemberGroupUUIDs(account));
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

        info = group.toDataTransferObject();
        for (Account addedAccount : group.getMembers()) {
            info.getMembers().add(addedAccount.toDataTransferObject());
        }
        info.setMemberCount(info.getMembers().size());
        return info;
    }

    public boolean deleteGroup(String userIdStr, long groupId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userIdStr);
        Group group = dao.get(groupId);
        if (group == null)
            return false;

        if (group.getType() == GroupType.PUBLIC && account.getType() != AccountType.ADMIN) {
            String errMsg = "Non admin " + account.getEmail() + " attempting to delete public group";
            Logger.error(errMsg);
            throw new PermissionException(errMsg);
        }

        if (group.getMembers() != null) {
            for (Account member : group.getMembers()) {
                accountController.removeMemberFromGroup(group.getId(), member.getEmail());
            }
        }

        DAOFactory.getPermissionDAO().clearPermissions(group);
        dao.delete(group);
        return true;
    }

    public Group createOrRetrievePublicGroup() {
        Group publicGroup = dao.getByUUID(PUBLIC_GROUP_UUID);
        if (publicGroup != null)
            return publicGroup;

        publicGroup = new Group();
        publicGroup.setLabel(PUBLIC_GROUP_NAME);
        publicGroup.setDescription(PUBLIC_GROUP_DESCRIPTION);
        publicGroup.setType(GroupType.SYSTEM);
        publicGroup.setUuid(PUBLIC_GROUP_UUID);
        return save(publicGroup);
    }

    public List<Group> getMatchingGroups(String userId, String query, int limit) {
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
    public List<Group> getAllGroups(Account account) {
        List<Group> groups = new ArrayList<>();
        groups.add(createOrRetrievePublicGroup());

        if (account != null)
            groups.addAll(account.getGroups());

        return groups;
    }

    public ArrayList<Group> getAllPublicGroupsForAccount(Account account) {
        ArrayList<Group> groups = new ArrayList<>();
        if (account == null || account.getGroups() == null)
            return groups;

        for (Group group : account.getGroups()) {
            if (group.getType() == GroupType.PUBLIC)
                groups.add(group);
        }
        return groups;
    }
}
