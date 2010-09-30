package org.jbei.ice.web.panels;

import java.io.File;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryDownloadAttachmentPage;
import org.jbei.ice.web.pages.EntryViewPage;

public class AttachmentItemViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private Integer index = null;
    private Attachment attachment = null;

    @SuppressWarnings("unchecked")
    public AttachmentItemViewPanel(String id, Integer counter, Attachment attachment) {
        super(id);

        this.setAttachment(attachment);
        this.setIndex(counter);

        add(new Label("counter", counter.toString()));
        String descriptionString = attachment.getDescription();
        if (descriptionString.length() > 70) {
            descriptionString = descriptionString.substring(0, 69) + "...";
        }
        Label description = new Label("description", descriptionString);
        add(description);

        class DeleteAttachmentLink extends AjaxFallbackLink {
            private static final long serialVersionUID = 1L;

            public DeleteAttachmentLink(String id) {
                super(id);
                this.add(new SimpleAttributeModifier("onclick",
                        "return confirm('Delete this attachment?');"));
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                AttachmentItemViewPanel thisPanel = (AttachmentItemViewPanel) getParent();
                Attachment attachment = thisPanel.getAttachment();

                AttachmentController attachmentController = new AttachmentController(IceSession
                        .get().getAccount());

                try {
                    attachmentController.delete(attachment);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to delete attachment!", e);
                }

                setRedirect(true);
                setResponsePage(EntryViewPage.class, new PageParameters("0="
                        + attachment.getEntry().getId() + ",1=attachments"));
            }
        }

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        AjaxFallbackLink deleteAttachmentLink = new DeleteAttachmentLink("deleteAttachmentLink");
        deleteAttachmentLink.setOutputMarkupId(true);
        deleteAttachmentLink.setOutputMarkupPlaceholderTag(true);

        try {
            if (!entryController.hasWritePermission(attachment.getEntry())) {
                deleteAttachmentLink.setVisible(false);
            }
        } catch (ControllerException e1) {
            throw new ViewException(e1);
        }

        add(deleteAttachmentLink);

        AttachmentController attachmentController = new AttachmentController(IceSession.get()
                .getAccount());

        /*Link downloadLink = null;
        try {
            downloadLink = new DownloadLink("downloadAttachmentLink", attachmentController
                    .getFile(attachment), attachment.getFileName());
        } catch (ControllerException e) {
            downloadLink = new Link("downloadAttachmentLink") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                }
            };

            downloadLink.setEnabled(false);
            deleteAttachmentLink.setVisible(false);
            remove(description);

            add(new Label("description", "File not found on server!"));
        } catch (PermissionException e) {
            throw new ViewPermissionException("No permissions to get attachment file!", e);
        }

        if (downloadLink != null) {
            downloadLink.add(new Label("fileName", attachment.getFileName()));

            add(downloadLink);
        }*/

        BookmarkablePageLink<String> downloadLink = new BookmarkablePageLink<String>(
                "downloadAttachmentLink", EntryDownloadAttachmentPage.class, new PageParameters(
                        "0=" + attachment.getEntry().getId() + ",1=" + attachment.getFileName()));

        try {
            File file = attachmentController.getFile(attachment);
        } catch (ControllerException e) {
            downloadLink.setEnabled(false);
            deleteAttachmentLink.setVisible(false);
            remove(description);

            add(new Label("description", "File not found on server!"));
        } catch (PermissionException e) {
            throw new ViewPermissionException("No permissions to get attachment file!", e);
        }

        downloadLink.add(new Label("fileName", attachment.getFileName()));

        add(downloadLink);
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }
}
