package org.jbei.ice.group;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.account.Account;
import org.jbei.ice.account.AccountAuthorization;
import org.jbei.ice.account.AccountController;
import org.jbei.ice.account.AccountType;
import org.jbei.ice.dto.common.Results;
import org.jbei.ice.dto.group.GroupType;
import org.jbei.ice.dto.group.UserGroup;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.dto.web.RemoteUser;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.hibernate.dao.RemoteClientModelDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.RemoteClientModel;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.utils.Utils;

import java.util.*;

/**
 * Groups of users with methods for manipulation (and access)
 *
 * @author Hector Plahar
 */
public class Groups {

    private final GroupDAO dao;
    private final AccountDAO accountDAO;
    private final String userId;
    private final RemoteClientModelDAO remoteClientModelDAO;
    private final RemotePartnerDAO remotePartnerDAO;
    private final AccountAuthorization authorization;

    public Groups(String userId) {
        this.userId = userId;
        this.dao = DAOFactory.getGroupDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.remoteClientModelDAO = DAOFactory.getRemoteClientModelDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.authorization = new AccountAuthorization();
    }

    /**
     * Retrieves groups that user is either a member of. Users are implicit members of the groups
     * that they create so call also returns those groups
     *
     * @param userId id of account whose groups are being requested
     * @return list of groups that user is a member of
     */
    public Results<UserGroup> get(long userId) {
        AccountController accountController = new AccountController();
        AccountModel userIdAccount = accountDAO.get(userId);

        AccountModel account = accountDAO.getByEmail(this.userId);

        // TODO : account authorization
        if (!this.authorization.isAdministrator(account.getEmail()) && account.getId() != userIdAccount.getId())
            return null;

        List<Group> result = dao.retrieveMemberGroups(account);
        Results<UserGroup> groupResults = new Results<>();

        for (Group group : result) {
            UserGroup user = group.toDataTransferObject();
            long count = dao.getMemberCount(group.getUuid());

            // get clients
            count += DAOFactory.getRemoteClientModelDAO().getClientCount(group);
            user.setMemberCount(count);
            groupResults.getData().add(user);
        }
        return groupResults;
    }

    public Results<UserGroup> get(GroupType groupType, int offset, int limit) {
        List<Group> groupList = dao.getGroupsByType(groupType, offset, limit);

        Results<UserGroup> results = new Results<>();
        results.setResultCount(dao.getGroupsByTypeCount(groupType));
        for (Group group : groupList) {
            UserGroup userGroup = group.toDataTransferObject();
            long memberCount = dao.getMemberCount(group.getUuid());
            userGroup.setMemberCount(memberCount);
            results.getData().add(userGroup);
        }
        return results;
    }

    public List<UserGroup> getMatchingGroups(String token, int limit) {
        AccountModel account = accountDAO.getByEmail(this.userId);
        List<Group> groups = dao.getMatchingGroups(account, token, limit);
        List<UserGroup> results = new ArrayList<>(groups.size());
        for (Group group : groups) {
            results.add(group.toDataTransferObject());
        }
        return results;
    }

    /**
     * Adds group to the list of groups for current user
     *
     * @param userGroup information about group to be added, including members (local and remote)
     * @return added group
     */
    public UserGroup addGroup(UserGroup userGroup) {
        if (userGroup.getType() == null)
            userGroup.setType(GroupType.PRIVATE);

        if (userGroup.getType() == GroupType.PUBLIC && !this.authorization.isAdministrator(userId)) {
            String errMsg = "Non admin '" + userId + "' attempting to create public group";
            Logger.error(errMsg);
            throw new PermissionException(errMsg);
        }

        AccountModel account = accountDAO.getByEmail(userId);
        Group group = new Group();
        group.setUuid(Utils.generateUUID());

        group.setLabel(userGroup.getLabel());
        group.setDescription(userGroup.getDescription() == null ? "" : userGroup.getDescription());
        group.setType(userGroup.getType());
        group.setOwner(account);
        group.setAutoJoin(userGroup.isAutoJoin());
        group.setCreationTime(new Date());
        group = dao.create(group);

        // add local members
        if (userGroup.getMembers() != null && !userGroup.getMembers().isEmpty()) {
            for (Account accountTransfer : userGroup.getMembers()) {
                AccountModel memberAccount = accountDAO.getByEmail(accountTransfer.getEmail());
                if (memberAccount == null)
                    continue;

                group.getMembers().add(memberAccount);
                memberAccount.getGroups().add(group);
                accountDAO.update(memberAccount);
            }
        }

        // add remote members
        for (RemoteUser remoteUser : userGroup.getRemoteMembers()) {
            RegistryPartner partner = remoteUser.getPartner();
            if (partner == null)
                continue;

            RemotePartner remotePartner = remotePartnerDAO.get(partner.getId());
            if (remotePartner == null)
                continue;

            Account accountTransfer = remoteUser.getUser();
            if (accountTransfer == null || StringUtils.isEmpty(accountTransfer.getEmail()))
                continue;

            String email = accountTransfer.getEmail();
            RemoteClientModel remoteClientModel = remoteClientModelDAO.getModel(email, remotePartner);
            if (remoteClientModel == null) {
                remoteClientModel = new RemoteClientModel();
                remoteClientModel.setEmail(email);
                remoteClientModel.setRemotePartner(remotePartner);
                remoteClientModel = remoteClientModelDAO.create(remoteClientModel);
            }

            remoteClientModel.getGroups().add(group);
            remoteClientModelDAO.update(remoteClientModel);
        }

        return group.toDataTransferObject();
    }

