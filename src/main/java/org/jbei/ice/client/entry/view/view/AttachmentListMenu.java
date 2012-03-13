package org.jbei.ice.client.entry.view.view;

import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

import java.util.ArrayList;

import org.jbei.ice.client.common.util.ImageUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AttachmentListMenu extends Composite {

    private final FlexTable layout;
    private Button cancelAttachmentSubmission;
    private Button saveAttachment;
    private Widget attachmentForm;
    private final AttachmentMenuPresenter presenter;
    private Image quickAdd;
    private long entryId;

    public AttachmentListMenu() {
        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_view_right_menu_2"); // TODO cannot find what I am using 1 for
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new HTML("Attachments"));

        quickAdd = ImageUtil.getPlusIcon();
        quickAdd.setStyleName("collection_quick_add_image");
        quickAdd.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                switchButton();
            }
        });

        panel.add(quickAdd);
        panel.setWidth("100%");
        panel.setCellHorizontalAlignment(quickAdd, HasAlignment.ALIGN_RIGHT);

        saveAttachment = new Button("Save");
        cancelAttachmentSubmission = new Button("Cancel");
        attachmentForm = createAddToAttachment();

        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "entry_view_sub_menu_header");
        layout.setWidget(1, 0, attachmentForm);

        cancelAttachmentSubmission.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                switchButton();
            }
        });

        attachmentForm.setVisible(false);
        presenter = new AttachmentMenuPresenter(this);

        // this is replaced when menu data is set
        layout.setHTML(2, 0,
            "<span style=\"padding: 2px\" class=\"font-75em\">No attachments available</span>");
    }

    public void switchButton() {
        if (attachmentForm == null)
            return;

        if (attachmentForm.isVisible()) {
            quickAdd.setUrl(ImageUtil.getPlusIcon().getUrl());
            quickAdd.setStyleName("collection_quick_add_image");
            attachmentForm.setVisible(false);
        } else {
            quickAdd.setUrl(ImageUtil.getMinusIcon().getUrl());
            quickAdd.setStyleName("collection_quick_add_image");
            attachmentForm.setVisible(true);
            //            quickAddBox.setFocus(true);// TODO 
        }
    }

    void setMenuItems(ArrayList<AttachmentItem> items, long entryId) {
        this.entryId = entryId;
        if (items.isEmpty())
            return;

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

    void addMenuItem(AttachmentItem item) {
        int row = layout.getRowCount();
        final MenuCell cell = new MenuCell(item);
        cell.addClickHandler(presenter.getCellClickHandler(item));
        layout.setWidget(row, 0, cell);
    }

    protected Widget createAddToAttachment() {

        SingleUploader uploader = new SingleUploader(FileInputType.BROWSER_INPUT, null,
                saveAttachment) {
            @Override
            public Panel getUploaderPanel() {
                VerticalPanel vPanel = new VerticalPanel();
                vPanel.setWidth("180px");
                return vPanel;
            }
        };
        uploader.setAutoSubmit(false);
        final String desc = "Enter File Description";
        final TextArea area = new TextArea();
        area.setText(desc);
        HTMLPanel panel = new HTMLPanel(
                "<br><div style=\"word-wrap: break-word\" id=\"attachment_input_description\"></div>");
        panel.add(area, "attachment_input_description");

        area.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                if (desc.equalsIgnoreCase(area.getText().trim()))
                    area.setText("");
            }
        });

        area.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if (area.getText().trim().isEmpty())
                    area.setText(desc);
            }
        });

        uploader.add(panel, 1);
        uploader.add(saveAttachment);
        uploader.setWidth("180px");

        // TODO : use presenter @see AttachmentListPresenter
        uploader.addOnStartUploadHandler(new OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                String attDesc = desc.equals(area.getText()) ? "" : area.getText();
                uploader.setServletPath(uploader.getServletPath() + "?desc=" + attDesc + "&eid="
                        + entryId + "&type=attachment");
            }
        });

        uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == Status.SUCCESS) {
                    switchButton();
                    UploadedInfo info = uploader.getServerInfo();
                    String fileId = info.message;
                    String attDesc = desc.equals(area.getText()) ? "" : area.getText();
                    AttachmentItem item = new AttachmentItem(layout.getRowCount() + 1, info.name,
                            attDesc);
                    item.setFileId(fileId);
                    addMenuItem(item);
                    uploader.reset();
                } else {
                    // TODO : notify user of error
                }
            }
        });

        return uploader;
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
                    String url = GWT.getHostPageBaseURL() + "download?type=attachment&name="
                            + item.getName() + "&id=" + item.getFileId();
                    Window.open(url, "Attachment Download", "");
                }
            };
        }
    }
}
