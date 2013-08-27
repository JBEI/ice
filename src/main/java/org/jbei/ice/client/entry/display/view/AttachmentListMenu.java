package org.jbei.ice.client.entry.display.view;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.entry.display.handler.HasAttachmentDeleteHandler;
import org.jbei.ice.client.entry.display.view.AttachmentListMenuPresenter.IAttachmentListMenuView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 * Widget that displays list of entry attachments in the entry detail view.
 * Allows user to download and also upload an attachment
 *
 * @author Hector Plahar
 */
public class AttachmentListMenu extends Composite implements IAttachmentListMenuView {

    private final FlexTable layout;
    private final Button saveAttachment;
    private final VerticalPanel formPanel;
    private final AttachmentListMenuPresenter presenter;
    private final Icon quickAdd;
    private long entryId;
    private HasAttachmentDeleteHandler handler;
    private HTML cancelUpload;

    public AttachmentListMenu() {
        layout = new FlexTable();
        initWidget(layout);

        initComponents();

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_attribute");
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new HTML("<i class=\"" + FAIconType.PAPER_CLIP.getStyleName()
                                   + " font-80em\"></i> &nbsp;Attachments"));

        quickAdd = new Icon(FAIconType.PLUS_SIGN);
        quickAdd.addStyleName("edit_icon");


        panel.add(quickAdd);
        panel.setWidth("100%");
        panel.setCellHorizontalAlignment(quickAdd, HasAlignment.ALIGN_RIGHT);

        saveAttachment = new Button();
        formPanel = new VerticalPanel();
        createAddToAttachment();

        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "entry_attributes_sub_header");
        layout.setWidget(1, 0, formPanel);
        layout.getRowFormatter().setVisible(1, false);

