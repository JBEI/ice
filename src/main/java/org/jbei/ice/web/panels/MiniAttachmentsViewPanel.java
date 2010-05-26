package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class MiniAttachmentsViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final String ATTACHMENTS_URL_KEY = "attachments";
    private static final int SHORT_FILENAME_LENGTH = 20;

    Entry entry = null;
    ArrayList<Attachment> attachments = new ArrayList<Attachment>();

    public MiniAttachmentsViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        add(new BookmarkablePageLink<Object>("attachmentsPageLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + ATTACHMENTS_URL_KEY)));
        AttachmentController attachmentController = new AttachmentController(IceSession.get()
                .getAccount());

        int numAttachments = 0;
        try {
            numAttachments = attachmentController.getNumberOfAttachments(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
        add(new Label("attachmentsCount", "(" + numAttachments + ")"));

        try {
            attachments.addAll(attachmentController.getAttachments(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        int showLimit = 4;
        if (attachments.size() > showLimit) {
            Panel moreLinkPanel = new MoreAttachmentsLinkPanel("moreAttachmentsLinkPanel", entry);
            add(moreLinkPanel);
            attachments = new ArrayList<Attachment>(attachments.subList(0, showLimit));
        } else {
            add(new EmptyPanel("moreAttachmentsLinkPanel"));
        }

        ListView<Attachment> attachmentsList = new ListView<Attachment>("attachmentsList",
                attachments) {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void populateItem(ListItem<Attachment> item) {
                AttachmentController attachmentController = new AttachmentController(IceSession
                        .get().getAccount());
                Link downloadLink = null;
                Attachment attachment = item.getModelObject();
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
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to get attachment file!", e);
                }
                if (downloadLink != null) {
                    String shortFileName = null;
                    if (attachment.getFileName().length() > SHORT_FILENAME_LENGTH) {
                        shortFileName = attachment.getFileName()
                                .substring(0, SHORT_FILENAME_LENGTH)
                                + "...";
                    } else {
                        shortFileName = attachment.getFileName();
                    }
                    downloadLink.add(new Label("fileName", shortFileName));
                    item.add(downloadLink);
                }

            }

        };

        add(attachmentsList);
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

}
