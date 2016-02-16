package org.jbei.ice.lib.entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.AuditType;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ABI to manipulate {@link Entry}s.
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
     * Update the information associated with the specified part.<br>
     * <b>Note</b> that any missing information will be deleted from the original entry.
     * In other words, if the part referenced by id <code>partId</code> has an alias value
     * of <code>alias</code> and the part object passed in the parameter does not contain this value,
     * when this method returns, the original entry's alias field will be removed.
     *
     * @param userId unique identifier for user making request
     * @param partId unique identifier for part being updated. This overrides the id in the partData object
     * @param part   information to update part with
     * @return unique identifier for part that was updated
     */
    public long updatePart(String userId, long partId, PartData part) {
        Entry existing = dao.get(partId);
        authorization.expectWrite(userId, existing);

        Entry entry = InfoToModelFactory.updateEntryField(part, existing);
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

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || authorization.canWriteThoroughCheck(userId, entry);
    }

    public PartStatistics retrieveEntryStatistics(String userId, long entryId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

        PartStatistics statistics = new PartStatistics();
        statistics.setEntryId(entryId);
        statistics.setCommentCount(commentDAO.getCommentCount(entry));
        int sequenceCount = DAOFactory.getTraceSequenceDAO().getTraceSequenceCount(entry) +
                DAOFactory.getShotgunSequenceDAO().getShotgunSequenceCount(entry);
        statistics.setSequenceCount(sequenceCount);
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
            if (entry == null || !authorization.canWriteThoroughCheck(userId, entry))
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
            entry = dao.getByRecordId(id);

        // get by unique name
        if (entry == null) {
            try {
                return dao.getByUniqueName(id);
            } catch (DAOException de) {
                // fine to ignore
                return null;
            }
        }

        return entry;
    }

    public PartData retrieveEntryDetails(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        return retrieveEntryDetails(userId, entry);
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

    protected PartData retrieveEntryDetails(String userId, Entry entry) throws PermissionException {
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
        partData.setCanEdit(authorization.canWriteThoroughCheck(userId, entry));
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

            if (parent.getVisibility() != Visibility.OK.getValue() && !authorization.canWriteThoroughCheck(userId, entry))
                continue;

            EntryType type = EntryType.nameToType(parent.getRecordType());
            PartData parentData = new PartData(type);
            parentData.setId(parent.getId());
            parentData.setName(parent.getName());
            parentData.setVisibility(Visibility.valueToEnum(parent.getVisibility()));
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
