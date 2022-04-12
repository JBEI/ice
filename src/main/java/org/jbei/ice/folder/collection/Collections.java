package org.jbei.ice.folder.collection;

import org.jbei.ice.account.AccountType;
import org.jbei.ice.dto.entry.Visibility;
import org.jbei.ice.dto.folder.FolderDetails;
import org.jbei.ice.dto.folder.FolderType;
import org.jbei.ice.entry.SharedEntries;
import org.jbei.ice.entry.VisibleEntries;
import org.jbei.ice.folder.CollectionCounts;
import org.jbei.ice.folder.FolderController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.AccountModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the fixed collections supported in the eco-system
 *
 * @author Hector Plahar
 */
public class Collections {

    private final AccountModel account;

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

        // get sample entries (this is determined by folder membership
        long size = DAOFactory.getFolderDAO().getEntryCountByFolderType(FolderType.SAMPLE, null);
        collection.setSamples(size);
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
            case AVAILABLE:
                return controller.getAvailableFolders(userId);

            case DRAFTS:
                return controller.getBulkUploadDrafts(userId);

            case PENDING:
                return controller.getPendingBulkUploads(userId);

            case SHARED:
                return controller.getSharedUserFolders(userId);

            case TRANSFERRED:
                return controller.getTransferredFolders(userId);

            case SAMPLES:
                return controller.getSampleFolders(userId);

            case DELETED:
                // not able to delete folders under the deleted collections yet
                return new ArrayList<>();

            default:
                throw new IllegalArgumentException("Unknown collection type " + type);
        }
    }
}
