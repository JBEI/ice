package org.jbei.ice.lib.entry.attachment;

import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.AttachmentDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.Entry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * ICE Entry Attachments
 *
 * @author Hector Plahar
 */
public class Attachments {

    private final AttachmentDAO dao;
    private final EntryDAO entryDAO;
    private final EntryAuthorization entryAuthorization;

    public static final String attachmentDirName = "attachments";

    public Attachments() {
        dao = DAOFactory.getAttachmentDAO();
        entryDAO = DAOFactory.getEntryDAO();
        entryAuthorization = new EntryAuthorization();
    }

    /**
     * Retrieves a previously uploaded attachment to an entry by it's unique file identifier
     * This file identifier is assigned on upload
     *
     * @return wrapper around file byte array and name
     * @throws PermissionException if the attachment referenced by the fileId exists but
     *                             the user does not have read permissions on the entry associated with the attachment
     * @throws IOException         on exception retrieving file
     */
    public ByteArrayWrapper getAttachmentByFileId(String userId, String fileId) throws IOException {
        Attachment attachment = dao.getByFileId(fileId);
        if (attachment == null)
            return null;

        entryAuthorization.expectRead(userId, attachment.getEntry());
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        byte[] bytes = Files.readAllBytes(Paths.get(dataDir, attachmentDirName, attachment.getFileId()));
        return new ByteArrayWrapper(bytes, attachment.getFileName());
    }

    /**
     * Retrieves the name of the file represented by the file identifer
     *
     * @param userId unique identifier for user making request
     * @param fileId unique file identifier for attachment
     * @return name of file associated with attachment if found, or null is there is no such attachment
     * @throws PermissionException if the user making the request does not have read permissions on the entry
     *                             associated with the attachment
     */
    public String getFileName(String userId, String fileId) {
        Attachment attachment = dao.getByFileId(fileId);
        if (attachment == null)
            return null;

        entryAuthorization.expectRead(userId, attachment.getEntry());
        return dao.getByFileId(fileId).getFileName();
    }

    /**
     * Save attachment to the database and the disk. Entry has to be a transferred entry (Visibility of 2)
     * or the account must have write permissions to it
     *
     * @param userId      account for user making request. Can be null if method is called as a result of a transfer
     * @param attachment  attachment
     * @param inputStream The data stream of the file.
     * @return Saved attachment.
     */
    public Attachment save(String userId, Attachment attachment, InputStream inputStream) {
        if (attachment.getEntry().getVisibility() != Visibility.TRANSFERRED.getValue())
            entryAuthorization.expectWrite(userId, attachment.getEntry());

        if (attachment.getFileId() == null || attachment.getFileId().isEmpty()) {
            String fileId = Utils.generateUUID();
            attachment.setFileId(fileId);
        }

        if (attachment.getDescription() == null)
            attachment.setDescription("");

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);

        try {
            if (inputStream != null) {
                Path path = Paths.get(dataDir, attachmentDirName, attachment.getFileId());
                Files.write(path, ByteStreams.toByteArray(inputStream));
            }
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
        return dao.create(attachment);
    }

    public boolean delete(String userId, long partId, long attachmentId) {
        Attachment attachment = dao.get(attachmentId);
        if (attachment == null)
            return false;

        if (attachment.getEntry().getId() != partId)
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

    public ArrayList<AttachmentInfo> getByEntry(String userId, long entryId) {
        Entry entry = entryDAO.get(entryId);
        entryAuthorization.expectRead(userId, entry);
        return ModelToInfoFactory.getAttachments(dao.getByEntry(entry));
    }

    public AttachmentInfo addAttachmentToEntry(String userId, long partId, AttachmentInfo info) {
        Entry entry = entryDAO.get(partId);
        entryAuthorization.expectRead(userId, entry);
        if (StringUtils.isEmpty(info.getFileId()))
            throw new IllegalArgumentException("Cannot save attachment with invalid file id");

        if (dao.getByFileId(info.getFileId()) != null)
            throw new IllegalArgumentException("Attachment with id " + info.getFileId() + " already exists");

        Attachment attachment = new Attachment();
        attachment.setEntry(entry);
        if (info.getDescription() == null)
            attachment.setDescription("");
        else
            attachment.setDescription(info.getDescription());
        attachment.setFileName(info.getFilename());
        attachment.setFileId(info.getFileId());
        // tODO : information about who added the file
        return dao.create(attachment).toDataTransferObject();
    }
}
