package org.jbei.ice.lib.collection;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.SharedEntries;
import org.jbei.ice.lib.entry.VisibleEntries;
import org.jbei.ice.lib.folder.CollectionCounts;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents collections in the system
 *
 * @author Hector Plahar
 */
public class Collections {

    private final Account account;

    public Collections(String userId) {
        this.account = DAOFactory.getAccountDAO().getByEmail(userId);
    }

    public CollectionCounts getAllCounts() {
        String userId = this.account.getEmail();
        EntryDAO entryDAO = DAOFactory.getEntryDAO();
        CollectionCounts collection = new CollectionCounts();
        VisibleEntries visibleEntries = new VisibleEntries(userId);
        collection.setAvailable(visibleEntries.getEntryCount());
        collection.setDeleted(entryDAO.getDeletedCount(userId));

        collection.setPersonal(getNumberOfOwnerEntries(userId));
        SharedEntries sharedEntries = new SharedEntries(userId);
        collection.setShared(sharedEntries.getNumberofEntries());
        collection.setDrafts(entryDAO.getByVisibilityCount(userId, Visibility.DRAFT));
        if (account.getType() == AccountType.ADMIN)
            collection.setPending(entryDAO.getPendingCount());
        return collection;
    }

    protected long getNumberOfOwnerEntries(String ownerEmail) {
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        return DAOFactory.getEntryDAO().ownerEntryCount(account, ownerEmail, accountGroups);
    }
}
