package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IFileInput;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

/**
 * @author Hector Plahar
 */
public class CellUploader implements IsWidget {

    private final SingleUploader uploader;
    private HTML fileUploadImg;
    private HorizontalPanel panel;
    private HandlerRegistration finishUploadRegistration;
    private long currentId;
    private HandlerRegistration startUpRegistration;

    public CellUploader(final boolean sequenceUpload, final int row, final EntryInfoDelegate delegate,
            final EntryAddType addType, final EntryType type) {
        fileUploadImg = new HTML("<i class=\"" + FAIconType.UPLOAD.getStyleName() + "\"></i>");
        fileUploadImg.addStyleName("cursor_pointer");
        fileUploadImg.addStyleName("opacity_hover");

        panel = new HorizontalPanel();
        panel.setWidth("100%");

        final FileUploadStatus uploaderStatus = new FileUploadStatus();
        uploader = new SingleUploader(IFileInput.FileInputType.CUSTOM.with(fileUploadImg), uploaderStatus) {
            @Override
            public Panel getUploaderPanel() {
                return panel;
            }
        };

        uploader.setAutoSubmit(true);
        IUploader.OnStartUploaderHandler handler = createStartUpHandler(row, delegate,
                                                                        sequenceUpload, type.name(), addType.name());
        if (startUpRegistration != null)
            startUpRegistration.removeHandler();
        startUpRegistration = uploader.addOnStartUploadHandler(handler);

        uploader.addOnCancelUploadHandler(new IUploader.OnCancelUploaderHandler() {

            @Override
            public void onCancel(IUploader uploader) {
                uploader.cancel();
                uploader.reset();
            }
        });
    }

    protected IUploader.OnStartUploaderHandler createStartUpHandler(final int row, final EntryInfoDelegate delegate,
            final boolean sequenceUpload, final String typeName, final String addTypeName) {
        return new IUploader.OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                currentId = delegate.getEntryIdForRow(row);
                long bid = delegate.getBulkUploadId();
                uploader.setServletPath("/upload?type=bulk_file_upload&is_sequence="
                                                + Boolean.toString(sequenceUpload)
                                                + "&sid=" + ClientController.sessionId + "&eid=" + currentId
                                                + "&bid=" + bid + "&entry_type=" + typeName
                                                + "&entry_add_type=" + addTypeName);
            }
        };
    }

    public IUploadStatus.Status getStatus() {
        return uploader.getStatus();
    }

    public long getCurrentId() {
        return this.currentId;
    }

    public void submitClick() {
        DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false),
                                 fileUploadImg);
    }

    public void addOnFinishUploadHandler(IUploader.OnFinishUploaderHandler onFinishUploaderHandler) {
        if (finishUploadRegistration != null)
            finishUploadRegistration.removeHandler();
        finishUploadRegistration = uploader.addOnFinishUploadHandler(onFinishUploaderHandler);
    }

    @Override
    public Widget asWidget() {
        return uploader.getWidget();
    }

    public void setPanelWidget(final Widget widget) {
        panel.clear();
        panel.add(uploader.getForm());
        panel.add(widget);
    }

    public void resetPanelWidget() {
        panel.clear();
        panel.add(uploader.getForm());
    }

    public void reset() {
        uploader.reset();
    }

    public HorizontalPanel getPanel() {
        return this.panel;
    }
}
