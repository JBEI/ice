package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;

public class MiniAttachmentsViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final String ATTACHMENTS_URL_KEY = "attachments";

    Entry entry = null;
    ArrayList<Attachment> attachments = new ArrayList<Attachment>();
    ArrayList<Panel> panels = new ArrayList<Panel>();

    public MiniAttachmentsViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        add(new BookmarkablePageLink<Object>("attachmentsLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + ATTACHMENTS_URL_KEY)));

        class AddAttachmentLink extends AjaxFallbackLink<Object> {
            private static final long serialVersionUID = 1L;

            public AddAttachmentLink(String id) {
                super(id);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                MiniAttachmentsViewPanel thisPanel = (MiniAttachmentsViewPanel) getParent()
                        .getParent();
                ArrayList<Panel> thisPanelsPanels = thisPanel.getPanels();
                if (thisPanelsPanels.size() > 0
                        && thisPanelsPanels.get(0) instanceof AttachmentItemEditPanel) {
                    // If the first item is already an edit form, do nothing.
                } else {
                    Attachment newAttachment = new Attachment();
                    newAttachment.setEntry(thisPanel.getEntry());
                    Panel newAttachmentEditPanel = new AttachmentItemEditPanel(
                            "attachmentItemPanel", newAttachment);
                    newAttachmentEditPanel.setOutputMarkupId(true);

                    panels.add(0, newAttachmentEditPanel);

                    target.getPage().replace(thisPanel);
                    target.addComponent(thisPanel);
                }
            }
        }

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLink");
            topLinkContainer.setVisible(entryController.hasWritePermission(entry));
            //topLinkContainer.add(new AddAttachmentLink("addAttachmentLink"));
            //add(topLinkContainer);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        AttachmentController attachmentController = new AttachmentController(IceSession.get()
                .getAccount());

        try {
            attachments.addAll(attachmentController.getAttachments(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        Object[] temp = attachments.toArray();
        if (temp.length == 0) {
            Panel attachmentItemPanel = new EmptyMessagePanel("attachmentItemPanel",
                    "No attachments provided");
            attachmentItemPanel.setOutputMarkupId(true);
            //panels.add(attachmentItemPanel);
        } else {
            populatePanels();
        }

        ListView<Object> attachmentsList = generateAttachmentsList("attachmentsListView");
        attachmentsList.setOutputMarkupId(true);
        //add(attachmentsList);
    }

    public void populatePanels() {
        int counter = 1;
        panels.clear();
        for (Attachment attachment : attachments) {
            Panel attachmentItemPanel = new AttachmentItemViewPanel("attachmentItemPanel", counter,
                    attachment);
            attachmentItemPanel.setOutputMarkupId(true);
            panels.add(attachmentItemPanel);
            counter = counter + 1;
        }
    }

    public ListView<Object> generateAttachmentsList(String id) {
        ListView<Object> attachmentsListView = new ListView<Object>(id, panels) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Object> item) {
                Panel panel = (Panel) item.getModelObject();
                item.add(panel);
            }
        };

        return attachmentsListView;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public ArrayList<Panel> getPanels() {
        return panels;
    }
}
