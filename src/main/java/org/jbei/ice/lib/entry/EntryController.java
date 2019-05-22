package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.*;

/**
 * ABI to manipulate {@link Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar, Elena Aravina
 */
public class EntryController extends HasEntry {

    private EntryDAO dao;
    private CommentDAO commentDAO;
    private SequenceDAO sequenceDAO;
    private PermissionsController permissionsController;
    private AccountController accountController;
    private final EntryAuthorization authorization;

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

    public boolean deleteShotgunSequence(String userId, long entryId, long shotgunId) {
        Entry entry = dao.get(entryId);
        if (entry == null)
            return false;

        ShotgunSequenceDAO shotgunSequenceDAO = DAOFactory.getShotgunSequenceDAO();
        ShotgunSequence shotgunSequence = shotgunSequenceDAO.get(shotgunId);
        if (shotgunSequence == null || !canEdit(userId, shotgunSequence.getDepositor(), entry))
            return false;

        try {
            new SequenceAnalysisController().removeShotgunSequence(shotgunSequence);
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }

        return true;
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
            for (Entry entry : toTrash) {
                if (entry.getVisibility() == Visibility.DELETED.getValue()) {
                    entry.setVisibility(Visibility.PERMANENTLY_DELETED.getValue());
                } else {
                    entry.setVisibility(Visibility.DELETED.getValue());
                }

                dao.update(entry);
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
        if (!permissionsController.isPubliclyVisible(entry))
            authorization.expectRead(userId, entry);

        PartData partData = retrieveEntryDetails(userId, entry);
        if (partData.getVisibility() == Visibility.REMOTE)
            partData.setCanEdit(false);
        else {
            partData.setCanEdit(authorization.canWrite(userId, entry));
            partData.setPublicRead(permissionsController.isPubliclyVisible(entry));
        }
        return partData;
    }

    protected PartData retrieveEntryDetails(String userId, Entry entry) throws PermissionException {
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
        if (partData == null)
            return null;

        // get custom data
        CustomFields fields = new CustomFields();
        partData.getCustomEntryFields().addAll(fields.getCustomFieldValuesForPart(entry.getId()));

        // retrieve sequence information
        boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
        partData.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
        partData.setHasOriginalSequence(hasOriginalSequence);
        Optional<String> sequenceString = sequenceDAO.getSequenceString(entry);
        if (sequenceString.isPresent())
            partData.setBasePairCount(sequenceString.get().trim().length());
        else
            partData.setBasePairCount(0);

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
                    link.setBasePairCount(linkedSequenceString.get().trim().length());
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
