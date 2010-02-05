package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.web.IceSession;

public class AttachmentsViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    Entry entry = null;
    ArrayList<Attachment> attachments = new ArrayList<Attachment>();
    ArrayList<Panel> panels = new ArrayList<Panel>();

    public AttachmentsViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        class AddAttachmentLink extends AjaxFallbackLink<Object> {
            private static final long serialVersionUID = 1L;

            public AddAttachmentLink(String id) {
                super(id);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                AttachmentsViewPanel thisPanel = (AttachmentsViewPanel) getParent().getParent();
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

        WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLink");
        topLinkContainer.setVisible(PermissionManager.hasWritePermission(entry.getId(), IceSession
                .get().getSessionKey()));
        topLinkContainer.add(new AddAttachmentLink("addAttachmentLink"));
        add(topLinkContainer);

        try {
            attachments.addAll(AttachmentManager.getByEntry(entry));
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        Object[] temp = attachments.toArray();
        if (temp.length == 0) {
            Panel attachmentItemPanel = new EmptyMessagePanel("attachmentItemPanel",
                    "No attachments provided");
            attachmentItemPanel.setOutputMarkupId(true);
            panels.add(attachmentItemPanel);
        } else {
            populatePanels();
        }

        ListView<Object> attachmentsList = generateAttachmentsList("attachmentsListView");
        attachmentsList.setOutputMarkupId(true);
        add(attachmentsList);
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
