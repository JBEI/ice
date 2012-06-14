package org.jbei.ice.controllers;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.AttachmentPermissionVerifier;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;

/**
 * ABI to manipulate {@link Attachment}s.
 * 
 * @author Timothy Ham, Zinovii Dmytriv.
 * 
 */
public class AttachmentController extends Controller {
    public AttachmentController(Account account) {
        super(account, new AttachmentPermissionVerifier());
    }

    /**
     * Determine if the user has read permission to the attachment.
     * 
     * @param attachment
     * @return True if user has read permission to the attachment.
     * @throws ControllerException
     */
    public boolean hasReadPermission(Attachment attachment) throws ControllerException {
        if (attachment == null) {
            throw new ControllerException("Failed to check read permissions for null attachment!");
        }

        return getAttachmentPermissionVerifier().hasReadPermissions(attachment, getAccount());
    }

    /**
     * Determine if the user has write permission to the attachment.
     * 
     * @param attachment
     * @return True if user has write permission to the attachment.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Attachment attachment) throws ControllerException {
        if (attachment == null) {
            throw new ControllerException("Failed to check write permissions for null attachment!");
        }

        return getAttachmentPermissionVerifier().hasWritePermissions(attachment, getAccount());
    }

    /**
     * Save attachment to the database, and the disk, then rebuild the search index.
     * 
     * @param attachment
     * @param inputStream
     *            The data stream of the file.
     * @return Saved attachment.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Attachment save(Attachment attachment, InputStream inputStream)
            throws ControllerException, PermissionException {
        return save(attachment, inputStream, true);
    }

    /**
     * Save attachment to the database and the disk.
     * 
     * @param attachment
     * @param inputStream
     *            The data stream of the file.
     * @param scheduleIndexRebuild
     *            set true to rebuild the search index.
     * @return Saved attachment.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Attachment save(Attachment attachment, InputStream inputStream,
            boolean scheduleIndexRebuild) throws ControllerException, PermissionException {
        if (!hasWritePermission(attachment)) {
            throw new PermissionException("No permissions to save attachment!");
        }

        Attachment savedAttachment = null;

        try {
            savedAttachment = AttachmentManager.save(attachment, inputStream);

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return savedAttachment;
    }

    /**
     * Delete the attachment from the database and the disk. Rebuild the search index.
     * 
     * @param attachment
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Attachment attachment) throws ControllerException, PermissionException {
        delete(attachment, true);
    }

    /**
     * Delete the attachment from the database and the disk.
     * 
     * @param attachment
     * @param scheduleIndexRebuild
     *            Set true to rebuild search index.
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Attachment attachment, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(attachment)) {
            throw new PermissionException("No permissions to delete attachment!");
        }

        try {
            AttachmentManager.delete(attachment);

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
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
        ArrayList<Attachment> attachments = null;

        try {
            attachments = AttachmentManager.getByEntry(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return attachments;
    }

    /**
     * Retrieve the number of attachments associated with the given {@link Entry entry}.
     * 
     * @param entry
     * @return number of attachments.
     * @throws ControllerException
     */
    public long getNumberOfAttachments(Entry entry) throws ControllerException {
        long result = 0;

        try {
            ArrayList<Attachment> attachments = AttachmentManager.getByEntry(entry);

            result = (attachments == null) ? 0 : attachments.size();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Retrieve the file associated with the {@link Attachment}.
     * 
     * @param attachment
     * @return file associated with the Attachment.
     * @throws ControllerException
     * @throws PermissionException
     */
    public File getFile(Attachment attachment) throws ControllerException, PermissionException {
        if (!hasReadPermission(attachment)) {
            throw new PermissionException("No permissions to read attachment file!");
        }

        File result = null;

        try {
            result = AttachmentManager.getFile(attachment);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Retrieves attachment referenced by unique file id
     * 
     * @param fileId
     * @return retrieved file
     * @throws ControllerException
     */
    public Attachment getAttachmentByFileId(String fileId) throws ControllerException {
        Attachment attachment = null;
        try {
            attachment = AttachmentManager.getByFileId(fileId);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return attachment;
    }

    /**
     * Return the {@link AttachmentPermissionVerifier}.
     * 
     * @return permssionVerifier
     */
    protected AttachmentPermissionVerifier getAttachmentPermissionVerifier() {
        return (AttachmentPermissionVerifier) getPermissionVerifier();
    }
}
