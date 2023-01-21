package org.jbei.ice.entry;

import org.jbei.ice.access.PermissionException;
import org.jbei.ice.access.PermissionsController;
import org.jbei.ice.account.AccountController;
import org.jbei.ice.account.TokenHash;
import org.jbei.ice.dto.AuditType;
import org.jbei.ice.dto.comment.UserComment;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.dto.sample.PartSample;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.dto.web.WebEntries;
import org.jbei.ice.entry.sequence.SequenceFormat;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.CommentDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SampleDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.util.*;

/**
 * ABI to manipulate {@link Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar, Elena Aravina
 */
public class EntryController extends HasEntry {

    private final EntryAuthorization authorization;
    private final EntryDAO dao;
    private final CommentDAO commentDAO;
    private final SequenceDAO sequenceDAO;
    private final PermissionsController permissionsController;
    private final AccountController accountController;

    public EntryController() {
        dao = DAOFactory.getEntryDAO();
        commentDAO = DAOFactory.getCommentDAO();
        permissionsController = new PermissionsController();
        accountController = new AccountController();
        authorization = new EntryAuthorization();
        sequenceDAO = DAOFactory.getSequenceDAO();
    }

    public PartData retrieveEntryTipDetails(String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        return ModelToInfoFactory.createTipView(entry);
    }

    public ArrayList<UserComment> retrieveEntryComments(String userId, long partId) {
        Entry entry = dao.get(partId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

        // comments
        List<Comment> comments = commentDAO.retrieveComments(entry);
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
        AccountModel account = accountController.getByEmail(userId);
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

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || authorization.canWrite(userId, entry);
    }

    public PartStatistics retrieveEntryStatistics(String userId, long entryId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);

        PartStatistics statistics = new PartStatistics();
        statistics.setEntryId(entryId);
        statistics.setCommentCount(commentDAO.getCommentCount(entry));
        int sequenceCount = DAOFactory.getTraceSequenceDAO().getCountByEntry(entry) +
            DAOFactory.getShotgunSequenceDAO().getShotgunSequenceCount(entry);
        statistics.setSequenceCount(sequenceCount);
        int sampleCount = DAOFactory.getSampleDAO().getSampleCount(entry);
        statistics.setSampleCount(sampleCount);
        int historyCount = DAOFactory.getAuditDAO().getAuditsForEntryCount(entry);
        statistics.setHistoryCount(historyCount);
        int eddCount = DAOFactory.getExperimentDAO().getExperimentCount(entryId);
        statistics.setExperimentalDataCount(eddCount);
        return statistics;
    }

