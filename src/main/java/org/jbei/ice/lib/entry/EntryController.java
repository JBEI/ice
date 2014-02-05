package org.jbei.ice.lib.entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.CommentDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.PartSample;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.dto.sample.SampleStorage;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.ServiceException;
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
    private PermissionsController permissionsController;
    private AccountController accountController;
    private SequenceController sequenceController;
    private final EntryAuthorization authorization;

    public EntryController() {
        dao = DAOFactory.getEntryDAO();
        commentDAO = DAOFactory.getCommentDAO();
        permissionsController = new PermissionsController();
        accountController = new AccountController();
        sequenceController = new SequenceController();
        authorization = new EntryAuthorization();
        sequenceDAO = DAOFactory.getSequenceDAO();
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
        return sequenceController.sequenceToDNASequence(sequenceDAO.getByEntry(entry));
    }

    public PartData getPublicEntryByRecordId(String recordId) throws ControllerException {
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

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public PartData getPublicEntryById(long id) throws ControllerException {
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

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        info.setOwnerId(0);
        info.setCreatorId(0);

        if (hasSequence) {
            String script = PigeonSBOLv.generatePigeonScript(sequenceDAO.getByEntry(entry));
            info.setSbolVisualURL(script);
        }

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
    public PartData getByPartNumber(Account account, String partNumber) throws ControllerException,
            PermissionException {
        Entry entry;
        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

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
                GroupController controller = new GroupController();
                Group everybodyGroup = controller.createOrRetrievePublicGroup();
                accountGroups.add(everybodyGroup);
                results = dao.retrieveVisibleEntries(account, accountGroups, field, asc, start, limit);
            }

            for (Entry entry : results) {
                PartData info = ModelToInfoFactory.createTableViewData(entry, false);
                info.setCanEdit(authorization.canWrite(account.getEmail(), entry));
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
        GroupController controller = new GroupController();
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
        if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
            return dao.retrieveOwnerEntries(ownerEmail, sort, asc, start, limit);
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.retrieveUserEntries(account, ownerEmail, accountGroups, sort, asc, start, limit);
    }

    public long getNumberOfOwnerEntries(Account account, String ownerEmail) throws ControllerException {
        if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
            return dao.ownerEntryCount(ownerEmail);
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.ownerEntryCount(account, ownerEmail, accountGroups);
    }

    public long updatePart(Account account, PartData part) throws ControllerException {
        Entry existing = dao.get(part.getId());
        authorization.expectWrite(account.getEmail(), existing);

        Entry entry = InfoToModelFactory.infoToEntry(part, existing);
        entry.getLinkedEntries().clear();
        if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
            for (PartData data : part.getLinkedParts()) {
                Entry linked = dao.getByPartNumber(data.getPartId());
                if (linked == null)
                    continue;

                if (!authorization.canRead(account.getEmail(), linked)) {
                    continue;
                }

                entry.getLinkedEntries().add(linked);
            }
        }

        entry.setModificationTime(Calendar.getInstance().getTime());
        entry.setVisibility(Visibility.OK.getValue());
        dao.update(entry);

        return entry.getId();
    }

    public void update(Account account, Entry entry) throws ControllerException, PermissionException {
        if (entry == null) {
            return;
        }

        authorization.expectWrite(account.getEmail(), entry);
        boolean scheduleRebuild = sequenceDAO.hasSequence(entry.getId());

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            if (entry.getVisibility() == null)
                entry.setVisibility(Visibility.OK.getValue());
            dao.update(entry);

            if (scheduleRebuild) {
                ApplicationController.scheduleBlastIndexRebuildTask(true);
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
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
        List<Folder> folders = folderController.getFoldersByEntry(entry);
        ArrayList<Long> entryIds = new ArrayList<>();
        entryIds.add(entry.getId());
        if (folders != null) {
            for (Folder folder : folders) {
                try {
                    Folder returned = folderController.removeFolderContents(account, folder.getId(), entryIds);
                    FolderDetails details = new FolderDetails(returned.getId(), returned.getName());
                    long size = folderController.getFolderSize(folder.getId());
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
     * @throws ControllerException
     */
    List<Long> filterEntriesByPermission(Account account, List<Long> ids) throws ControllerException {
        ArrayList<Long> result = new ArrayList<>();
        for (Long id : ids) {
            Entry entry = dao.get(id);
            if (authorization.canRead(account.getEmail(), entry)) {
                result.add(id);
            }
        }
        return result;
    }

    public ArrayList<Entry> getEntriesByIdSet(Account account, ArrayList<Long> queryResultIds)
            throws ControllerException {
        List<Long> filtered = this.filterEntriesByPermission(account, queryResultIds);
        return new ArrayList<>(dao.getEntriesByIdSet(filtered));
    }

    public UserComment addCommentToEntry(Account account, UserComment userComment) throws ControllerException {
        Entry entry = dao.get(userComment.getEntryId());
        Comment comment = new Comment(entry, account, userComment.getMessage());
        comment = commentDAO.create(comment);
        return comment.toDataTransferObject();
    }

    public PartData retrieveEntryTipDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
        try {
            PartData info = api.getPublicPart(entryId);
            boolean hasSequence = api.hasSequence(info.getRecordId());
            info.setHasSequence(hasSequence);
            boolean hasOriginalSequence = api.hasUploadedSequence(info.getRecordId());
            info.setHasOriginalSequence(hasOriginalSequence);
            return info;
        } catch (ServiceException se) {
            Logger.error(se);
            throw new ControllerException(se);
        }
    }

    public PartData retrieveEntryTipDetails(Account account, long entryId) throws ControllerException {
        Entry entry = dao.get(entryId);
        if (!authorization.canRead(account.getEmail(), entry))
            return null;

        return ModelToInfoFactory.createTipView(entry);
    }

    public PartData retrieveEntryDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
        try {
            PartData info = api.getPublicPart(entryId);
            boolean hasSequence = api.hasSequence(info.getRecordId());
            info.setHasSequence(hasSequence);
            boolean hasOriginalSequence = api.hasUploadedSequence(info.getRecordId());
            info.setHasOriginalSequence(hasOriginalSequence);

            if (hasSequence && info.getSbolVisualURL() != null) {
                // retrieve cached pigeon image or generate and cache
                String tmpDir = new ConfigurationController()
                        .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);

                String hash = Utils.generateUUID();
                URI uri = PigeonSBOLv.postToPigeon(info.getSbolVisualURL());
                if (uri != null) {
                    try {
                        IOUtils.copy(uri.toURL().openStream(),
                                     new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
                        info.setSbolVisualURL(hash + ".png");
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }

            return info;
        } catch (ServiceException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

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

    public PartData retrieveEntryDetails(Account account, long entryId) throws ControllerException {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        PartData partData = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
        partData.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        partData.setHasOriginalSequence(hasOriginalSequence);

        // attachments
        try {
            ArrayList<Attachment> attachments = new AttachmentController().getByEntry(account, entry);
            ArrayList<AttachmentInfo> attachmentInfos = ModelToInfoFactory.getAttachments(attachments);
            partData.setAttachments(attachmentInfos);
            partData.setHasAttachment(!attachmentInfos.isEmpty());

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
            partData.setSampleMap(sampleStorages);
        } catch (ControllerException ce) {
            Logger.error(ce);
        }

        // sequence analysis
        List<TraceSequence> sequences = DAOFactory.getTraceSequenceDAO().getByEntry(entry);
        partData.setSequenceAnalysis(ModelToInfoFactory.getSequenceAnalysis(sequences));

        // comments
        ArrayList<Comment> comments = commentDAO.retrieveComments(entry);
        for (Comment comment : comments) {
            partData.getComments().add(comment.toDataTransferObject());
        }

        // permissions
        partData.setCanEdit(authorization.canWrite(account.getEmail(), entry));

        // viewing permissions is restricted to users who have write access
        if (partData.isCanEdit()) {
            ArrayList<AccessPermission> accessPermissions =
                    permissionsController.retrieveSetEntryPermissions(entry);
            partData.setAccessPermissions(accessPermissions);
            partData.setPublicRead(permissionsController.isPubliclyVisible(entry));
        }

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

        return partData;
    }

    public UserComment sendProblemNotification(Account account, long entryId, String msg) {
        try {
            Entry entry = dao.get(entryId);
            if (entry == null) {
                Logger.error("Entry could not be retrieve for id " + entryId);
                return null;
            }

            Comment comment = new Comment(entry, account, msg);
            comment = commentDAO.create(comment);

            String email = new ConfigurationController().getPropertyValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (email != null && !email.isEmpty()) {
                String site = new ConfigurationController().getPropertyValue(
                        ConfigurationKey.URI_PREFIX);
                StringBuilder body = new StringBuilder();
                body.append("A problem notification was sent by ")
                    .append(account.getFullName())
                    .append(" for entry ")
                    .append(entry.getPartNumber())
                    .append(" (https://").append(site).append("/#page=entry;id=").append(entry.getId()).append(")")
                    .append("\n\nMessage:\n\n")
                    .append(msg)
                    .append("\n\n");
                boolean success = Emailer.send(email, ("Problem alert for " + entry.getPartNumber()), body.toString());
                if (!success)
                    Logger.warn("Could not send email for problem notification");
            }

            return comment.toDataTransferObject();
        } catch (ControllerException e) {
            Logger.error(e);
        }
        return null;
    }
}
