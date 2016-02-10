package org.jbei.ice.lib.group;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class UserGroups {

    private final GroupDAO dao;
    private final Account account;
    private final AccountDAO accountDAO;

    public UserGroups(String userId) {
        this.dao = DAOFactory.getGroupDAO();
        accountDAO = DAOFactory.getAccountDAO();
        this.account = accountDAO.getByEmail(userId);
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
        Account userIdAccount = accountDAO.get(userId);

        // TODO : account authorization
        if (!accountController.isAdministrator(account.getEmail()) && account.getId() != userIdAccount.getId())
            return null;

        List<Group> result = dao.retrieveMemberGroups(account);
        Results<UserGroup> groupResults = new Results<>();

        for (Group group : result) {
            UserGroup user = group.toDataTransferObject();
            long count = dao.getMemberCount(group.getUuid());

            // get clients
            count += DAOFactory.getClientModelDAO().getClientCount(group);
            user.setMemberCount(count);
            groupResults.getData().add(user);
        }
        return groupResults;
    }
}
