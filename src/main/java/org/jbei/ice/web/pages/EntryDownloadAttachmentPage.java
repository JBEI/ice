package org.jbei.ice.web.pages;

import java.io.File;
import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;

public class EntryDownloadAttachmentPage extends ProtectedPage {
    public Entry entry = null;
    public String fileToDownload = null;

    public EntryDownloadAttachmentPage(PageParameters parameters) {
        super(parameters);

        processPageParameters(parameters);

        Account account = IceSession.get().getAccount();

        AttachmentController attachmentsController = new AttachmentController(account);

        Attachment fileAttachment = null;

        File file = null;

        try {
            ArrayList<Attachment> entryAttachments = attachmentsController.getAttachments(entry);

            for (int i = 0; i < entryAttachments.size(); i++) {
                Attachment attachment = entryAttachments.get(i);

                if (attachment.getFileName().toLowerCase().equals(fileToDownload)) {
                    fileAttachment = attachment;

                    break;
                }
            }

            if (fileAttachment != null) {
                file = attachmentsController.getFile(fileAttachment);

                if (file == null) {
                    return;
                }

                IResourceStream resourceStream = new FileResourceStream(
                        new org.apache.wicket.util.file.File(file));

                getRequestCycle().setRequestTarget(
                    new ResourceStreamRequestTarget(resourceStream, fileAttachment.getFileName()) {
                        @Override
                        public void respond(RequestCycle requestCycle) {
                            super.respond(requestCycle);
                        }
                    });
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewException(e);
        }
    }

    private void processPageParameters(PageParameters parameters) {
        if (parameters == null || parameters.size() == 0) {
            throw new ViewException("Parameters are missing!");
        }

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        String identifier = parameters.getString("0");
        String fileToDownload = parameters.getString("1");

        if (fileToDownload == null || fileToDownload.isEmpty()) {
            this.fileToDownload = null;
        } else {
            this.fileToDownload = fileToDownload.toLowerCase();
        }

        try {
            entry = entryController.getByIdentifier(identifier);
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewPermissionException("No permission to view entry!", e);
        }

        if (entry == null) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        }
    }
}