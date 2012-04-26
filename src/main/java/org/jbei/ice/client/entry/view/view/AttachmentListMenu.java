package org.jbei.ice.client.entry.view.view;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnCancelUploaderHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.IUploader.OnStatusChangedHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

import java.util.ArrayList;

import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.entry.view.view.AttachmentListMenuPresenter.IAttachmentListMenuView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that displays list of entry attachments in the entry detail view.
 * Allows user to download and also upload an attachment
 * 
 * @author Hector Plahar
 */
public class AttachmentListMenu extends Composite implements IAttachmentListMenuView {

    private final FlexTable layout;
    private Button saveAttachment;
    private Widget attachmentForm;
    private final AttachmentListMenuPresenter presenter;
    private Image quickAdd;
    private long entryId;
    private final TextArea attachmentDescription;

    public AttachmentListMenu() {
        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("attachment_list_menu");
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new HTML("Attachments"));

        quickAdd = ImageUtil.getPlusIcon();
        quickAdd.setStyleName("collection_quick_add_image");
        attachmentDescription = new TextArea();
        attachmentDescription.setStyleName("attachment_description_input");
        attachmentDescription.getElement().setAttribute("placeholder", "Enter File Description");

        panel.add(quickAdd);
        panel.setWidth("100%");
        panel.setCellHorizontalAlignment(quickAdd, HasAlignment.ALIGN_RIGHT);

        saveAttachment = new Button();
        attachmentForm = createAddToAttachment();

        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "entry_view_sub_menu_header");
        layout.setWidget(1, 0, attachmentForm);

        attachmentForm.setVisible(false);

        // this is replaced when menu data is set
        layout.setHTML(2, 0, "No attachments available");
        layout.getCellFormatter().setStyleName(2, 0, "font-75em");
        layout.getCellFormatter().addStyleName(2, 0, "pad-6");

        presenter = new AttachmentListMenuPresenter(this);
    }

    public HandlerRegistration addQuickAddHandler(ClickHandler handler) {
        return quickAdd.addClickHandler(handler);
    }

    /**
     * sets the attachment upload form visibility and the
     * corresponding button user clicks to enable/disable it
     */
    public void switchAttachmentAddButton() {
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
        }
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
            layout.setHTML(2, 0, "No attachments available");
            layout.getCellFormatter().setStyleName(2, 0, "font-75em");
            layout.getCellFormatter().addStyleName(2, 0, "pad-6");
            return;
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

        final VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("180px");

        SingleUploader uploader = new SingleUploader(FileInputType.BROWSER_INPUT,
                new AttachmentUploadStatus(), saveAttachment) {
            @Override
            public Panel getUploaderPanel() {
                return vPanel;
            }
        };

        saveAttachment.setText("Submit");
        saveAttachment.setEnabled(false);
        saveAttachment.setStyleName("entry_attachment_submit_button");
        uploader.setAutoSubmit(false);
        uploader.getForm().setWidth("180px");

        uploader.add(attachmentDescription, 1);
        uploader.setFileInputSize(13);

        uploader.addOnStartUploadHandler(new OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                String attDesc = attachmentDescription.getText().trim();
                uploader.setServletPath(uploader.getServletPath() + "?desc=" + attDesc + "&eid="
                        + entryId + "&type=attachment");
                attachmentDescription.setVisible(false);
            }
        });

        uploader.addOnStatusChangedHandler(new OnStatusChangedHandler() {

            @Override
            public void onStatusChanged(IUploader uploader) {
                switch (uploader.getStatus()) {
                case ERROR:
                    Window.alert(uploader.getServerResponse());
                    saveAttachment.setEnabled(false);
                    break;

                case CHANGED:
                    saveAttachment.setEnabled(true);
                    break;
                }
            }
        });

        uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == Status.SUCCESS) {
                    switchAttachmentAddButton();
                    UploadedInfo info = uploader.getServerInfo();
                    String fileId = info.message;
                    String attDesc = attachmentDescription.getText().trim();
                    int rowCount = layout.getRowCount();
                    AttachmentItem item = new AttachmentItem(rowCount + 1, info.name, attDesc);
                    item.setFileId(fileId);
                    addMenuItem(item);
                    attachmentDescription.setVisible(true);
                    uploader.reset();
                } else {
                    // TODO : notify user of error
                }

                attachmentDescription.setVisible(true);
            }
        });

        uploader.addOnCancelUploadHandler(new OnCancelUploaderHandler() {

            @Override
            public void onCancel(IUploader uploader) {
                uploader.cancel();
                //                attachmentDescription.setVisible(true);
            }
        });

        return uploader;
    }

    private class MenuCell extends HTML implements HasClickHandlers {

        public MenuCell(AttachmentItem item) {

            String name = item.getName();
            if (name.length() > 20) {
                name = (name.substring(0, 18) + "...");
            }

            String description = (item.getDescription() == null || item.getDescription().isEmpty()) ? "No description provided"
                    : item.getDescription();
            String html = "<span class=\"collection_user_menu\">" + name
                    + "</span><br><div class=\"attachment_small_text\">" + description + "</div>";

            setStyleName("entry_detail_view_row");
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant(html);
            setHTML(sb.toSafeHtml());
            setTitle(item.getName());
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }

    // inner classes
    private class AttachmentUploadStatus extends BaseUploadStatus {

        @Override
        protected void addElementsToPanel() {
            panel.add(statusLabel);
            panel.add(cancelLabel);
        }

        @Override
        public void setFileName(String name) {
            if (name.length() > 25) {
                name.lastIndexOf('.');
                name = name.substring(0, 22) + "...";
            }
            fileNameLabel.setText(name);
        }
    }
}
