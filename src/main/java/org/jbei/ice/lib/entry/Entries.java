package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.dao.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.dao.PermissionDAO;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class Entries {

    private final EntryDAO dao;

    public Entries() {
        this.dao = DAOFactory.getEntryDAO();
    }

    public boolean updateVisibility(String userId, List<Long> entryIds, Visibility visibility) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Set<Group> accountGroups = new GroupController().getAllGroups(account);
        PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
        if (!new AccountController().isAdministrator(userId) && !permissionDAO.canWrite(account, accountGroups, entryIds))
            return false;

        for (long entryId : entryIds) {
            Entry entry = dao.get(entryId);
            if (entry.getVisibility() == visibility.getValue())
                continue;

            entry.setVisibility(visibility.getValue());
            dao.update(entry);
        }

        return true;
    }
}
