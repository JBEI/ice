package org.jbei.ice.folder.collection;

import org.jbei.ice.access.PermissionException;
import org.jbei.ice.bulkupload.BulkUploadAuthorization;
import org.jbei.ice.bulkupload.BulkUploadInfo;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.entry.attachment.Attachments;
import org.jbei.ice.folder.AbstractFolder;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.BulkUpload;
import org.jbei.ice.storage.model.Entry;

import java.util.List;

/**
 * Represents a collection of a specific type
 *
 * @author Hector Plahar
 */
public class Collection {

    private final CollectionType type;
    private final String userId;

    public Collection(String userId, CollectionType type) {
        this.type = type;
        this.userId = userId;
    }

    public AbstractFolder getFolder(long folderId, int offset, int limit) {
        switch (this.type) {
            case DRAFTS:
            default:
                return this.getBulkUploadFolder(folderId, offset, limit);
        }
    }

    /**
     * Retrieves bulk import and entries associated with it that are referenced by the
     * id in the parameter. Only owners or administrators are allowed to retrieve bulk imports
     *
     * @param id     unique identifier for bulk import
     * @param offset offset for upload entries (start)
     * @param limit  maximum number of entries to return with the upload
     * @return data transfer object with the retrieved bulk import data and associated entries
     * @throws PermissionException
     */
    protected AbstractFolder getBulkUploadFolder(long id, int offset, int limit) {
        BulkUploadDAO uploadDAO = DAOFactory.getBulkUploadDAO();
        BulkUploadAuthorization authorization = new BulkUploadAuthorization();

        BulkUpload draft = uploadDAO.get(id);
        if (draft == null)
            return null;

        AccountModel account = DAOFactory.getAccountDAO().getByEmail(userId);
        authorization.expectRead(account.getEmail(), draft);

        // retrieve the entries associated with the bulk import
        BulkUploadInfo info = draft.toDataTransferObject();

        List<Entry> list = uploadDAO.retrieveDraftEntries(id, offset, limit);
        for (Entry entry : list) {
            PartData partData = setFileData(userId, entry, ModelToInfoFactory.getInfo(entry));

            // check if any links and convert
            if (!entry.getLinkedEntries().isEmpty()) {
                Entry linked = (Entry) entry.getLinkedEntries().toArray()[0];
                PartData linkedData = partData.getLinkedParts().remove(0);
                linkedData = setFileData(userId, linked, linkedData);
                partData.getLinkedParts().add(linkedData);
            }

            info.getEntryList().add(partData);
        }

        info.setCount(uploadDAO.retrieveSavedDraftCount(id));
        return info;
    }

    protected PartData setFileData(String userId, Entry entry, PartData partData) {
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();

        if (sequenceDAO.hasSequence(entry.getId())) {
            partData.setHasSequence(true);
            String name = sequenceDAO.getSequenceFilename(entry);
            partData.setSequenceFileName(name);
        }

        Attachments attachments = new Attachments();

        // check attachment
        if (DAOFactory.getAttachmentDAO().hasAttachment(entry)) {
            partData.setHasAttachment(true);
            partData.setAttachments(attachments.getByEntry(userId, entry.getId()));
        }

        // todo: trace sequences

        return partData;
    }
}
