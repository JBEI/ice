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

public class AttachmentController extends Controller {
    public AttachmentController(Account account) {
        super(account, new AttachmentPermissionVerifier());
    }

    public boolean hasReadPermission(Attachment attachment) throws ControllerException {
        if (attachment == null) {
            throw new ControllerException("Failed to check read permissions for null attachment!");
        }

        return getAttachmentPermissionVerifier().hasReadPermissions(attachment, getAccount());
    }

    public boolean hasWritePermission(Attachment attachment) throws ControllerException {
        if (attachment == null) {
            throw new ControllerException("Failed to check write permissions for null attachment!");
        }

        return getAttachmentPermissionVerifier().hasWritePermissions(attachment, getAccount());
    }

    public Attachment save(Attachment attachment, InputStream inputStream)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(attachment)) {
            throw new PermissionException("No permissions to save attachment!");
        }

        Attachment savedAttachment = null;

        try {
            savedAttachment = AttachmentManager.save(attachment, inputStream);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return savedAttachment;
    }

    public void delete(Attachment attachment) throws ControllerException, PermissionException {
        if (!hasWritePermission(attachment)) {
            throw new PermissionException("No permissions to delete attachment!");
        }

        try {
            AttachmentManager.delete(attachment);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<Attachment> getAttachments(Entry entry) throws ControllerException {
        ArrayList<Attachment> attachments = null;

        try {
            attachments = AttachmentManager.getByEntry(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return attachments;
    }

    public int getNumberOfAttachments(Entry entry) throws ControllerException {
        int result = 0;

        try {
            ArrayList<Attachment> attachments = AttachmentManager.getByEntry(entry);

            result = (attachments == null) ? 0 : attachments.size();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

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

    protected AttachmentPermissionVerifier getAttachmentPermissionVerifier() {
        return (AttachmentPermissionVerifier) getPermissionVerifier();
    }
}
