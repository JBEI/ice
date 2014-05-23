package org.jbei.ice.lib.entry.attachment;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.AttachmentDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.servlet.ModelToInfoFactory;

import org.apache.commons.lang.StringUtils;

/**
 * ABI to manipulate {@link Attachment}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv.
 */
public class AttachmentController {

    private final AttachmentDAO dao;
    private final EntryDAO entryDAO;
    private final EntryAuthorization entryAuthorization;

    public static final String attachmentDirName = "attachments";

    public AttachmentController() {
        dao = DAOFactory.getAttachmentDAO();
        entryDAO = DAOFactory.getEntryDAO();
        entryAuthorization = new EntryAuthorization();
    }

    /**
     * Save attachment to the database and the disk. Entry has to be a transferred entry (Visibility of 2)
     * or the account must have write permissions to it
     *
     * @param account     account for user making request. Can be null if method is called as a result of a transfer
     * @param attachment  attachment
     * @param inputStream The data stream of the file.
     * @return Saved attachment.
     * @throws ControllerException
     */
    public Attachment save(Account account, Attachment attachment, InputStream inputStream) throws ControllerException {
        if (attachment.getEntry().getVisibility() != Visibility.TRANSFERRED.getValue() &&
                !entryAuthorization.canWrite(account.getEmail(), attachment.getEntry())) {
            throw new ControllerException("No permissions to save attachment!");
        }

        if (attachment.getFileId() == null || attachment.getFileId().isEmpty()) {
            String fileId = Utils.generateUUID();
            attachment.setFileId(fileId);
        }

        if (attachment.getDescription() == null)
            attachment.setDescription("");

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        File attachmentDir = Paths.get(dataDir, attachmentDirName).toFile();

        try {
            return dao.save(attachmentDir, attachment, inputStream);
        } catch (DAOException e) {
            throw new ControllerException("Failed to save attachment!", e);
        }
    }

    /**
     * Delete the attachment from the database and the disk.
     *
     * @param attachment
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Attachment attachment) throws ControllerException, PermissionException {
        entryAuthorization.expectWrite(account.getEmail(), attachment.getEntry());

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        File attachmentDir = Paths.get(dataDir, attachmentDirName).toFile();
        try {
            dao.delete(attachmentDir, attachment);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean delete(String userId, long partId, long attachmentId) {
        Attachment attachment = dao.get(attachmentId);
        if (attachment == null)
            return false;

        if(attachment.getEntry().getId() != partId)
            return false;

        entryAuthorization.expectWrite(userId, attachment.getEntry());
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        File attachmentDir = Paths.get(dataDir, attachmentDirName).toFile();
        try {
            dao.delete(attachmentDir, attachment);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void delete(Account account, String fileId) throws ControllerException, PermissionException {
        Attachment attachment = getAttachmentByFileId(fileId);
        if (attachment != null)
            delete(account, attachment);
    }

    /**
     * Retrieve the file associated with the {@link Attachment}.
     *
     * @param attachment
     * @return file associated with the Attachment.
     * @throws ControllerException
     * @throws PermissionException
     */
    public File getFile(Account account, Attachment attachment) throws ControllerException, PermissionException {
        entryAuthorization.expectRead(account.getEmail(), attachment.getEntry());

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        File attachmentDir = Paths.get(dataDir, attachmentDirName).toFile();
        return dao.getFile(attachmentDir, attachment);
    }

    /**
     * Retrieves attachment referenced by unique file id
     *
     * @param fileId
     * @return retrieved file
     * @throws ControllerException
     */
    public Attachment getAttachmentByFileId(String fileId) throws ControllerException {
        Attachment attachment;
        try {
            attachment = dao.getByFileId(fileId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return attachment;
    }

    public ArrayList<Attachment> getByEntry(String userId, Entry entry) throws ControllerException {
        entryAuthorization.expectRead(userId, entry);
        return dao.getByEntry(entry);
    }

    public ArrayList<AttachmentInfo> getByEntry(String userId, long entryId) {
        Entry entry = entryDAO.get(entryId);
        entryAuthorization.expectRead(userId, entry);
        return ModelToInfoFactory.getAttachments(dao.getByEntry(entry));
    }

    public boolean hasAttachment(Entry entry) throws ControllerException {
        return dao.hasAttachment(entry);
    }

    public AttachmentInfo addAttachmentToEntry(String userId, long partId, AttachmentInfo info) {
        Entry entry = entryDAO.get(partId);
        entryAuthorization.expectRead(userId, entry);
        // todo : make sure file at /fileId exists
        Attachment attachment = new Attachment();
        attachment.setEntry(entry);
        if (StringUtils.isEmpty(info.getDescription()))
            attachment.setDescription("");
        else
            attachment.setDescription(info.getDescription());
        attachment.setFileName(info.getFilename());
        attachment.setFileId(info.getFileId());
        // tODO : information about who added the file
        return dao.create(attachment).toDataTransferObject();
    }
}