    /**
     * Retrieves both local and remote members of the specified group if the user making the request
     * has the appropriate permissions
     *
     * @param groupId unique local identifier of group
     * @return information about specified group including remote and local members
     * @throws PermissionException if the user does not have read permissions
     */
    public UserGroup getGroupMembers(long groupId) {
        Group group = dao.get(groupId);
        if (group == null)
            return null;

        if (group.getType() == GroupType.PUBLIC) {
            if (!this.authorization.isAdministrator(userId))
                throw new PermissionException("Administrative privileges required");
        } else if (!userId.equalsIgnoreCase(group.getOwner().getEmail())) {
            AccountModel account = accountDAO.getByEmail(this.userId);
            if (account.getType() != AccountType.ADMIN)
                throw new PermissionException("Missing required permissions");
        }

        UserGroup userGroup = group.toDataTransferObject();

        for (AccountModel account : group.getMembers()) {
            userGroup.getMembers().add(account.toDataTransferObject());
        }

        // get remote members
        List<RemoteClientModel> clients = remoteClientModelDAO.getClientsForGroup(group);
        for (RemoteClientModel clientModel : clients) {
            userGroup.getRemoteMembers().add(clientModel.toDataTransferObject());
        }

        return userGroup;
    }

    public boolean update(long groupId, UserGroup userGroup) {
        Group group = dao.get(groupId);
        if (group == null) {
            return false;
        }

        if (group.getType() == GroupType.PUBLIC && !this.authorization.isAdministrator(userId)) {
            String errMsg = "Non admin " + userId + " attempting to update public group";
            Logger.error(errMsg);
            throw new PermissionException(errMsg);
        }

        group.setLabel(userGroup.getLabel());
        group.setDescription(userGroup.getDescription());
        group.setAutoJoin(userGroup.isAutoJoin());
        group = dao.update(group);

        setGroupMembers(group, userGroup.getMembers(), userGroup.getRemoteMembers());
        return true;
    }

    protected void setGroupMembers(Group group, List<Account> members, List<RemoteUser> remoteUsers) {
        //
        // deal with local members
        //
        Set<String> memberHash = new HashSet<>();
        for (AccountModel member : group.getMembers()) {
            memberHash.add(member.getEmail());
        }

        for (Account account : members) {
            String email = account.getEmail();

            // do not add if found in set
            if (memberHash.remove(email))
                continue;

            // not found so add
            AccountModel memberToAdd = accountDAO.getByEmail(email);
            if (memberToAdd == null) {
                Logger.error("Could not find account " + email + " to add to group");
                continue;
            }

            group.getMembers().add(memberToAdd);
            memberToAdd.getGroups().add(group);
            accountDAO.update(memberToAdd);
        }

        // all emails remaining should be removed
        for (String memberEmail : memberHash) {
            AccountModel memberAccount = accountDAO.getByEmail(memberEmail);
            if (memberAccount == null)
                continue;
            memberAccount.getGroups().remove(group);
            group.getMembers().remove(memberAccount);
            accountDAO.update(memberAccount);
        }

        //
        // deal with remote users
        //
        Map<String, RemoteClientModel> remoteMembers = new HashMap<>();
        // get remote members
        List<RemoteClientModel> clients = remoteClientModelDAO.getClientsForGroup(group);
        for (RemoteClientModel clientModel : clients) {
            // technically the same email can be at different instances so adding partner id
            String identifier = (clientModel.getEmail() + clientModel.getRemotePartner().getUrl()).toLowerCase();
            remoteMembers.put(identifier, clientModel);
        }

        for (RemoteUser remoteUser : remoteUsers) {
            String identifier = (remoteUser.getUser().getEmail() + remoteUser.getPartner().getUrl()).toLowerCase();

            // do not add if found in set (already exists)
            if (remoteMembers.remove(identifier) != null)
                continue;

            // add   // todo : duplicates line 147. move to separate method
            RemotePartner remotePartner = remotePartnerDAO.getByUrl(remoteUser.getPartner().getUrl());
            if (remotePartner == null)
                continue;

            Account account = remoteUser.getUser();
            if (account == null || StringUtils.isEmpty(account.getEmail()))
                continue;

            String email = account.getEmail();
            RemoteClientModel remoteClientModel = remoteClientModelDAO.getModel(email, remotePartner);
            if (remoteClientModel == null) {
                remoteClientModel = new RemoteClientModel();
                remoteClientModel.setEmail(email);
                remoteClientModel.setRemotePartner(remotePartner);
                remoteClientModel = remoteClientModelDAO.create(remoteClientModel);
            }

            remoteClientModel.getGroups().add(group);
            remoteClientModelDAO.update(remoteClientModel);
        }

        // remove all remaining
        if (remoteMembers.isEmpty())
            return;

        for (String identifier : remoteMembers.keySet()) {
            RemoteClientModel remoteClientModel = remoteMembers.get(identifier);
            remoteClientModel.getGroups().remove(group);
            remoteClientModelDAO.update(remoteClientModel);
        }
    }
}