    /**
     * Moves the specified list of entries to the deleted folder.
     * If an already deleted entry (list of entries) is being deleted then
     * the status "permanently deleted" is assigned, and these entries
     * become invisible to everyone.
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
            EntryAudit audit = new EntryAudit(userId);
            for (Entry entry : toTrash) {
                AuditType auditType;
                if (entry.getVisibility() == Visibility.DELETED.getValue()) {
                    entry.setVisibility(Visibility.PERMANENTLY_DELETED.getValue());
                    auditType = AuditType.PERMANENTLY_DELETE;
                } else {
                    entry.setVisibility(Visibility.DELETED.getValue());
                    auditType = AuditType.DELETE;
                }

                Date modificationDate = new Date();
                entry.setModificationTime(modificationDate);
                dao.update(entry);

                audit.action(entry.getId(), auditType, modificationDate);
            }
        } catch (DAOException de) {
            Logger.error(de);
            return false;
        }

        return true;
    }

    public PartData getRequestedEntry(String remoteUserId, String token, String entryId,
                                      long folderId, RegistryPartner requestingPartner) {
        Entry entry = getEntry(entryId);
        if (entry == null)
            return null;

        // see folderContents.getRemoteSharedContents
        Folder folder = DAOFactory.getFolderDAO().get(folderId);      // folder that the entry is contained in
        if (folder == null) {
            // must be a public entry (todo : move to separate method
            if (!permissionsController.isPubliclyVisible(entry))
                throw new PermissionException("Not a public entry");
            return retrieveEntryDetails(null, entry);
        }

        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().getByUrl(requestingPartner.getUrl());

        // check that the remote user has the right token
        Permission shareModel = DAOFactory.getPermissionDAO().get(remoteUserId, remotePartner, folder);
        if (shareModel == null) {
            Logger.error("Could not retrieve share model");
            return null;
        }

        // validate access token
        TokenHash tokenHash = new TokenHash();
        String secret = tokenHash.encrypt(folderId + remotePartner.getUrl() + remoteUserId, token);
        if (!secret.equals(shareModel.getSecret())) {
            throw new PermissionException("Secret does not match");
        }

        // check that entry id is contained in folder
        return retrieveEntryDetails(null, entry);
    }

    public PartData retrieveEntryDetails(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        // user must be able to read if not public entry
        authorization.expectRead(userId, entry);

        PartData partData = retrieveEntryDetails(userId, entry);
        if (partData.getVisibility() == Visibility.REMOTE)
            partData.setCanEdit(false);
        else {
            partData.setCanEdit(authorization.canWrite(userId, entry));
            partData.setPublicRead(permissionsController.isPubliclyVisible(entry));
        }
        Optional<SequenceFormat> format = sequenceDAO.getSequenceFormat(entry.getId());
        format.ifPresent(sequenceFormat -> partData.setBasePairCount(sequenceFormat.toString()));
        return partData;
    }

    private PartData retrieveEntryDetails(String userId, Entry entry) throws PermissionException {
        if (entry.getVisibility() == Visibility.REMOTE.getValue()) {
            WebEntries webEntries = new WebEntries();
            PartData partData = webEntries.getPart(entry.getRecordId());
            partData.setVisibility(Visibility.REMOTE);
            partData.setId(entry.getId()); // id returned from remote is different from the local id
            partData.getParents().clear(); // need to map parents to local
            // todo : sequence data
            return partData;
        }

        PartData partData = ModelToInfoFactory.getInfo(entry);
        EntryType entryType = EntryType.nameToType(entry.getRecordType());

        // get custom data
        CustomFields fields = new CustomFields();
        List<CustomEntryField> customValues = fields.getCustomFieldValuesForPart(entry.getId());
        Set<EntryFieldLabel> existingCustomFields = new HashSet<>(); // to keep track of existing
        for (CustomEntryField customEntryField : customValues) {
            // convert to entry field
            EntryField field = new EntryField();

            switch (customEntryField.getFieldType()) {
                // keep track of existing field customizations to avoid duplicating it when retrieving
                // regular fields
                case EXISTING -> {
                    existingCustomFields.add(customEntryField.getExistingField());
                    field.setFieldInputType(customEntryField.getExistingField().getFieldType());
                }

                case MULTI_CHOICE, MULTI_CHOICE_PLUS -> field.setFieldInputType(FieldInputType.SELECT);
            }

            // create and add entry field
            field.setRequired(customEntryField.isRequired());
            field.setEntryType(entryType);
            field.setCustom(true);
            field.setId(customEntryField.getId());
            field.setValue(customEntryField.getValue());
            field.setLabel(customEntryField.getLabel());
            field.getOptions().addAll(customEntryField.getOptions());
            partData.getFields().add(field);
        }

        // get fields data
        PartFields partFields = new PartFields(userId, entryType);
        for (EntryField entryField : partFields.get()) {        // note: this also includes custom fields
            if (entryField.isCustom() || existingCustomFields.contains(EntryFieldLabel.fromString(entryField.getLabel())))
                continue;

            ModelToInfoFactory.entryToInfo(entry, entryField);
            partData.getFields().add(entryField);
        }

        // retrieve sequence information
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
        partData.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        partData.setHasOriginalSequence(hasOriginalSequence);
        Optional<String> sequenceString = sequenceDAO.getSequenceString(entry);
        sequenceString.ifPresent(s -> partData.setBasePairCount(Integer.toString(s.trim().length())));

        // create audit event if not owner
        // todo : remote access check
        if (userId != null && authorization.getOwner(entry) != null && !authorization.getOwner(entry).equalsIgnoreCase(userId)) {
            EntryHistory entryHistory = new EntryHistory(userId, entry.getId());
            entryHistory.add();
        }

        // retrieve more information about linked entries if any (default only contains id)
        if (partData.getLinkedParts() != null) {
            ArrayList<PartData> newLinks = new ArrayList<>();
            for (PartData link : partData.getLinkedParts()) {
                Entry linkedEntry = dao.get(link.getId());
                if (!authorization.canRead(userId, linkedEntry))
                    continue;

                link = ModelToInfoFactory.createTipView(linkedEntry);
                link.setSelectionMarkers(EntryUtil.getSelectionMarkersAsList(linkedEntry.getSelectionMarkers()));

                Optional<String> linkedSequenceString = sequenceDAO.getSequenceString(linkedEntry);

                if (linkedSequenceString.isPresent()) {
                    link.setBasePairCount(Integer.toString(linkedSequenceString.get().trim().length()));
                    link.setFeatureCount(DAOFactory.getSequenceFeatureDAO().getFeatureCount(linkedEntry));
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

            if (parent.getVisibility() != Visibility.OK.getValue() && !authorization.canWrite(userId, entry))
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
}