package org.jbei.ice.lib.entry.attachment;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.utils.Utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * ABI to manipulate {@link Attachment}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv.
 */
public class AttachmentController {

    private final AttachmentDAO dao;
    private final PermissionsController permissionsController;


    public AttachmentController() {
        permissionsController = new PermissionsController();
        dao = new AttachmentDAO();
    }

    /**
     * Determine if the user has read permission to the attachment.
     *
     * @param account    account of requesting user
     * @param attachment entry attachment
     * @return True if user has read permission to the attachment.
     * @throws ControllerException
     */
    public boolean hasReadPermission(Account account, Attachment attachment) throws ControllerException {
        if (attachment == null) {
            throw new ControllerException("Failed to check read permissions for null attachment!");
        }

        return permissionsController.hasReadPermission(account, attachment.getEntry());
    }

    /**
     * Determine if the user has write permission to the attachment.
     *
     * @param account    account of requesting user
     * @param attachment entry attachment
     * @return True if user has write permission to the attachment.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Account account, Attachment attachment) throws ControllerException {
        if (attachment == null) {
            throw new ControllerException("Failed to check write permissions for null attachment!");
        }

        return permissionsController.hasWritePermission(account, attachment.getEntry());
    }

    /**
     * Save attachment to the database, and the disk, then rebuild the search index.
     *
     * @param attachment
     * @param inputStream The data stream of the file.
     * @return Saved attachment.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Attachment save(Account account, Attachment attachment, InputStream inputStream)
            throws ControllerException, PermissionException {
        return save(account, attachment, inputStream, true);
    }

    /**
     * Save attachment to the database and the disk.
     *
     * @param attachment
     * @param inputStream          The data stream of the file.
     * @param scheduleIndexRebuild set true to rebuild the search index.
     * @return Saved attachment.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Attachment save(Account account, Attachment attachment, InputStream inputStream,
            boolean scheduleIndexRebuild) throws ControllerException, PermissionException {

        if (!hasWritePermission(account, attachment)) {
            throw new PermissionException("No permissions to save attachment!");
        }

        if (attachment.getFileId() == null || attachment.getFileId() == "") {
            String fileId = Utils.generateUUID();
            attachment.setFileId(fileId);
        }

        Attachment result;

        try {
            result = dao.save(attachment, inputStream);
        } catch (DAOException e) {
            throw new ControllerException("Failed to save attachment!", e);
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleSearchIndexRebuildJob();
        }

        return result;
    }


    /**
     * Delete the attachment from the database and the disk. Rebuild the search index.
     *
     * @param attachment
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Attachment attachment) throws ControllerException, PermissionException {
        delete(account, attachment, true);
    }

    /**
     * Delete the attachment from the database and the disk.
     *
     * @param attachment
     * @param scheduleIndexRebuild Set true to rebuild search index.
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Attachment attachment, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(account, attachment)) {
            throw new PermissionException("No permissions to delete attachment!");
        }

        try {
            dao.delete(attachment);
            if (scheduleIndexRebuild) {
                ApplicationController.scheduleSearchIndexRebuildJob();
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve all the attachments associated with the given {@link Entry entry}.
     *
     * @param entry
     * @return List of Attachments or null.
     * @throws ControllerException
     */
    public ArrayList<Attachment> getAttachments(Entry entry) throws ControllerException {

        try {
            return dao.getByEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
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
        if (!hasReadPermission(account, attachment)) {
            throw new PermissionException("No permissions to read attachment file!");
        }

        try {
            return dao.getFile(attachment);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
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
        if (!permissionsController.hasReadPermission(account, entry))
            throw new ControllerException(account.getEmail() + " does not have read permission for entry "
                                                  + entry.getRecordId());

        try {
            return dao.getByEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean hasAttachment(Account account, Entry entry) throws ControllerException {
        if (!permissionsController.hasReadPermission(account, entry))
            throw new ControllerException(account.getEmail() + " does not have read permission for entry "
                                                  + entry.getRecordId());
        try {
            return dao.hasAttachment(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}