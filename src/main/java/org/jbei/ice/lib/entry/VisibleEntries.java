package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entries that a specified user has read permissions on by virtue of account status (e.g. an admin can see all entries)
 * or access permissions granted to that account or group that the account belongs to
 *
 * @author Hector Plahar
 */
public class VisibleEntries {

    private final Account account;
    private final boolean isAdmin;
    private final EntryDAO dao;
    private final Group everybodyGroup;

    public VisibleEntries(String userId) {
        this.account = DAOFactory.getAccountDAO().getByEmail(userId);
        EntryAuthorization authorization = new EntryAuthorization();
        // todo consider using public entries for visible entries without user id
        if (account == null)
            throw new IllegalArgumentException("Cannot retrieve account for \"" + userId + "\"");
        this.isAdmin = authorization.isAdmin(userId);
        this.dao = DAOFactory.getEntryDAO();
        this.everybodyGroup = new GroupController().createOrRetrievePublicGroup();
    }

    public List<PartData> getEntries(ColumnField field, boolean asc, int start, int limit, String filter, List<String> fields) {
        List<Long> resultIds;

        if (isAdmin) {
            // no filters
            resultIds = dao.retrieveAllEntryIds(field, asc, start, limit, filter);
        } else {
            // retrieve groups for account and filter by permission
            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            accountGroups.add(everybodyGroup);
            resultIds = dao.retrieveVisibleEntries(account, accountGroups, field, asc, start, limit, filter);
        }

        ArrayList<PartData> data = new ArrayList<>();
        for (Long id : resultIds) {
            PartData info = ModelToInfoFactory.createTableView(id, fields);
            data.add(info);
        }

        return data;
    }

    /**
     * Retrieve the number of entries that is visible to a particular user
     *
     * @return Number of entries that user with account referenced in the parameter can read.
     */
    public long getEntryCount(String filter) {
        if (isAdmin) {
            return dao.getAllEntryCount(filter);
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        accountGroups.add(everybodyGroup);
        return dao.visibleEntryCount(account, accountGroups, filter);
    }

    public long getEntryCount() {
        return this.getEntryCount(null);
    }
}
