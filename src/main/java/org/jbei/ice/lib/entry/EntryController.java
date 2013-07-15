package org.jbei.ice.lib.entry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.shared.AutoCompleteField;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.ServiceException;

/**
 * ABI to manipulate {@link org.jbei.ice.lib.entry.model.Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class EntryController {

    private EntryDAO dao;
    private CommentDAO commentDAO;
    private PermissionsController permissionsController;
    private AccountController accountController;
    private SequenceController sequenceController;

    public EntryController() {
        dao = new EntryDAO();
        commentDAO = new CommentDAO();
        permissionsController = ControllerFactory.getPermissionController();
        accountController = ControllerFactory.getAccountController();
        sequenceController = ControllerFactory.getSequenceController();
    }

    public Set<String> getMatchingAutoCompleteField(AutoCompleteField field, String token, int limit)
            throws ControllerException {
        try {
            switch (field) {
                case SELECTION_MARKERS:
                    return dao.getMatchingSelectionMarkers(token, limit);

                case ORIGIN_OF_REPLICATION:
                    return dao.getMatchingOriginOfReplication(token, limit);

                case PROMOTERS:
                    return dao.getMatchingPromoters(token, limit);

                case PLASMID_NAME:
                    return dao.getMatchingPlasmidNames(token, limit);

                default:
                    return new HashSet<>();
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Retrieves the IDs of all part records in the system
     *
     * @return list of ids
     * @throws ControllerException on DAOException retrieving the IDs
     */
    public LinkedList<Long> getAllEntryIds() throws ControllerException {
        try {
            return dao.getAllEntryIds();
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public String getEntrySummary(long id) throws ControllerException {
        try {
            return dao.getEntrySummary(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public HashSet<Entry> createStrainWithPlasmid(Account account, Entry strain, Entry plasmid,
            ArrayList<PermissionInfo> permissions) throws ControllerException {
        if (strain == null || plasmid == null)
            throw new ControllerException("Cannot create null entries");

        HashSet<Entry> results = new HashSet<>();
        plasmid = createEntry(account, plasmid, permissions);
        results.add(plasmid);
        String plasmidPartNumberString = "[[" + Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX) + ":"
                + plasmid.getOnePartNumber().getPartNumber() + "|" + plasmid.getOneName().getName() + "]]";
        ((Strain) strain).setPlasmids(plasmidPartNumberString);
        strain = createEntry(account, strain, permissions);
        results.add(strain);
        return results;
    }

    /**
     * Generate the next part number string using system settings.
     *
     * @return The next part number.
     * @throws ControllerException
     */
    private String getNextPartNumber() throws ControllerException {
        try {
            return dao.generateNextPartNumber(Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX),
                                              Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DELIMITER),
                                              Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX));
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Entry createEntry(Account account, Entry entry) throws ControllerException {
        return createEntry(account, entry, null);
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account     account of user creating entry
     * @param entry       entry record being created
     * @param permissions list of permissions to associate with created entry
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Account account, Entry entry, ArrayList<PermissionInfo> permissions)
            throws ControllerException {
        PartNumber partNumber = new PartNumber();
        String nextPart = getNextPartNumber();
        partNumber.setPartNumber(nextPart);
        entry.getPartNumbers().add(partNumber);

        partNumber.setEntry(entry);

        entry.setRecordId(Utils.generateUUID());
        entry.setVersionId(entry.getRecordId());
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());
        entry.setOwner(account.getFullName());
        entry.setOwnerEmail(account.getEmail());

        if (entry.getSelectionMarkers() != null) {
            for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                selectionMarker.setEntry(entry);
            }
        }

        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
                link.setEntry(entry);
            }
        }

        if (entry.getNames() != null) {
            for (Name name : entry.getNames()) {
                name.setEntry(entry);
            }
        }

        if (entry.getPartNumbers() != null) {
            for (PartNumber pNumber : entry.getPartNumbers()) {
                pNumber.setEntry(entry);
            }
        }

        if (entry.getStatus() == null)
            entry.setStatus("");

        if (entry.getBioSafetyLevel() == null)
            entry.setBioSafetyLevel(0);

        entry.setModificationTime(entry.getCreationTime());

        try {
            entry = dao.saveEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        // add write permissions for owner
        PermissionInfo info = new PermissionInfo(PermissionInfo.Article.ACCOUNT, account.getId(),
                                                 PermissionInfo.Type.WRITE_ENTRY, entry.getId(), account.getFullName());
        permissionsController.addPermission(account, info);

        if (permissions != null) {
            for (PermissionInfo permissionInfo : permissions) {
                permissionInfo.setTypeId(entry.getId());
                permissionsController.addPermission(account, permissionInfo);
            }
        }

        // retrieve all public groups that this user is a part of an assign read permissions to those groups
        // for this entry
//        for (Group group : ControllerFactory.getGroupController().getAllPublicGroupsForAccount(account)) {
//            PermissionInfo permissionInfo = new PermissionInfo();
//            permissionInfo.setType(PermissionInfo.Type.READ_ENTRY);
//            permissionInfo.setTypeId(entry.getId());
//            permissionInfo.setArticle(PermissionInfo.Article.GROUP);
//            permissionInfo.setArticleId(group.getId());
//            permissionInfo.setDisplay(group.getLabel());
//            permissionsController.addPermission(account, permissionInfo);
//        }

        if (sequenceController.hasSequence(entry.getId())) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    public Entry recordEntry(Entry entry, ArrayList<PermissionInfo> permissions) throws ControllerException {
        entry.setId(0);
        PartNumber partNumber = new PartNumber();
        String nextPart = getNextPartNumber();
        partNumber.setPartNumber(nextPart);
        entry.getPartNumbers().add(partNumber);

        partNumber.setEntry(entry);

        if (entry.getRecordId() == null) {
            entry.setRecordId(Utils.generateUUID());
            entry.setVersionId(entry.getRecordId());
        }

        if (entry.getVersionId() == null) {
            entry.setVersionId(entry.getRecordId());
        }

        if (entry.getCreationTime() == null) {
            entry.setCreationTime(Calendar.getInstance().getTime());
            entry.setModificationTime(entry.getCreationTime());
        }

        if (entry.getSelectionMarkers() != null) {
            for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                selectionMarker.setEntry(entry);
            }
        }

        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
                link.setEntry(entry);
            }
        }

        if (entry.getNames() != null) {
            for (Name name : entry.getNames()) {
                name.setEntry(entry);
            }
        }

        if (entry.getPartNumbers() != null) {
            for (PartNumber pNumber : entry.getPartNumbers()) {
                pNumber.setEntry(entry);
            }
        }

        if (entry.getStatus() == null)
            entry.setStatus("");

        if (entry.getBioSafetyLevel() == null)
            entry.setBioSafetyLevel(0);

        try {
            entry = dao.saveEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (permissions != null) {
            for (PermissionInfo permissionInfo : permissions) {
                permissionInfo.setTypeId(entry.getId());
                permissionsController.addPermission(accountController.getSystemAccount(), permissionInfo);
            }
        }

        if (sequenceController.hasSequence(entry.getId())) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param account account of user performing action
     * @param id      unique local identifier for entry
     * @return entry retrieved from the database.
     * @throws ControllerException
     */
    public Entry get(Account account, long id) throws ControllerException {
        Entry entry;

        try {
            entry = dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new ControllerException(account.getEmail() + ": No read permission for entry " + id);
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by recordId (uuid).
     *
     * @param recordId universally unique identifier that was assigned to entry on create
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByRecordId(Account account, String recordId) throws ControllerException, PermissionException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;

//        EntryInfo info = ModelToInfoFactory.getInfo(account, entry, null, null, null);
//        boolean hasSequence = sequenceController.hasSequence(entry.getId());
//        info.setHasSequence(hasSequence);
//        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
//        info.setHasOriginalSequence(hasOriginalSequence);
//        return info;
    }

    public FeaturedDNASequence getPublicSequence(String recordId) throws ControllerException {
        Entry entry;
        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry)) {
            String errMsg = "Entry " + recordId + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }
        return sequenceController.sequenceToDNASequence(sequenceController.getByEntry(entry));
    }

    public EntryInfo getPublicEntryByRecordId(String recordId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry)) {
            String errMsg = "Entry " + recordId + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }

        EntryInfo info = ModelToInfoFactory.getInfo(null, entry, null, null, null);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public EntryInfo getPublicEntryById(long id) throws ControllerException {
        Entry entry;

        try {
            entry = dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry)) {
            String errMsg = "Entry " + id + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }

        EntryInfo info = ModelToInfoFactory.getInfo(null, entry, null, null, null);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    /**
     * Retrieve {@link Entry} from the database by part number.
     * <p/>
     * Throws exception if multiple entries have the same part number.
     *
     * @param partNumber entry part number
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public EntryInfo getByPartNumber(Account account, String partNumber) throws ControllerException,
            PermissionException {
        Entry entry;
        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        EntryInfo info = ModelToInfoFactory.getInfo(account, entry, null, null, null);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    //TODO hack for BulkUploadUtil that needs Entry Object.
    public Entry getEntryByPartNumber(Account account, String partNumber) throws ControllerException {
        Entry entry;
        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new ControllerException("No read permission for entry!");
        }
        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by name.
     * <p/>
     * Throws exception if multiple entries have the same name.
     *
     * @param name entry name
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public EntryInfo getByUniqueName(Account account, String name) throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.getByUniqueName(name);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        EntryInfo info = ModelToInfoFactory.getInfo(account, entry, null, null, null);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public FolderDetails retrieveVisibleEntries(Account account, ColumnField field, boolean asc, int start, int limit)
            throws ControllerException {
        Set<Entry> results;
        FolderDetails details = new FolderDetails();
        try {
            if (accountController.isAdministrator(account)) {
                // no filters
                results = dao.retrieveAllEntries(field, asc, start, limit);
            } else {
                // retrieve groups for account and filter by permission
                Set<Group> accountGroups = new HashSet<>(account.getGroups());
                GroupController controller = ControllerFactory.getGroupController();
                Group everybodyGroup = controller.createOrRetrievePublicGroup();
                accountGroups.add(everybodyGroup);
                results = dao.retrieveVisibleEntries(account, accountGroups, field, asc, start, limit);
            }

            for (Entry entry : results) {
                EntryInfo info = ModelToInfoFactory.createTableViewData(entry, false);
                details.getEntries().add(info);
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return details;
    }

    /**
     * Retrieve the number of entries that is visible to a particular user
     *
     * @param account user account
     * @return Number of entries that user with account referenced in the parameter can read.
     * @throws ControllerException
     */
    public long getNumberOfVisibleEntries(Account account) throws ControllerException {
        if (accountController.isAdministrator(account)) {
            try {
                return dao.getAllEntryCount();
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = ControllerFactory.getGroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        try {
            return dao.visibleEntryCount(account, accountGroups);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public List<Entry> retrieveOwnerEntries(Account account, String ownerEmail,
            ColumnField sort, boolean asc, int start, int limit) throws ControllerException {
        try {
            if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
                return dao.retrieveOwnerEntries(ownerEmail, sort, asc, start, limit);
            }

            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = ControllerFactory.getGroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            return dao.retrieveUserEntries(account, ownerEmail, accountGroups, sort, asc, start, limit);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public long getNumberOfOwnerEntries(Account account, String ownerEmail) throws ControllerException {
        try {
            if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
                return dao.ownerEntryCount(ownerEmail);
            }

            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = ControllerFactory.getGroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            return dao.ownerEntryCount(account, ownerEmail, accountGroups);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Entry update(Account account, Entry entry) throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to update null entry!");
        }

        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        boolean scheduleRebuild = sequenceController.hasSequence(entry.getId());
        Entry savedEntry;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            savedEntry = dao.updateEntry(entry);

            if (scheduleRebuild) {
                ApplicationController.scheduleBlastIndexRebuildTask(true);
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return savedEntry;
    }

    /**
     * Delete the entry in the database. Schedule an index rebuild.
     *
     * @param entryId unique identifier for entry to be deleted
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, long entryId) throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.get(entryId);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
        boolean schedule = sequenceController.hasSequence(entry.getId());
//        if (entry.getVisibility() == Visibility.DRAFT.getValue())   TODO
//            fullDelete(entry, schedule);
//        else
        delete(account, entry, schedule);
    }

    /**
     * Experimental. Do not use
     * Performs a full deletion of the entry, not just marking it as deleted.
     *
     * @param entry Entry to be deleted
     * @throws ControllerException
     */
    protected void fullDelete(Entry entry, boolean schedule) throws ControllerException {
        if (entry == null)
            return;

        try {
            dao.fullDelete(entry);
            if (schedule) {
                ApplicationController.scheduleBlastIndexRebuildTask(true);
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Delete the entry in the database. Optionally schedule an index rebuild.
     *
     * @param entry                entry to deleted
     * @param scheduleIndexRebuild True if index rebuild is scheduled.
     * @throws ControllerException
     * @throws PermissionException
     */
    private void delete(Account account, Entry entry, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry");
        }
        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry");
        }
        String deletionString = "This entry is deleted. It was owned by " + entry.getOwnerEmail();
        entry.setLongDescription(deletionString + entry.getLongDescription());
        Account sysAccount = accountController.getSystemAccount();
        entry.setOwnerEmail(sysAccount.getEmail());

        permissionsController.clearPermissions(sysAccount, entry);
        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            dao.update(entry);
        } catch (DAOException e1) {
            throw new ControllerException("Failed to save entry deletion", e1);
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }
    }

    /**
     * Filter {@link Entry} id's for display.
     * <p/>
     * Given a List of entry id's, keep only id's that user has read access to.
     *
     * @param account user account
     * @param ids     list of entry ids
     * @return List of Entry ids.
     * @throws ControllerException
     */
    List<Long> filterEntriesByPermission(Account account, List<Long> ids) throws ControllerException {
        ArrayList<Long> result = new ArrayList<>();
        for (Long id : ids) {
            Entry entry;
            try {
                entry = dao.get(id);
            } catch (DAOException e) {
                Logger.error(e);
                continue;
            }

            if (permissionsController.hasReadPermission(account, entry)) {
                result.add(id);
            }
        }
        return result;
    }

    public ArrayList<Entry> getEntriesByIdSet(Account account, ArrayList<Long> queryResultIds)
            throws ControllerException {
        List<Long> filtered = this.filterEntriesByPermission(account, queryResultIds);
        try {
            return new ArrayList<>(dao.getEntriesByIdSet(filtered));
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public HashSet<Long> retrieveStrainsForPlasmid(Plasmid plasmid) throws ControllerException {
        try {
            return dao.retrieveStrainsForPlasmid(plasmid);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public UserComment addCommentToEntry(Account account, UserComment userComment) throws ControllerException {
        Entry entry = get(account, userComment.getEntryId());
        Comment comment = new Comment(entry, account, userComment.getMessage());
        try {
            comment = commentDAO.save(comment);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return Comment.toDTO(comment);
    }

    public EntryInfo retrieveEntryTipDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
        try {
            EntryInfo info = api.getPublicEntryById(entryId);
            boolean hasSequence = api.hasSequence(info.getRecordId());
            info.setHasSequence(hasSequence);
            boolean hasOriginalSequence = api.hasOriginalSequence(info.getRecordId());
            info.setHasOriginalSequence(hasOriginalSequence);
            return info;
        } catch (ServiceException se) {
            Logger.error(se);
            throw new ControllerException(se);
        }
    }

    public EntryInfo retrieveEntryTipDetails(Account account, long entryId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.get(entryId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return ModelToInfoFactory.createTipView(account, entry);
    }

    public EntryInfo retrieveEntryDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
        try {
            EntryInfo info = api.getPublicEntryById(entryId);
            boolean hasSequence = api.hasSequence(info.getRecordId());
            info.setHasSequence(hasSequence);
            boolean hasOriginalSequence = api.hasOriginalSequence(info.getRecordId());
            info.setHasOriginalSequence(hasOriginalSequence);
            return info;
        } catch (ServiceException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public EntryInfo retrieveEntryDetails(Account account, long entryId) throws ControllerException {
        Entry entry = get(account, entryId);
        EntryInfo info = ModelToInfoFactory.getInfo(account, entry, null, null, null);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);

        // comments
        ArrayList<Comment> comments = commentDAO.retrieveComments(entry);
        for (Comment comment : comments) {
            info.getComments().add(Comment.toDTO(comment));
        }

        // permissions
        info.setCanEdit(permissionsController.hasWritePermission(account, entry));

        // viewing permissions is restricted to users who have write access
        if (info.isCanEdit()) {
            try {
                ArrayList<PermissionInfo> permissions = permissionsController.retrieveSetEntryPermissions(account,
                                                                                                          entry);
                info.setPermissions(permissions);
            } catch (PermissionException e) {
                Logger.error(e);
            }
        }

        if (hasSequence) {
            Sequence sequence = sequenceController.getByEntry(entry);
            long start = System.currentTimeMillis();
            URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
            Logger.info(account.getEmail() + ": Pigeon took " + (System.currentTimeMillis() - start)
                                + "ms for " + entry.getId());
            if (uri != null) {
                info.setSbolVisualURL(uri.toString());
            }
        }

        return info;
    }

    public boolean requestSample(Account account, long entryID, String form) {
        try {
            Entry entry = dao.get(entryID);
            String email = ControllerFactory.getConfigurationController().
                    getPropertyValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (entry == null || email == null || email.isEmpty()) {
                Logger.error("Entry could not be retrieve for id " + entryID + " or bulk uploader email is not set");
                return false;
            }

            String site = ControllerFactory.getConfigurationController().getPropertyValue(ConfigurationKey.URI_PREFIX);
            StringBuilder body = new StringBuilder();
            body.append("A sample request has been received from ")
                .append(account.getFullName())
                .append(" (")
                .append("https://").append(site)
                .append("/#page=profile;id=").append(account.getId()).append(";s=profile)")
                .append(" for entry ")
                .append(entry.getOnePartNumber().getPartNumber())
                .append(" (https://").append(site).append("/#page=entry;id=").append(entry.getId()).append(")")
                .append(". \n\nThe requested form is ")
                .append(form);
            return Emailer.send(email, ("Sample request for " + entry.getOnePartNumber().getPartNumber()),
                                body.toString());
        } catch (DAOException | ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    // also submits a comment
    public UserComment sendProblemNotification(Account account, long entryId, String msg) {
        try {
            Entry entry = dao.get(entryId);
            String email = ControllerFactory.getConfigurationController().
                    getPropertyValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (entry == null || email == null || email.isEmpty()) {
                Logger.error("Entry could not be retrieve for id " + entryId + " or bulk uploader email is not set");
                return null;
            }

            String site = ControllerFactory.getConfigurationController().getPropertyValue(ConfigurationKey.URI_PREFIX);
            StringBuilder body = new StringBuilder();
            body.append("A problem notification was sent by ")
                .append(account.getFullName())
                .append(" for entry ")
                .append(entry.getOnePartNumber().getPartNumber())
                .append(" (https://").append(site).append("/#page=entry;id=").append(entry.getId()).append(")")
                .append("\n\nMessage:\n\n")
                .append(msg)
                .append("\n\n");
            boolean success = Emailer.send(email, ("Problem alert for " + entry.getOnePartNumber().getPartNumber()),
                                           body.toString());
            if (success) {
                Comment comment = new Comment(entry, account, msg);
                comment = commentDAO.save(comment);
                return Comment.toDTO(comment);
            }
        } catch (DAOException | ControllerException e) {
            Logger.error(e);
        }
        return null;
    }

    public boolean hasWritePermission(Account account, long entryId) throws ControllerException {
        Entry entry;
        try {
            entry = dao.get(entryId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return false;

        return permissionsController.hasWritePermission(account, entry);
    }
}
