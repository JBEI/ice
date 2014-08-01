package org.jbei.ice.lib.entry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.AuditDAO;
import org.jbei.ice.lib.dao.hibernate.CommentDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.FolderDAO;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dao.hibernate.TraceSequenceDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.PartSample;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.sample.SampleStorage;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.Audit;
import org.jbei.ice.lib.models.AuditType;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.servlet.ModelToInfoFactory;

import org.apache.commons.io.IOUtils;

/**
 * ABI to manipulate {@link org.jbei.ice.lib.entry.model.Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class EntryController {

    private EntryDAO dao;
    private CommentDAO commentDAO;
    private SequenceDAO sequenceDAO;
    private AuditDAO auditDAO;
    private PermissionsController permissionsController;
    private AccountController accountController;
    private final EntryAuthorization authorization;
    private final SequenceAnalysisController sequenceAnalysisController;

    public EntryController() {
        dao = DAOFactory.getEntryDAO();
        commentDAO = DAOFactory.getCommentDAO();
        permissionsController = new PermissionsController();
        sequenceAnalysisController = new SequenceAnalysisController();
        accountController = new AccountController();
        authorization = new EntryAuthorization();
        sequenceDAO = DAOFactory.getSequenceDAO();
        auditDAO = DAOFactory.getAuditDAO();
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
    public PartData getByUniqueName(Account account, String name) throws ControllerException, PermissionException {
        Entry entry = dao.getByUniqueName(name);
        if (entry == null)
            return null;

        authorization.expectRead(account.getEmail(), entry);

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public FolderDetails retrieveVisibleEntries(String userId, ColumnField field, boolean asc, int start, int limit)
            throws ControllerException {
        if (userId == null) {
            return new FolderController().getPublicEntries();
        }

        Set<Entry> results;
        FolderDetails details = new FolderDetails();
        Account account = accountController.getByEmail(userId);

        if (accountController.isAdministrator(account)) {
            // no filters
            results = dao.retrieveAllEntries(field, asc, start, limit);
        } else {
            // retrieve groups for account and filter by permission
            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = new GroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            results = dao.retrieveVisibleEntries(account, accountGroups, field, asc, start, limit);
        }

        for (Entry entry : results) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            info.setCanEdit(authorization.canWrite(account.getEmail(), entry));
            details.getEntries().add(info);
        }

        return details;
    }

    /**
     * Retrieve the number of entries that is visible to a particular user
     *
     * @param userId user account unique identifier
     * @return Number of entries that user with account referenced in the parameter can read.
     */
    public long getNumberOfVisibleEntries(String userId) {
        Account account = accountController.getByEmail(userId);

        if (account == null)
            return -1;

        if (accountController.isAdministrator(account)) {
            return dao.getAllEntryCount();
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.visibleEntryCount(account, accountGroups);
    }

    public long getNumberofEntriesSharedWithUser(String userId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.sharedEntryCount(account, accountGroups);
    }

    public List<PartData> getEntriesSharedWithUser(String userId, ColumnField field, boolean asc, int start,
            int limit) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        List<Entry> entries = dao.sharedWithUserEntries(account, accountGroups, field, asc, start, limit);

        ArrayList<PartData> data = new ArrayList<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            data.add(info);
        }
        return data;
    }

    public List<PartData> retrieveOwnerEntries(String userId, String ownerEmail,
            ColumnField sort, boolean asc, int start, int limit) {
        List<Entry> entries;
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
            entries = dao.retrieveOwnerEntries(ownerEmail, sort, asc, start, limit);
        } else {
            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = new GroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            // retrieve entries for user that can be read by others
            entries = dao.retrieveUserEntries(account, ownerEmail, accountGroups, sort, asc, start, limit);
        }

        ArrayList<PartData> data = new ArrayList<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            data.add(info);
        }
        return data;
    }

    public long getNumberOfOwnerEntries(String requesterUserEmail, String ownerEmail) {
        Account account = DAOFactory.getAccountDAO().getByEmail(requesterUserEmail);
        if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
            return dao.ownerEntryCount(ownerEmail);
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.ownerEntryCount(account, ownerEmail, accountGroups);
    }

    public long updatePart(String userId, long partId, PartData part) {
        Entry existing = dao.get(partId);
        authorization.expectWrite(userId, existing);

        Entry entry = InfoToModelFactory.updateEntryField(part, existing);
        entry.getLinkedEntries().clear();
        if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
            for (PartData data : part.getLinkedParts()) {
                Entry linked = dao.getByPartNumber(data.getPartId());
                if (linked == null)
                    continue;

                if (!authorization.canRead(userId, linked)) {
                    continue;
                }

                entry.getLinkedEntries().add(linked);
            }
        }

        entry.setModificationTime(Calendar.getInstance().getTime());
        Visibility visibility = EntryUtil.validates(part) ? Visibility.OK : Visibility.DRAFT;
        entry.setVisibility(visibility.getValue());
        dao.update(entry);

        return entry.getId();
    }

    public void update(Account account, Entry entry) {
        if (entry == null) {
            return;
        }

        authorization.expectWrite(account.getEmail(), entry);
        boolean scheduleRebuild = sequenceDAO.hasSequence(entry.getId());

        entry.setModificationTime(Calendar.getInstance().getTime());
        if (entry.getVisibility() == null)
            entry.setVisibility(Visibility.OK.getValue());
        dao.update(entry);

        if (scheduleRebuild) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }
    }

    /**
     * Delete the entry in the database. Schedule an index rebuild.
     *
     * @param entryId unique identifier for entry to be deleted
     * @throws ControllerException
     * @throws PermissionException
     */
    public ArrayList<FolderDetails> delete(Account account, long entryId)
            throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.get(entryId);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
        boolean schedule = sequenceDAO.hasSequence(entry.getId());

        FolderController folderController = new FolderController();
        ArrayList<FolderDetails> folderList = new ArrayList<>();
        FolderDAO folderDAO = DAOFactory.getFolderDAO();
        List<Folder> folders = folderDAO.getFoldersByEntry(entry);
        ArrayList<Long> entryIds = new ArrayList<>();
        entryIds.add(entry.getId());
        if (folders != null) {
            for (Folder folder : folders) {
                try {
                    Folder returned = folderController.removeFolderContents(account, folder.getId(), entryIds);
                    FolderDetails details = new FolderDetails(returned.getId(), returned.getName());
                    long size = folderDAO.getFolderSize(folder.getId());
                    details.setCount(size);
                    folderList.add(details);
                } catch (ControllerException me) {
                    Logger.error(me);
                }
            }
        }
        delete(account, entry, schedule);
        return folderList;
    }

    /**
     * Experimental. Do not use
     * Performs a full deletion of the entry, not just marking it as deleted.
     *
     * @param entry Entry to be deleted
     * @throws ControllerException
     */
    protected void fullDelete(Account account, Entry entry, boolean schedule) throws ControllerException {
        if (entry == null)
            return;

        authorization.expectWrite(account.getEmail(), entry);

        if (schedule) {
            Sequence sequence = sequenceDAO.getByEntry(entry);
            if (sequence != null) {
                sequenceDAO.delete(sequence);
            }
        }
        DAOFactory.getPermissionDAO().clearPermissions(entry);
        dao.fullDelete(entry);
        if (schedule) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }
    }

    /**
     * Delete the entry in the database. Optionally schedule an index rebuild.
     *
     * @param entry                entry to deleted
     * @param scheduleIndexRebuild True if index rebuild is scheduled.
     * @throws ControllerException
     */
    private void delete(Account account, Entry entry, boolean scheduleIndexRebuild) throws ControllerException {
        if (entry == null) {
            return;
        }

        authorization.expectWrite(account.getEmail(), entry);

        if (entry.getVisibility() == Visibility.DELETED.getValue()) {
            fullDelete(account, entry, scheduleIndexRebuild);
            return;
        }

        entry.setModificationTime(Calendar.getInstance().getTime());
        entry.setVisibility(Visibility.DELETED.getValue());

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
     */
    List<Long> filterEntriesByPermission(Account account, List<Long> ids) {
        ArrayList<Long> result = new ArrayList<>();
        for (Long id : ids) {
            Entry entry = dao.get(id);
            if (authorization.canRead(account.getEmail(), entry)) {
                result.add(id);
            }
        }
        return result;
    }

    public ArrayList<Entry> getEntriesByIdSet(Account account, ArrayList<Long> queryResultIds) {
        List<Long> filtered = this.filterEntriesByPermission(account, queryResultIds);
        return new ArrayList<>(dao.getEntriesByIdSet(filtered));
    }

    public UserComment addCommentToEntry(Account account, UserComment userComment) throws ControllerException {
        Entry entry = dao.get(userComment.getEntryId());
        Comment comment = new Comment(entry, account, userComment.getMessage());
        comment = commentDAO.create(comment);
        return comment.toDataTransferObject();
    }

    // TODO
//    public PartData retrieveEntryTipDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
//        try {
//            PartData info = api.getPublicPart(entryId);
//            boolean hasSequence = api.hasSequence(info.getRecordId());
//            info.setHasSequence(hasSequence);
//            boolean hasOriginalSequence = api.hasUploadedSequence(info.getRecordId());
//            info.setHasOriginalSequence(hasOriginalSequence);
//            return info;
//        } catch (ServiceException se) {
//            Logger.error(se);
//            throw new ControllerException(se);
//        }
//    }

    public PartData retrieveEntryTipDetails(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        if (!authorization.canRead(userId, entry))
            return null;

        return ModelToInfoFactory.createTipView(entry);
    }

    // tODO
//    public PartData retrieveEntryDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
//        try {
//            PartData info = api.getPublicPart(entryId);
//            boolean hasSequence = api.hasSequence(info.getRecordId());
//            info.setHasSequence(hasSequence);
//            boolean hasOriginalSequence = api.hasUploadedSequence(info.getRecordId());
//            info.setHasOriginalSequence(hasOriginalSequence);
//
//            if (hasSequence && info.getSbolVisualURL() != null) {
//                // retrieve cached pigeon image or generate and cache
//                String tmpDir = new ConfigurationController()
//                        .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
//
//                String hash = Utils.generateUUID();
//                URI uri = PigeonSBOLv.postToPigeon(info.getSbolVisualURL());
//                if (uri != null) {
//                    try {
//                        IOUtils.copy(uri.toURL().openStream(),
//                                     new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
//                        info.setSbolVisualURL(hash + ".png");
//                    } catch (IOException e) {
//                        Logger.error(e);
//                    }
//                }
//            }
//
//            return info;
//        } catch (ServiceException e) {
//            Logger.error(e);
//            throw new ControllerException(e);
//        }
//    }

    public void updatePartStatus(Account account, String recordId, String newStatus) throws ControllerException {
        Entry entry = dao.getByRecordId(recordId);
        entry.setStatus(newStatus);
        dao.update(entry);

        if (entry.getLinkedEntries() != null) {
            for (Entry linkedEntry : entry.getLinkedEntries()) {
                linkedEntry.setStatus(newStatus);
                dao.update(entry);
            }
        }
    }

    public ArrayList<UserComment> retrieveEntryComments(String userId, long partId) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return null;

        // comments
        ArrayList<Comment> comments = commentDAO.retrieveComments(entry);
        ArrayList<UserComment> userComments = new ArrayList<>();

        for (Comment comment : comments) {
            userComments.add(comment.toDataTransferObject());
        }
        return userComments;
    }

    public UserComment createEntryComment(String userId, long partId, UserComment newComment) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return null;

        authorization.canRead(userId, entry);
        Account account = accountController.getByEmail(userId);
        Comment comment = new Comment();
        comment.setAccount(account);
        comment.setEntry(entry);
        comment.setBody(newComment.getMessage());
        comment.setCreationTime(new Date(System.currentTimeMillis()));
        comment = commentDAO.create(comment);
        return comment.toDataTransferObject();
    }

    public boolean deleteTraceSequence(String userId, long entryId, long traceId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return false;

        Account account = accountController.getByEmail(userId);
        TraceSequenceDAO traceSequenceDAO = DAOFactory.getTraceSequenceDAO();
        TraceSequence traceSequence = traceSequenceDAO.get(traceId);
        if (traceSequence == null)
            return false;

        try {
            new SequenceAnalysisController().removeTraceSequence(account, traceSequence);
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
        return true;
    }

    public ArrayList<TraceSequenceAnalysis> getTraceSequences(String userId, long entryId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

        List<TraceSequence> sequences = DAOFactory.getTraceSequenceDAO().getByEntry(entry);
        return ModelToInfoFactory.getSequenceAnalysis(sequences);
    }

    public ArrayList<History> getHistory(String userId, long entryId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        authorization.expectWrite(userId, entry);
        List<Audit> list = auditDAO.getAuditsForEntry(entry);
        ArrayList<History> result = new ArrayList<>();
        for (Audit audit : list) {
            History history = audit.toDataTransferObject();
            if (history.isLocalUser()) {
                history.setAccount(accountController.getByEmail(history.getUserId()).toDataTransferObject());
            }
            result.add(history);
        }
        return result;
    }

    public ArrayList<SampleStorage> retrieveEntrySamples(String userId, long entryId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

        // samples
        ArrayList<Sample> samples = new SampleController().getSamples(entry);
        ArrayList<SampleStorage> sampleStorages = new ArrayList<>();
        if (samples != null && !samples.isEmpty()) {
            for (Sample sample : samples) {
                SampleStorage sampleStorage = new SampleStorage();

                // convert sample to info
                PartSample partSample = new PartSample();
                partSample.setCreationTime(sample.getCreationTime());
                partSample.setLabel(sample.getLabel());
                partSample.setNotes(sample.getNotes());
                partSample.setDepositor(sample.getDepositor());
                sampleStorage.setPartSample(partSample);

                // convert sample to info
                Storage storage = sample.getStorage();

                while (storage != null) {
                    if (storage.getStorageType() == Storage.StorageType.SCHEME) {
                        partSample.setLocationId(storage.getId() + "");
                        partSample.setLocation(storage.getName());
                        break;
                    }

                    sampleStorage.getStorageList().add(ModelToInfoFactory.getStorageInfo(storage));
                    storage = storage.getParent();
                }
                sampleStorages.add(sampleStorage);
            }
        }
        return sampleStorages;
    }

    public PartStatistics retrieveEntryStatistics(String userId, long entryId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

        PartStatistics statistics = new PartStatistics();
        statistics.setEntryId(entryId);
        statistics.setCommentCount(commentDAO.getCommentCount(entry));
        int traceSequenceCount = DAOFactory.getTraceSequenceDAO().getTraceSequenceCount(entry);
        statistics.setTraceSequenceCount(traceSequenceCount);
        int sampleCount = DAOFactory.getSampleDAO().getSampleCount(entry);
        statistics.setSampleCount(sampleCount);
        int historyCount = DAOFactory.getAuditDAO().getHistoryCount(entry);
        statistics.setHistoryCount(historyCount);
        int eddCount = DAOFactory.getExperimentDAO().getExperimentCount(entryId);
        statistics.setExperimentalDataCount(eddCount);
        return statistics;
    }

    public boolean moveEntriesToTrash(String userId, ArrayList<PartData> list) {
        List<Entry> toTrash = new LinkedList<>();
        for (PartData data : list) {
            Entry entry = dao.get(data.getId());
            if (entry == null || !authorization.canWrite(userId, entry))
                return false;

            toTrash.add(entry);
        }

        // add to bin
        try {
            for (Entry entry : toTrash) {
                entry.setVisibility(Visibility.DELETED.getValue());
                dao.update(entry);
            }
        } catch (DAOException de) {
            Logger.error(de);
            return false;
        }

        return true;
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
            dao.getByRecordId(id);

        return entry;
    }

//    public PartData setPermissions(String userId, String id, ArrayList<AccessPermission> permissions) {
//        Entry entry = getEntry(id);
//        if (entry == null)
//            return null;
//
//        EntryType type = EntryType.nameToType(entry.getRecordType());
//        PartData data = new PartData(type);
//
//        // TODO :
//        if (entry == null) {
//            partId = new EntryCreator().createPart(userId, data);
//            entry = DAOFactory.getEntryDAO().get(partId);
//        } else {
//            EntryAuthorization authorization = new EntryAuthorization();
//            authorization.expectWrite(userId, entry);
//            dao.clearPermissions(entry);
//        }
//
//        data.setId(partId);
//
//        if (permissions == null)
//            return data;
//
//        for (AccessPermission access : permissions) {
//            Permission permission = new Permission();
//            permission.setEntry(entry);
//            entry.getPermissions().add(permission);
//            permission.setAccount(account);
//            permission.setCanRead(access.isCanRead());
//            permission.setCanWrite(access.isCanWrite());
//            dao.create(permission);
//        }
//
//        return data;
//    }

    public PartData retrieveEntryDetails(String userId, String id) {
        EntryType type = EntryType.nameToType(id);
        if (type != null)
            return getPartDefaults(userId, type);

        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        // user must be able to read
        authorization.expectRead(userId, entry);
        return retrieveEntryDetails(userId, entry);
    }

    protected PartData getPartDefaults(String userId, EntryType type) {
        PartData partData = new PartData(type);
        PreferencesController preferencesController = new PreferencesController();

        // pi defaults
        String value = preferencesController.getPreferenceValue(userId, PreferenceKey.PRINCIPAL_INVESTIGATOR.name());
        if (value != null) {
            Account piAccount = accountController.getByEmail(value);
            if (piAccount == null) {
                partData.setPrincipalInvestigator(value);
            } else {
                partData.setPrincipalInvestigator(piAccount.getFullName());
                partData.setPrincipalInvestigatorEmail(piAccount.getEmail());
            }
        }

        // funding source defaults
        value = preferencesController.getPreferenceValue(userId, PreferenceKey.FUNDING_SOURCE.name());
        if (value != null) {
            partData.setFundingSource(value);
        }

        Account account = accountController.getByEmail(userId);
        if (account != null) {
            partData.setOwner(account.getFullName());
            partData.setOwnerEmail(account.getEmail());
            partData.setCreator(partData.getOwner());
            partData.setCreatorEmail(partData.getOwnerEmail());
        }

        return partData;
    }

    protected PartData retrieveEntryDetails(String userId, Entry entry) {
        PartData partData = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());

        partData.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        partData.setHasOriginalSequence(hasOriginalSequence);

        // permissions
        partData.setCanEdit(authorization.canWrite(userId, entry));
        partData.setPublicRead(permissionsController.isPubliclyVisible(entry));

        // retrieve cached pigeon image or generate and cache
        String tmpDir = new ConfigurationController()
                .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        if (hasSequence) {
            Sequence sequence = sequenceDAO.getByEntry(entry);
            String hash = sequence.getFwdHash();
            if (Paths.get(tmpDir, hash + ".png").toFile().exists()) {
                partData.setSbolVisualURL(hash + ".png");
            } else {
                URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                if (uri != null) {
                    try {
                        IOUtils.copy(uri.toURL().openStream(),
                                     new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
                        partData.setSbolVisualURL(hash + ".png");
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }
        }

        // create audit event if not owner
        if (!authorization.getOwner(entry).equalsIgnoreCase(userId)) {
            try {
                Audit audit = new Audit();
                audit.setAction(AuditType.READ.getAbbrev());
                audit.setEntry(entry);
                audit.setUserId(userId);
                audit.setLocalUser(true);
                audit.setTime(new Date(System.currentTimeMillis()));
                auditDAO.create(audit);
            } catch (Exception e) {
                Logger.error(e);
            }
        }

        // retrieve more information about linked entries if any (default only contains id)
        if (partData.getLinkedParts() != null) {
            ArrayList<PartData> newLinks = new ArrayList<>();
            for (PartData link : partData.getLinkedParts()) {
                Entry linkedEntry = dao.get(link.getId());
                link = ModelToInfoFactory.createTipView(linkedEntry);
                Sequence sequence = sequenceDAO.getByEntry(linkedEntry);
                if (sequence != null) {
                    link.setBasePairCount(sequence.getSequence().length());
                    link.setFeatureCount(sequence.getSequenceFeatures().size());
                }

                newLinks.add(link);
            }
            partData.getLinkedParts().clear();
            partData.getLinkedParts().addAll(newLinks);
        }

        // check if there is a parent available
        List<Entry> parents = dao.getParents(entry.getId());
        if (parents == null)
            return partData;

        for (Entry parent : parents) {
            EntryType type = EntryType.nameToType(entry.getRecordType());
            PartData parentData = new PartData(type);
            parentData.setId(parent.getId());
            parentData.setName(parent.getName());
            partData.getParents().add(parentData);
        }

        return partData;
    }

    public boolean addTraceSequence(String userId, long partId, File file, String uploadFileName) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return false;

        authorization.expectWrite(userId, entry);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.error(e);
            return false;
        }

        if (uploadFileName.toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zis = new ZipInputStream(inputStream)) {
                ZipEntry zipEntry;
                while (true) {
                    zipEntry = zis.getNextEntry();

                    if (zipEntry != null) {
                        if (!zipEntry.isDirectory() && !zipEntry.getName().startsWith("__MACOSX")) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            int c;
                            while ((c = zis.read()) != -1) {
                                byteArrayOutputStream.write(c);
                            }

                            boolean parsed = parseTraceSequence(userId, entry, zipEntry.getName(),
                                                                byteArrayOutputStream.toByteArray());
                            if (!parsed) {
                                String errMsg = ("Could not parse \"" + zipEntry.getName()
                                        + "\". Only Fasta, GenBank & ABI files are supported.");
                                Logger.error(errMsg);
                                return false;
                            }
                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                String errMsg = ("Could not parse zip file.");
                Logger.error(errMsg);
                return false;
            }
        } else {
            try {
                boolean parsed = parseTraceSequence(userId, entry, uploadFileName, IOUtils.toByteArray(inputStream));
                if (!parsed) {
                    String errMsg = ("Could not parse \"" + uploadFileName
                            + "\". Only Fasta, GenBank & ABI files are supported.");
                    Logger.error(errMsg);
                    return false;
                }
            } catch (IOException e) {
                Logger.error(e);
                return false;
            }
        }

        return true;
    }

    // uploads trace sequence file and builds or rebuilds alignment
    private boolean parseTraceSequence(String userId, Entry entry, String fileName, byte[] bytes) {
        DNASequence dnaSequence = sequenceAnalysisController.parse(bytes);
        if (dnaSequence == null || dnaSequence.getSequence() == null) {
            String errMsg = ("Could not parse \"" + fileName
                    + "\". Only Fasta, GenBank & ABI files are supported.");
            Logger.error(errMsg);
            return false;
        }

        TraceSequence traceSequence = sequenceAnalysisController.uploadTraceSequence(
                entry, fileName, userId, dnaSequence.getSequence().toLowerCase(), new ByteArrayInputStream(bytes));

        if (traceSequence == null)
            return false;

        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
        if (sequence == null)
            return true;
        sequenceAnalysisController.buildOrRebuildAlignment(traceSequence, sequence);
        return true;
    }
}
