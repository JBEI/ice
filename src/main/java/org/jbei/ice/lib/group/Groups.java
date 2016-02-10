package org.jbei.ice.lib.group;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemoteUser;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.ClientModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Groups of users with methods for manipulation (and access)
 *
 * @author Hector Plahar
 */
public class Groups {

    private final GroupDAO dao;
    private final AccountDAO accountDAO;
    private final String userId;

    public Groups(String userId) {
        this.userId = userId;
        this.dao = DAOFactory.getGroupDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
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
        Account account = accountDAO.getByEmail(this.userId);
        Set<Group> groups = dao.getMatchingGroups(account, token, limit);
        List<UserGroup> results = new ArrayList<>(groups.size());
        for (Group group : groups) {
            results.add(group.toDataTransferObject());
        }
        return results;
    }

    public UserGroup addGroup(UserGroup userGroup) {
        if (userGroup.getType() == null)
            userGroup.setType(GroupType.PRIVATE);

        AccountController accountController = new AccountController();
        if (userGroup.getType() == GroupType.PUBLIC && !accountController.isAdministrator(userId)) {
            String errMsg = "Non admin '" + userId + "' attempting to create public group";
            Logger.error(errMsg);
            throw new PermissionException(errMsg);
        }

        Account account = accountDAO.getByEmail(userId);
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
        for (AccountTransfer accountTransfer : userGroup.getMembers()) {
            Account memberAccount = accountDAO.getByEmail(accountTransfer.getEmail());
            if (memberAccount == null)
                continue;

            memberAccount.getGroups().add(group);
            accountDAO.update(memberAccount);
        }

        // add remote members
        RemotePartnerDAO remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        for (RemoteUser remoteUser : userGroup.getRemoteMembers()) {
            RegistryPartner partner = remoteUser.getPartner();
            if (partner == null)
                continue;

            RemotePartner remotePartner = remotePartnerDAO.get(partner.getId());
            if (remotePartner == null)
                continue;

            AccountTransfer accountTransfer = remoteUser.getUser();
            if (accountTransfer == null || StringUtils.isEmpty(accountTransfer.getEmail()))
                continue;

            ClientModel clientModel = new ClientModel();
            clientModel.setEmail(accountTransfer.getEmail());
            clientModel.setGroup(group);
            clientModel.setRemotePartner(remotePartner);
            DAOFactory.getClientModelDAO().create(clientModel); // todo : check and retrieve first
        }

        return group.toDataTransferObject();
    }
}
