package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.dao.EntryDAO;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class EntryRetriever {

    private final EntryDAO dao;
    private final EntryAuthorization authorization;

    public EntryRetriever() {
        this.dao = DAOFactory.getEntryDAO();
        authorization = new EntryAuthorization();
    }

    public String getPartNumber(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry.getPartNumber();
    }

    protected Entry getEntry(String id) {
        Entry entry = null;

        // check if numeric
        try {
            entry = dao.get(Long.decode(id));
        } catch (NumberFormatException nfe) {
            // fine to ignore
        }

        // check for part Id
        if (entry == null)
            entry = dao.getByPartNumber(id);

        // check for global unique id
        if (entry == null)
            entry = dao.getByRecordId(id);

        return entry;
    }

    public ArrayList<AccessPermission> getEntryPermissions(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        // viewing permissions requires write permissions
        authorization.expectWrite(userId, entry);

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();
        Set<Permission> permissions = DAOFactory.getPermissionDAO().getEntryPermissions(entry);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        for (Permission permission : permissions) {
            if (permission.getAccount() == null && permission.getGroup() == null)
                continue;
            if (permission.getGroup() != null && permission.getGroup() == publicGroup)
                continue;
            accessPermissions.add(permission.toDataTransferObject());
        }

        return accessPermissions;
    }

    // return list of part data with only partId and id filled in
    public ArrayList<PartData> getMatchingPartNumber(String token, int limit) {
        if (token == null)
            return new ArrayList<>();

        token = token.replaceAll("'", "");
        ArrayList<PartData> dataList = new ArrayList<>();
        for (Entry entry : dao.getMatchingEntryPartNumbers(token, limit, null)) {
            EntryType type = EntryType.nameToType(entry.getRecordType());
            PartData partData = new PartData(type);
            partData.setId(entry.getId());
            partData.setPartId(entry.getPartNumber());
            partData.setName(entry.getName());
            dataList.add(partData);
        }
        return dataList;
    }

    public Set<String> getMatchingAutoCompleteField(AutoCompleteField field, String token, int limit) {
        token = token.replaceAll("'", "");
        Set<String> results;
        switch (field) {
            case SELECTION_MARKERS:
                results = dao.getMatchingSelectionMarkers(token, limit);
                break;

            case ORIGIN_OF_REPLICATION:
                results = dao.getMatchingOriginOfReplication(token, limit);
                break;

            case PROMOTERS:
                results = dao.getMatchingPromoters(token, limit);
                break;

            case REPLICATES_IN:
                results = dao.getMatchingReplicatesIn(token, limit);
                break;

            case PLASMID_NAME:
                results = dao.getMatchingPlasmidPartNumbers(token, limit);
                break;

            case PLASMID_PART_NUMBER:
                results = dao.getMatchingPlasmidPartNumbers(token, limit);
                break;

            default:
                results = new HashSet<>();
        }

        // process to remove commas
        HashSet<String> individualResults = new HashSet<>();
        for (String result : results) {
            for (String split : result.split(",")) {
                individualResults.add(split.trim());
            }
        }
        return individualResults;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param userId account identifier of user performing action
     * @param id     unique local identifier for entry
     * @return entry retrieved from the database.
     */
    public Entry get(String userId, long id) {
        Entry entry = dao.get(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry;
    }

    public String getEntrySummary(long id) {
        return dao.getEntrySummary(id);
    }

    public List<Long> getEntriesFromSelectionContext(String userId, EntrySelection context) {
        boolean all = context.isAll();
        EntryType entryType = context.getEntryType();

        switch (context.getSelectionType()) {
            default:
            case FOLDER:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    long folderId = Long.decode(context.getFolderId());
                    return getFolderEntries(userId, folderId, all, entryType);
                }

            case SEARCH:
                // todo
                break;

            case COLLECTION:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    return getCollectionEntries(userId, context.getFolderId(), all, entryType);
                }
        }

        return null;
    }

    protected List<Long> getCollectionEntries(String userId, String collection, boolean all, EntryType type) {
        List<Long> entries = null;
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

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
    protected List<Long> getFolderEntries(String userId, long folderId, boolean all, EntryType type) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectRead(userId, folder);

        if (all)
            type = null;
        return DAOFactory.getFolderDAO().getFolderContentIds(folderId, type);
    }
}
