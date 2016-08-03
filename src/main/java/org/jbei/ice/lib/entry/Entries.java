package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Group;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class Entries extends HasEntry {

    private final EntryDAO dao;
    private final PermissionDAO permissionDAO;
    private final AccountDAO accountDAO;
    private final EntryAuthorization authorization;
    private final String userId;

    /**
     * @param userId unique identifier for user creating permissions. Must have write privileges on the entry
     *               if one exists
     */
    public Entries(String userId) {
        this.dao = DAOFactory.getEntryDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.userId = userId;
        this.authorization = new EntryAuthorization();
    }

    public boolean updateVisibility(List<Long> entryIds, Visibility visibility) {
        Account account = accountDAO.getByEmail(userId);
        Set<Group> accountGroups = new GroupController().getAllGroups(account);
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

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param id unique local identifier for entry
     * @return entry retrieved from the database.
     */
    public Entry get(long id) {
        Entry entry = dao.get(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry;
    }

    public List<Long> getEntriesFromSelectionContext(EntrySelection context) {
        boolean all = context.isAll();
        EntryType entryType = context.getEntryType();

        if (context.getSelectionType() == null)
            return context.getEntries();

        switch (context.getSelectionType()) {
            default:
            case FOLDER:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    long folderId = Long.decode(context.getFolderId());
                    return getFolderEntries(folderId, all, entryType);
                }

            case SEARCH:
                return getSearchResults(context.getSearchQuery());

            case COLLECTION:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    return getCollectionEntries(context.getFolderId(), all, entryType);
                }
        }
    }

    protected List<Long> getCollectionEntries(String collection, boolean all, EntryType type) {
        List<Long> entries = null;
        Account account = accountDAO.getByEmail(userId);

        switch (collection.toLowerCase()) {
            case "personal":
                if (all)
                    type = null;
                entries = dao.getOwnerEntryIds(userId, type);
                break;
            case "shared":
                entries = dao.sharedWithUserEntryIds(account, account.getGroups());
                break;
            case "available":
                entries = dao.getVisibleEntryIds(account.getType() == AccountType.ADMIN);
                break;
        }

        return entries;
    }

    // todo : folder controller
    protected List<Long> getFolderEntries(long folderId, boolean all, EntryType type) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectRead(userId, folder);

        if (all)
            type = null;

        boolean visibleOnly = folder.getType() != FolderType.TRANSFERRED;
        return DAOFactory.getFolderDAO().getFolderContentIds(folderId, type, visibleOnly);
    }

    protected List<Long> getSearchResults(SearchQuery searchQuery) {
        SearchController searchController = new SearchController();
        SearchResults searchResults = searchController.runSearch(userId, searchQuery);
        // todo : inefficient: have search return ids only
        List<Long> results = new LinkedList<>();
        for (SearchResult result : searchResults.getResults()) {
            results.add(result.getEntryInfo().getId());
        }
        return results;
    }
}