//        attachmentForm.setVisible(false);

        // this is replaced when menu data is set
        layout.setHTML(2, 0, "&nbsp;");
        layout.getCellFormatter().setStyleName(2, 0, "font-75em");
        layout.getCellFormatter().addStyleName(2, 0, "pad-6");

        presenter = new AttachmentListMenuPresenter(this);
    }

    private void initComponents() {
        cancelUpload = new HTML("Cancel");
        cancelUpload.setStyleName("footer_feedback_widget");
        cancelUpload.addStyleName("font-70em");
        cancelUpload.addStyleName("display-inline");
    }

    @Override
    public HandlerRegistration addQuickAddHandler(ClickHandler handler) {
        return quickAdd.addClickHandler(handler);
    }

    @Override
    public void setCancelHandler(ClickHandler handler) {
        cancelUpload.addClickHandler(handler);
    }

    public void setCanEdit(boolean visible) {
        quickAdd.setVisible(visible);
    }

    /**
     * sets the attachment upload form visibility and the corresponding button user clicks to enable/disable it
     */
    @Override
    public void switchAttachmentAddButton() {
        if (formPanel == null)
            return;

//        if (formPanel.isVisible()) {
//            formPanel.setVisible(false);
//        } else {
//            formPanel.setVisible(true);
//        }
        layout.getRowFormatter().setVisible(1, !layout.getRowFormatter().isVisible(1));
    }

    void setMenuItems(ArrayList<AttachmentItem> items, long entryId) {
        this.entryId = entryId;
        int row = 2;

        // clear rows 2+
        while (row < layout.getRowCount()) {
            layout.removeRow(row);
            row += 1;
        }

        if (items.isEmpty()) {
            reset();
            return;
        }

        row = 2;

        for (AttachmentItem item : items) {
            final MenuCell cell = new MenuCell(item);
            cell.setDownloadHandler(presenter.getCellClickHandler(item));
            cell.addDeleteHandler(presenter.getDeleteClickHandler(handler, item));
            layout.setWidget(row, 0, cell);
            row += 1;
        }
    }

    public void reset() {
        for (int i = 2; i < layout.getRowCount(); i += 1)
            layout.removeRow(i);

        layout.setHTML(2, 0, "<i style=\"color: #999\">No attachments available</i>");
        layout.getCellFormatter().setStyleName(2, 0, "font-75em");
        layout.getCellFormatter().addStyleName(2, 0, "pad-6");
        presenter.reset();
    }

    @Override
    public void addMenuItem(AttachmentItem item, int itemCount) {
        int row;
        if (itemCount == 0)
            row = 2;
        else
            row = layout.getRowCount();

        layout.getCellFormatter().removeStyleName(2, 0, "font-75em");
        layout.getCellFormatter().removeStyleName(2, 0, "pad-6");

        final MenuCell cell = new MenuCell(item);
        cell.setDownloadHandler(presenter.getCellClickHandler(item));
        cell.addDeleteHandler(presenter.getDeleteClickHandler(handler, item));
        layout.setWidget(row, 0, cell);
    }

    public ArrayList<AttachmentItem> getAttachmentItems() {
        return presenter.getAttachmentItems();
    }

    protected void createAddToAttachment() {
        formPanel.setWidth("180px");

        final FormPanel panel = new FormPanel();
        panel.setWidth("180px");
        panel.setAction("/upload?type=attachment&sid=" + ClientController.sessionId);
        panel.setEncoding(FormPanel.ENCODING_MULTIPART);
        panel.setMethod(FormPanel.METHOD_POST);

        final FileUpload fileUpload = new FileUpload();
        fileUpload.setName("uploadFormElement");
        fileUpload.setStyleName("font-75em");
        fileUpload.setWidth("180px");
        panel.add(fileUpload);

        HTMLPanel actionPanel = new HTMLPanel("<span id=\"upload_att\"></span>&nbsp;<span id=\"cancel_att\"></span>");
        actionPanel.add(saveAttachment, "upload_att");
        actionPanel.add(cancelUpload, "cancel_att");

        final TextArea description = new TextArea();
        description.setStyleName("attachment_description_input");
        description.getElement().setAttribute("placeholder", "Enter File Description");

        formPanel.add(panel);
        formPanel.add(description);
        formPanel.add(actionPanel);
        formPanel.setCellHorizontalAlignment(actionPanel, HasAlignment.ALIGN_RIGHT);

        saveAttachment.setText("Submit");

        panel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                panel.setAction(panel.getAction() + "&eid=" + entryId + "&desc=" + description.getText());
            }
        });

        panel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String[] split = event.getResults().split(",");
                if (split.length < 2) {
                    Window.alert("Error uploading file");
                    return;
                }
                switchAttachmentAddButton();
                String attDesc = description.getText().trim();
                int rowCount = layout.getRowCount();
                AttachmentItem item = new AttachmentItem(rowCount + 1, split[1], attDesc);
                item.setFileId(split[0]);
                presenter.addAttachmentItem(item);
                description.setVisible(true);
                panel.reset();
            }
        });

        saveAttachment.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (fileUpload.getFilename().isEmpty())
                    return;

                panel.submit();
            }
        });
    }

    private class MenuCell extends Composite implements HasMouseOverHandlers, HasMouseOutHandlers {

        private final Icon delete;
        private final HTMLPanel panel;
        private final AttachmentItem item;
        private final Label fileName;

        public MenuCell(AttachmentItem item) {
            String name = item.getName();
            if (name.length() > 20) {
                name = (name.substring(0, 18) + "...");
            }

            this.item = item;
            fileName = new Label(name);
            fileName.setStyleName("display-inline");
            delete = new Icon(FAIconType.TRASH);
            delete.addStyleName("delete_icon");
            delete.setVisible(false);

            String description;
            if (item.getDescription() == null || item.getDescription().isEmpty())
                description = "No description provided";
            else
                description = item.getDescription();

            String html = "<span class=\"collection_user_menu\"><span id=\"attachment_file_name\"></span>"
                    + "<span id=\"delete_link\" style=\"cursor: pointer; float: right\"></span></span><br>";

            if (description.length() > 163)
                html += "<span title=\"" + description + "\" class=\"attachment_small_text\">"
                        + description.subSequence(0, 160) + "...</span>";
            else
                html += "<span style=\"color: #999;font-size: 11px\">" + description + "</span>";

            panel = new HTMLPanel(html);

            initWidget(panel);
            panel.add(delete, "delete_link");
            panel.add(fileName, "attachment_file_name");
            setStyleName("entry_detail_view_row");

            // show delete icon on mouse over
            this.addMouseOverHandler(new MouseOverHandler() {

                @Override
                public void onMouseOver(MouseOverEvent event) {
                    delete.setVisible(true);
                }
            });

            // hide delete icon on mouse out
            this.addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    delete.setVisible(false);
                }
            });
        }

        public void setDownloadHandler(ClickHandler handler) {
            fileName.addClickHandler(handler);
        }

        public void addDeleteHandler(ClickHandler handler) {
            delete.addClickHandler(handler);
        }

        @Override
        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }

        @Override
        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }

        public AttachmentItem getItem() {
            return this.item;
        }
    }

    public void removeAttachment(AttachmentItem item) {
        int rowCount = layout.getRowCount();

        for (int i = 2; i < rowCount; i += 1) {
            Widget widget = layout.getWidget(i, 0);
            if (!(widget instanceof MenuCell)) {
                continue;
            }

            MenuCell cell = (MenuCell) widget;
            if (cell.getItem().getFileId().equals(item.getFileId())) {
                layout.removeRow(i);
                break;
            }
        }

        if (layout.getRowCount() == 2) {
            layout.setHTML(2, 0, "<i style=\"color: #999\">No attachments available</i>");
            layout.getCellFormatter().setStyleName(2, 0, "font-75em");
            layout.getCellFormatter().addStyleName(2, 0, "pad-6");
        }
    }

    public void setDeleteHandler(HasAttachmentDeleteHandler handler) {
        this.handler = handler;
    }
}
