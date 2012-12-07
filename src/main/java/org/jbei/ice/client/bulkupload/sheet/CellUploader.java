package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.entry.EntryInfo;

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

    public CellUploader(final boolean sequenceUpload, final int row, final EntryInfoDelegate delegate,
            final Boolean isStrainWithPlasmidPlasmid) {
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
        uploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                EntryInfo info = delegate.getInfoForRow(row, isStrainWithPlasmidPlasmid);
                long id = info == null ? 0 : info.getId();
                uploader.setServletPath("servlet.gupld?type=bulk_file_upload&is_sequence="
                                                + Boolean.toString(sequenceUpload)
                                                + "&sid=" + ClientController.sessionId + "&eid=" + id);
            }
        });

        uploader.addOnCancelUploadHandler(new IUploader.OnCancelUploaderHandler() {

            @Override
            public void onCancel(IUploader uploader) {
                uploader.cancel();
                uploader.reset();
            }
        });
    }

    public IUploadStatus.Status getStatus() {
        return uploader.getStatus();
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
