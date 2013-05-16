package org.jbei.ice.lib.entry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageDAO;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

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
    private AttachmentController attachmentController;
    private SampleController sampleController;
    private SequenceAnalysisController sequenceAnalysisController;
    private SequenceController sequenceController;

    public EntryController() {
        dao = new EntryDAO();
        commentDAO = new CommentDAO();
        permissionsController = ControllerFactory.getPermissionController();
        accountController = ControllerFactory.getAccountController();
        attachmentController = ControllerFactory.getAttachmentController();
        sampleController = ControllerFactory.getSampleController();
        sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();
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
        Set<PartNumber> partNumbers = new LinkedHashSet<>();
        partNumbers.add(partNumber);
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
            entry.setBioSafetyLevel(new Integer(0));

        if (entry.getLongDescriptionType() == null)
            entry.setLongDescriptionType("text");

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
        for (Group group : ControllerFactory.getGroupController().getAllPublicGroupsForAccount(account)) {
            PermissionInfo permissionInfo = new PermissionInfo();
            permissionInfo.setType(PermissionInfo.Type.READ_ENTRY);
            permissionInfo.setTypeId(entry.getId());
            permissionInfo.setArticle(PermissionInfo.Article.GROUP);
            permissionInfo.setArticleId(group.getId());
            permissionInfo.setDisplay(group.getLabel());
            permissionsController.addPermission(account, permissionInfo);
        }

        if (sequenceController.hasSequence(entry)) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    public Entry recordEntry(Entry entry, ArrayList<PermissionInfo> permissions) throws ControllerException {
        entry.setId(0);
        PartNumber partNumber = new PartNumber();
        String nextPart = getNextPartNumber();
        partNumber.setPartNumber(nextPart);
        Set<PartNumber> partNumbers = new LinkedHashSet<>();
        partNumbers.add(partNumber);
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
            entry.setBioSafetyLevel(new Integer(0));

        if (entry.getLongDescriptionType() == null)
            entry.setLongDescriptionType("text");

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

        if (sequenceController.hasSequence(entry)) {
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
     * @throws PermissionException
     */
    public Entry get(Account account, long id) throws ControllerException, PermissionException {
        Entry entry;

        try {
            entry = dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException(account.getEmail() + ": No read permission for entry " + id);
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

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    public Entry getPublicEntryByRecordId(String recordId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        Group publicGroup = ControllerFactory.getGroupController().createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);
        if (entry != null && !permissionsController.groupHasReadPermission(groups, entry)) {
            String errMsg = "Entry " + recordId + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }

        return entry;
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
    public Entry getByPartNumber(Account account, String partNumber) throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
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
    public Entry getByName(Account account, String name) throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.getByName(name);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    public FolderDetails retrieveVisibleEntries(Account account, ColumnField field, boolean asc, int start, int limit)
            throws ControllerException {
        LinkedList<Entry> results;
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

    public Entry update(Account account, Entry entry, ArrayList<PermissionInfo> permissions)
            throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to update null entry!");
        }

        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        boolean scheduleRebuild = sequenceController.hasSequence(entry);
        Entry savedEntry;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            savedEntry = dao.updateEntry(entry);

//            if (permissions != null && !permissions.isEmpty()) {
//                permissionsController.clearPermissions(account, entry);
//                for (PermissionInfo permissionInfo : permissions) {
//                    permissionInfo.setTypeId(entry.getId());
//                    permissionsController.addPermission(account, permissionInfo);
//                }
//            }

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
     * @param entry
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Entry entry) throws ControllerException, PermissionException {
        // TODO : check status and if draft, actually delete. not just mark for delete
        boolean schedule = sequenceController.hasSequence(entry);
        delete(account, entry, schedule);
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
     * @param ids
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
            ArrayList<Entry> results = new ArrayList<>(dao.getEntriesByIdSet(filtered));
            return results;
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

    public EntryInfo retrieveEntryDetails(Account account, Entry entry) throws ControllerException {
        ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
        ArrayList<Sample> samples = sampleController.getSamplesByEntry(entry);
        List<TraceSequence> sequences = sequenceAnalysisController.getTraceSequences(entry);

        // samples
        Map<Sample, LinkedList<Storage>> sampleMap = new HashMap<>();
        for (Sample sample : samples) {
            Storage storage = sample.getStorage();
            LinkedList<Storage> storageList = new LinkedList<>();
            List<Storage> storages = StorageDAO.getStoragesUptoScheme(storage);
            if (storages != null)
                storageList.addAll(storages);
            Storage scheme = StorageDAO.getSchemeContainingParentStorage(storage);
            if (scheme != null)
                storageList.add(scheme);

            sampleMap.put(sample, storageList);
        }

        boolean hasSequence = sequenceController.hasSequence(entry);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry);
        EntryInfo info = ModelToInfoFactory.getInfo(account, entry, attachments, sampleMap, sequences, hasSequence,
                                                    hasOriginalSequence);

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
            URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
            if (uri != null) {
                info.setSbolVisualURL(uri.toString());
            }
        }

        return info;
    }
}
