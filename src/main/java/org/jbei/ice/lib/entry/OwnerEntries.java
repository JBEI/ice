package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entries for specified owner
 *
 * @author Hector Plahar
 */
public class OwnerEntries {

    private final Account ownerAccount;
    private final Account account;
    private final EntryDAO entryDAO;
    private final boolean isAdmin;
    private final boolean isSelf;       // requester is same as owner
    private final AccountDAO accountDAO;

    public OwnerEntries(String userId, long id) {
        this.accountDAO = DAOFactory.getAccountDAO();
        this.account = this.accountDAO.getByEmail(userId);
        this.ownerAccount = this.accountDAO.get(id);
        if (this.ownerAccount == null)
            throw new IllegalArgumentException("Cannot retrieve account with id  \"" + id + "\"");
        this.entryDAO = DAOFactory.getEntryDAO();
        EntryAuthorization entryAuthorization = new EntryAuthorization();
        this.isAdmin = entryAuthorization.isAdmin(userId);
        this.isSelf = userId.equalsIgnoreCase(this.ownerAccount.getEmail());
    }

    /**
     * @param userId     userId for account making request
     * @param ownerEmail userId for account whose entries are to be retrieved
     */
    public OwnerEntries(String userId, String ownerEmail) {
        this.accountDAO = DAOFactory.getAccountDAO();
        this.account = this.accountDAO.getByEmail(userId);
        this.ownerAccount = this.accountDAO.getByEmail(ownerEmail);
        if (this.ownerAccount == null)
            throw new IllegalArgumentException("Cannot retrieve account for \"" + ownerEmail + "\"");
        this.entryDAO = DAOFactory.getEntryDAO();
        EntryAuthorization entryAuthorization = new EntryAuthorization();
        this.isAdmin = entryAuthorization.isAdmin(userId);
        this.isSelf = userId.equalsIgnoreCase(ownerEmail);
    }

    public List<PartData> retrieveOwnerEntries(ColumnField sort, boolean asc, int start, int limit, String filter) {
        List<Entry> entries;

        if (this.isAdmin || this.isSelf) {
            entries = entryDAO.retrieveOwnerEntries(this.ownerAccount.getEmail(), sort, asc, start, limit, filter);
        } else {
            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = new GroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            // retrieve entries for user that can be read by others
            entries = entryDAO.retrieveUserEntries(account, this.ownerAccount.getEmail(),
                    accountGroups, sort, asc, start, limit, filter);
        }

        ArrayList<PartData> data = new ArrayList<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(account.getEmail(), entry, false);
            info.setViewCount(DAOFactory.getAuditDAO().getHistoryCount(entry));
            data.add(info);
        }
        return data;
    }

    public long getNumberOfOwnerEntries() {
        if (this.isAdmin || this.isSelf) {
            return entryDAO.ownerEntryCount(ownerAccount.getEmail());
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return entryDAO.ownerEntryCount(account, ownerAccount.getEmail(), accountGroups);
    }
}
