package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class AttachmentListMenu extends Composite {

    private final FlexTable layout;
    private Button cancelAttachmentSubmission;
    private Button saveAttachment;
    private Widget attachmentForm;
    private final AttachmentMenuPresenter presenter;

    public AttachmentListMenu() {
        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_view_right_menu_2"); // TODO cannot find what I am using 1 for
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new HTML("Attachments"));

        final Button button = new Button("+9", new ClickHandler() { // TODO : use a push button

                    @Override
                    public void onClick(ClickEvent event) {
                        boolean visible = attachmentForm.isVisible();
                        attachmentForm.setVisible(!visible);
                    }
                });

        panel.add(button);
        panel.setWidth("100%");
        panel.setCellHorizontalAlignment(button, HasAlignment.ALIGN_RIGHT);

        saveAttachment = new Button("Save");
        cancelAttachmentSubmission = new Button("Cancel");
        attachmentForm = createAddToAttachment();

        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "entry_view_sub_menu_header");
        layout.setWidget(1, 0, attachmentForm);

        cancelAttachmentSubmission.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO : clear the form field values
                attachmentForm.setVisible(false);
            }
        });

        attachmentForm.setVisible(false);
        presenter = new AttachmentMenuPresenter(this);
    }

    void setMenuItems(ArrayList<AttachmentItem> items) {

        int row = 2;

        // clear rows 2+
        while (row < layout.getRowCount()) {
            layout.removeRow(row);
            row += 1;
        }

        row = 2;

        for (AttachmentItem item : items) {
            final MenuCell cell = new MenuCell(item);
            cell.addClickHandler(presenter.getCellClickHandler(item));
            layout.setWidget(row, 0, cell);
            row += 1;
        }
    }

    protected Widget createAddToAttachment() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");

        table.setHTML(0, 0, "<b class=\"font-80em\">File</b>");
        table.setWidget(1, 0, new FileUpload());
        table.setHTML(2, 0, "<b class=\"font-80em\">Description</b>");
        TextArea area = new TextArea();
        table.setWidget(3, 0, area);
        HTMLPanel panel = new HTMLPanel(
                "<span id=\"save_attachment\"></span><span id=\"cancel_attachment_submission\"></span>");

        panel.add(saveAttachment, "save_attachment");
        panel.add(cancelAttachmentSubmission, "cancel_attachment_submission");
        table.setWidget(4, 0, panel);
        table.getFlexCellFormatter().setHorizontalAlignment(4, 0, HasAlignment.ALIGN_RIGHT);
        return table;
    }

    private class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final AttachmentItem item;

        public MenuCell(AttachmentItem item) {

            this.item = item;

            String name = item.getName();
            if (name.length() > 20) {
                name = (name.substring(0, 18) + "...");
            }

            String description = (item.getDescription() == null || item.getDescription().isEmpty()) ? "No description provided"
                    : item.getDescription();
            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + name
                    + "</span><br /><span class=\"attachment_small_text\" style=\"padding-left: 2px\">"
                    + description + "</span>";

            panel = new HTMLPanel(html);
            panel.setStyleName("entry_detail_view_row");
            panel.setTitle(item.getName());
            initWidget(panel);
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        private AttachmentItem getItem() {
            return this.item;
        }
    }

    public class AttachmentMenuPresenter {

        private final AttachmentListMenu view;

        public AttachmentMenuPresenter(AttachmentListMenu view) {
            this.view = view;
        }

        public ClickHandler getCellClickHandler(final AttachmentItem item) {
            return new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    String url = GWT.getHostPageBaseURL() + "download?type=attachment&id="
                            + item.getFileId();
                    Window.open(url, "Attachment Download", "");
                }
            };
        }
    }
}
