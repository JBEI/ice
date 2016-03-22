package org.jbei.ice.lib.folder.collection;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.entry.SharedEntries;
import org.jbei.ice.lib.entry.VisibleEntries;
import org.jbei.ice.lib.folder.CollectionCounts;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the fixed collections supported in the eco-system
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

        long ownerEntryCount = DAOFactory.getEntryDAO().ownerEntryCount(userId);
        collection.setPersonal(ownerEntryCount);
        SharedEntries sharedEntries = new SharedEntries(userId);
        collection.setShared(sharedEntries.getNumberOfEntries(null));
        collection.setDrafts(entryDAO.getByVisibilityCount(userId, Visibility.DRAFT, null));

        if (account.getType() != AccountType.ADMIN)
            return collection;

        // admin only options
        collection.setPending(entryDAO.getByVisibilityCount(Visibility.PENDING));
        collection.setTransferred(entryDAO.getByVisibilityCount(Visibility.TRANSFERRED));
        return collection;
    }

    /**
     * Retrieves the sub folders for the specified collection type
     *
     * @param type type of collection whose sub folders are to be retrieved
     * @return list of folders found
     */
    public List<FolderDetails> getSubFolders(CollectionType type) {
        FolderController controller = new FolderController();
        final String userId = this.account.getEmail();

        switch (type) {
            case PERSONAL:
                return controller.getUserFolders(userId);

            case FEATURED:
                return controller.getAvailableFolders(userId);

            case DRAFTS:
                return controller.getBulkUploadDrafts(userId);

            case PENDING:
                return controller.getPendingBulkUploads(userId);

            case SHARED:
                return controller.getSharedUserFolders(userId);

            case TRANSFERRED:
                return controller.getTransferredFolders(userId);

            case DELETED:
                // not able to delete folders under the deleted collections yet
                return new ArrayList<>();

            default:
                throw new IllegalArgumentException("Unknown collection type " + type);
        }
    }
}
