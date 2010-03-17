package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class AttachmentItemViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private Integer index = null;
    private Attachment attachment = null;
    private transient AttachmentController attachmentController;

    @SuppressWarnings("unchecked")
    public AttachmentItemViewPanel(String id, Integer counter, Attachment attachment) {
        super(id);

        attachmentController = new AttachmentController(IceSession.get().getAccount());

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

        AjaxFallbackLink deleteAttachmentLink = new DeleteAttachmentLink("deleteAttachmentLink");
        deleteAttachmentLink.setOutputMarkupId(true);
        deleteAttachmentLink.setOutputMarkupPlaceholderTag(true);
        if (!PermissionManager.hasWritePermission(attachment.getEntry().getId())) {
            deleteAttachmentLink.setVisible(false);
        }
        add(deleteAttachmentLink);

        Link downloadLink = null;
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
        }
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
