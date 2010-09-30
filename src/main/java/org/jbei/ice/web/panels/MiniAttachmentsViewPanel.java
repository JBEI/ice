package org.jbei.ice.web.panels;

import java.io.File;
import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryDownloadAttachmentPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.WelcomePage;

public class MiniAttachmentsViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final String ATTACHMENTS_URL_KEY = "attachments";
    private static final int SHORT_FILENAME_LENGTH = 20;

    Entry entry = null;
    ArrayList<Attachment> attachments = new ArrayList<Attachment>();

    public MiniAttachmentsViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;
        EntryController entryController = new EntryController(IceSession.get().getAccount());

        WebMarkupContainer editLink = new WebMarkupContainer("editLink");
        editLink.add(new BookmarkablePageLink<Object>("attachmentsPageLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + ATTACHMENTS_URL_KEY)));
        try {
            editLink.setVisible(entryController.hasWritePermission(entry));
        } catch (ControllerException e1) {
            throw new ViewException(e1);
        }
        add(editLink);
        AttachmentController attachmentController = new AttachmentController(IceSession.get()
                .getAccount());

        int numAttachments = 0;
        try {
            numAttachments = attachmentController.getNumberOfAttachments(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

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

            @Override
            protected void populateItem(ListItem<Attachment> item) {
                AttachmentController attachmentController = new AttachmentController(IceSession
                        .get().getAccount());

                Attachment attachment = item.getModelObject();

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
                }*/

                BookmarkablePageLink<String> downloadLink = new BookmarkablePageLink<String>(
                        "downloadAttachmentLink", EntryDownloadAttachmentPage.class,
                        new PageParameters("0=" + attachment.getEntry().getId() + ",1="
                                + attachment.getFileName()));

                try {
                    File file = attachmentController.getFile(attachment);
                } catch (ControllerException e) {
                    downloadLink.setEnabled(false);
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to get attachment file!", e);
                }

                String shortFileName = null;
                if (attachment.getFileName().length() > SHORT_FILENAME_LENGTH) {
                    shortFileName = attachment.getFileName().substring(0, SHORT_FILENAME_LENGTH)
                            + "...";
                } else {
                    shortFileName = attachment.getFileName();
                }

                downloadLink.add(new Label("fileName", shortFileName));

                item.add(downloadLink);
            }

        };

        ArrayList<String> emptyAttachmentsArray = new ArrayList<String>();
        emptyAttachmentsArray.add("No Attachments");
        ListView<String> emptyAttachmentsList = new ListView<String>("attachmentsList",
                emptyAttachmentsArray) {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void populateItem(ListItem<String> item) {
                BookmarkablePageLink downloadLink = new BookmarkablePageLink(
                        "downloadAttachmentLink", WelcomePage.class);
                downloadLink.add(new Label("fileName", item.getModelObject()));
                downloadLink.setEnabled(false);
                item.add(downloadLink);

            }
        };

        if (numAttachments != 0) {
            add(new Label("attachmentsCount", " (" + numAttachments + ")"));
            add(attachmentsList);
        } else {
            add(new Label("attachmentsCount", ""));
            add(emptyAttachmentsList);
        }

    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

}
