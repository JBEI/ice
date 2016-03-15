package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entries that have been shared with the specified user
 * or with groups that the specified user is a member of
 *
 * @author Hector Plahar
 */
public class SharedEntries {

    private final Account account;
    private final EntryDAO entryDAO;

    public SharedEntries(String userId) {
        this.account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (this.account == null)
            throw new IllegalArgumentException("Cannot retrieve account for \"" + userId + "\"");
        this.entryDAO = DAOFactory.getEntryDAO();
    }

    public long getNumberOfEntries(String filter) {
        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> accountGroups = account.getGroups();
        accountGroups.remove(publicGroup);
        return this.entryDAO.sharedEntryCount(account, accountGroups, filter);
    }

    public List<PartData> getEntries(ColumnField field, boolean asc, int start, int limit, String filter) {
        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> accountGroups = account.getGroups();
        accountGroups.remove(publicGroup);
        List<Entry> entries = this.entryDAO.sharedWithUserEntries(account, accountGroups, field, asc, start, limit, filter);

        ArrayList<PartData> data = new ArrayList<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(account.getEmail(), entry, false);
            info.setViewCount(DAOFactory.getAuditDAO().getHistoryCount(entry));
            data.add(info);
        }
        return data;
    }
}
