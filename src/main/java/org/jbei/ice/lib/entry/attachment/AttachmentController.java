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
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Attachment}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv.
 */
public class AttachmentController {

    private final AttachmentDAO dao;
    private final EntryAuthorization entryAuthorization;

    public static final String attachmentDirName = "attachments";

    public AttachmentController() {
        dao = DAOFactory.getAttachmentDAO();
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

    public ArrayList<Attachment> getByEntry(Account account, Entry entry) throws ControllerException {
        entryAuthorization.expectRead(account.getEmail(), entry);
        return dao.getByEntry(entry);
    }

    public boolean hasAttachment(Entry entry) throws ControllerException {
        return dao.hasAttachment(entry);
    }
}
