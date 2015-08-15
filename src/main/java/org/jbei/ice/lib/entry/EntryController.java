package org.jbei.ice.lib.entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.ApplicationController;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.*;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.*;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.servlet.ModelToInfoFactory;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public FolderDetails retrieveVisibleEntries(String userId, ColumnField field, boolean asc, int start, int limit) {
        Set<Entry> results;
        FolderDetails details = new FolderDetails();
        Account account = accountController.getByEmail(userId);

        if (authorization.isAdmin(userId)) {
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

        if (authorization.isAdmin(userId)) {
            return dao.getAllEntryCount();
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.visibleEntryCount(account, accountGroups);
    }

    public long getNumberOfEntriesSharedWithUser(String userId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        return dao.sharedEntryCount(account, account.getGroups());
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
            info.setViewCount(DAOFactory.getAuditDAO().getHistoryCount(entry));
            data.add(info);
        }
        return data;
    }

    public List<PartData> retrieveOwnerEntries(String userId, String ownerEmail,
                                               ColumnField sort, boolean asc, int start, int limit) {
        List<Entry> entries;
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        if (authorization.isAdmin(userId) || account.getEmail().equals(ownerEmail)) {
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
            info.setViewCount(DAOFactory.getAuditDAO().getHistoryCount(entry));
            data.add(info);
        }
        return data;
    }

    public long getNumberOfOwnerEntries(String requesterUserEmail, String ownerEmail) {
        Account account = DAOFactory.getAccountDAO().getByEmail(requesterUserEmail);
        if (authorization.isAdmin(requesterUserEmail) || account.getEmail().equals(ownerEmail)) {
            return dao.ownerEntryCount(ownerEmail);
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return dao.ownerEntryCount(account, ownerEmail, accountGroups);
    }

    /**
     * Determines if the two entries can be linked
     *
     * @param entry parent in link hierarchy
     * @param link  child in link hierarchy
     * @return true if the two entries can be linked in the hierarchy specified
     */
    private boolean canLink(Entry entry, Entry link) {
        if (entry == null || link == null || entry.getId() == link.getId())
            return false;

        if (link.getLinkedEntries().contains(entry))
            return false;

        EntryType linkedType = EntryType.nameToType(link.getRecordType());
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null || linkedType == null)
            return false;

        switch (type) {
            case PLASMID:
                if (linkedType != type && linkedType != EntryType.PART)
                    return false;
                break;

            case PART:
                if (linkedType != type)
                    return false;
                break;

            case STRAIN:
                if (linkedType != type && linkedType != EntryType.PLASMID && linkedType != EntryType.PART)
                    return false;
                break;

            case ARABIDOPSIS:
                if (linkedType != type && linkedType != EntryType.PART)
                    return false;
                break;
        }

        return true;
    }

    public long updatePart(String userId, long partId, PartData part) {
        Entry existing = dao.get(partId);
        authorization.expectWrite(userId, existing);

        Entry entry = InfoToModelFactory.updateEntryField(part, existing);
        entry.getLinkedEntries().clear();
        if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
            for (PartData data : part.getLinkedParts()) {
                Entry linked = dao.getByPartNumber(data.getPartId());

                // check permissions on link
                if (!authorization.canRead(userId, linked)) {
                    continue;
                }

                if (!canLink(entry, linked))
                    continue;

                entry.getLinkedEntries().add(linked);
            }
        }

        entry.setModificationTime(Calendar.getInstance().getTime());
        if (entry.getVisibility() == Visibility.DRAFT.getValue()) {
            List<EntryField> invalidFields = EntryUtil.validates(part);
            if (invalidFields.isEmpty())
                entry.setVisibility(Visibility.OK.getValue());
        }
        entry = dao.update(entry);

        // check pi email
        String piEmail = entry.getPrincipalInvestigatorEmail();
        if (StringUtils.isNotEmpty(piEmail)) {
            Account pi = DAOFactory.getAccountDAO().getByEmail(piEmail);
            if (pi != null) {
                // add write permission for the PI (method also checks to see if permission already exists)
                AccessPermission accessPermission = new AccessPermission();
                accessPermission.setArticle(AccessPermission.Article.ACCOUNT);
                accessPermission.setArticleId(pi.getId());
                accessPermission.setType(AccessPermission.Type.WRITE_ENTRY);
                accessPermission.setTypeId(entry.getId());
                permissionsController.addPermission(userId, accessPermission);
            }
        }

        return entry.getId();
    }

    public void update(String userId, Entry entry) {
        if (entry == null) {
            return;
        }

        authorization.expectWrite(userId, entry);
        boolean scheduleRebuild = sequenceDAO.hasSequence(entry.getId());

        entry.setModificationTime(Calendar.getInstance().getTime());
        if (entry.getVisibility() == null)
            entry.setVisibility(Visibility.OK.getValue());
        entry = dao.update(entry);

        // check pi email
        String piEmail = entry.getPrincipalInvestigatorEmail();
        if (StringUtils.isNotEmpty(piEmail)) {
            Account pi = DAOFactory.getAccountDAO().getByEmail(piEmail);
            if (pi != null) {
                // add write permission for the PI (method also checks to see if permission already exists)
                AccessPermission accessPermission = new AccessPermission();
                accessPermission.setArticle(AccessPermission.Article.ACCOUNT);
                accessPermission.setArticleId(pi.getId());
                accessPermission.setType(AccessPermission.Type.WRITE_ENTRY);
                accessPermission.setTypeId(entry.getId());
                permissionsController.addPermission(userId, accessPermission);
            }
        }

        if (scheduleRebuild) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }
    }

    public PartData retrieveEntryTipDetails(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry) && !authorization.canRead(userId, entry))
            return null;

        return ModelToInfoFactory.createTipView(entry);
    }

    public ArrayList<UserComment> retrieveEntryComments(String userId, long partId) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

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
        comment.setCreationTime(new Date());
        comment = commentDAO.create(comment);

        if (newComment.getSamples() != null) {
            SampleDAO sampleDAO = DAOFactory.getSampleDAO();
            for (PartSample partSample : newComment.getSamples()) {
                Sample sample = sampleDAO.get(partSample.getId());
                if (sample == null)
                    continue;
                comment.getSamples().add(sample);
                sample.getComments().add(comment);
            }
        }

        comment = commentDAO.update(comment);
        return comment.toDataTransferObject();
    }

    public UserComment updateEntryComment(String userId, long partId, long commentId, UserComment userComment) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return null;

        authorization.canRead(userId, entry);
        Comment comment = commentDAO.get(commentId);
        if (comment == null)
            return createEntryComment(userId, partId, userComment);

        if (comment.getEntry().getId() != partId)
            return null;

        if (userComment.getMessage() == null || userComment.getMessage().isEmpty())
            return null;

        comment.setBody(userComment.getMessage());
        comment.setModificationTime(new Date());
        return commentDAO.update(comment).toDataTransferObject();
    }

    public boolean deleteTraceSequence(String userId, long entryId, long traceId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return false;

        TraceSequenceDAO traceSequenceDAO = DAOFactory.getTraceSequenceDAO();
        TraceSequence traceSequence = traceSequenceDAO.get(traceId);
        if (traceSequence == null || !canEdit(userId, traceSequence.getDepositor(), entry))
            return false;

        try {
            new SequenceAnalysisController().removeTraceSequence(traceSequence);
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

        ArrayList<TraceSequenceAnalysis> analysisArrayList = new ArrayList<>();
        if (sequences == null)
            return analysisArrayList;

        AccountController accountController = new AccountController();

        for (TraceSequence traceSequence : sequences) {
            TraceSequenceAnalysis analysis = traceSequence.toDataTransferObject();
            AccountTransfer accountTransfer = new AccountTransfer();

            String depositor = traceSequence.getDepositor();
            boolean canEdit = canEdit(userId, depositor, entry);
            analysis.setCanEdit(canEdit);

            Account account = accountController.getByEmail(traceSequence.getDepositor());
            if (account != null) {
                accountTransfer.setFirstName(account.getFirstName());
                accountTransfer.setLastName(account.getLastName());
                accountTransfer.setEmail(account.getEmail());
                accountTransfer.setId(account.getId());
            }

            analysis.setDepositor(accountTransfer);
            analysisArrayList.add(analysis);
        }

        return analysisArrayList;
    }

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || authorization.canWrite(userId, entry);
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

    public boolean deleteHistory(String userId, long entryId, long historyId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return false;

        authorization.expectWrite(userId, entry);
        Audit audit = auditDAO.get(historyId);
        if (audit == null)
            return true;

        auditDAO.delete(audit);
        return true;
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

    /**
     * Moves the specified list of entries to the deleted folder
     *
     * @param userId unique identifier for user making the request. Must have write access privileges on the
     *               entries in the list
     * @param list   unique identifiers for entries
     * @return true or false if operation succeeds on all listed entries or not
     */
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

    public boolean removeLink(String userId, long partId, long linkedPart) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return false;

        authorization.expectWrite(userId, entry);
        Entry linkedEntry = dao.get(linkedPart);

        return entry.getLinkedEntries().remove(linkedEntry) && dao.update(entry) != null;
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

        // get by unique name
        if (entry == null)
            return dao.getByUniqueName(id);

        return entry;
    }

    public PartData retrieveEntryDetails(String userId, String id) {
        try {
            Entry entry = getEntry(id);
            if (entry == null)
                return null;

            return retrieveEntryDetails(userId, entry);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Retrieves and sets the default values for the entry. Some of these values (e.g. PI, and Funding Source)
     * are set by individual users as part of their personal preferences
     *
     * @param userId Unique identifier for user requesting the values.
     * @param type   entry type
     * @return PartData object with the retrieve part defaults
     */
    public PartData getPartDefaults(String userId, EntryType type) {
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
                partData.setPrincipalInvestigatorId(piAccount.getId());
            }
        }

        // funding source defaults
        value = preferencesController.getPreferenceValue(userId, PreferenceKey.FUNDING_SOURCE.name());
        if (value != null) {
            partData.setFundingSource(value);
        }

        // owner and creator details
        Account account = accountController.getByEmail(userId);
        if (account != null) {
            partData.setOwner(account.getFullName());
            partData.setOwnerEmail(account.getEmail());
            partData.setCreator(partData.getOwner());
            partData.setCreatorEmail(partData.getOwnerEmail());
        }

        // set the entry type defaults
        return EntryUtil.setPartDefaults(partData);
    }

    protected PartData retrieveEntryDetails(String userId, Entry entry) {
        // user must be able to read if not public entry
        if (!permissionsController.isPubliclyVisible(entry))
            authorization.expectRead(userId, entry);

        PartData partData = ModelToInfoFactory.getInfo(entry);
        if (partData == null)
            return null;
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());

        partData.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        partData.setHasOriginalSequence(hasOriginalSequence);

        // permissions
        partData.setCanEdit(authorization.canWrite(userId, entry));
        partData.setPublicRead(permissionsController.isPubliclyVisible(entry));

        // create audit event if not owner
        // todo : remote access check
        if (userId != null && authorization.getOwner(entry) != null && !authorization.getOwner(entry).equalsIgnoreCase(userId)) {
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
                if (!authorization.canRead(userId, linkedEntry))
                    continue;

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
            if (!authorization.canRead(userId, parent))
                continue;

            EntryType type = EntryType.nameToType(parent.getRecordType());
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

        authorization.expectRead(userId, entry);

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
