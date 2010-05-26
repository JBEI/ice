package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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
import org.jbei.ice.web.pages.EntryViewPage;

public class MiniAttachmentsViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final String ATTACHMENTS_URL_KEY = "attachments";

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
        } catch (ControllerException e1) {
            throw new ViewException(e1);
        }
        add(new Label("attachmentsCount", "(" + numAttachments + ")"));

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLink");
            topLinkContainer.setVisible(entryController.hasWritePermission(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        try {
            attachments.addAll(attachmentController.getAttachments(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        if (attachments.size() > 0) {
            BookmarkablePageLink<Object> moreLink = new BookmarkablePageLink<Object>("moreLink",
                    EntryViewPage.class, new PageParameters("0=" + entry.getId() + ",1="
                            + ATTACHMENTS_URL_KEY));
            moreLink.setVisible(false);
            int showLimit = 4;
            if (attachments.size() > showLimit) {
                moreLink.setVisible(true);

                attachments = new ArrayList<Attachment>(attachments.subList(0, showLimit));
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
                        downloadLink = new DownloadLink("downloadAttachmentLink",
                                attachmentController.getFile(attachment), attachment.getFileName());
                    } catch (ControllerException e) {
                        downloadLink = new Link("downloadAttachmentLink") {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onClick() {
                            }
                        };
                        downloadLink.setEnabled(false);
                    } catch (PermissionException e) {
                        throw new ViewPermissionException("No permissions to get attachment file!",
                                e);
                    }
                    if (downloadLink != null) {
                        downloadLink.add(new Label("fileName", attachment.getFileName()));
                        item.add(downloadLink);
                    }

                }

            };

            add(attachmentsList);
            add(moreLink);
        }
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

}
