package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.common.util.ImageUtil;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IFileInput;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

/**
 * @author Hector Plahar
 */
public class CellUploader implements IsWidget {

    private final SingleUploader uploader;
    private Image fileUploadImg;

    public CellUploader() {
        fileUploadImg = ImageUtil.getFileUpload();
        fileUploadImg.setHeight((fileUploadImg.getHeight() - 2) + "px");
        fileUploadImg.setStyleName("cursor_pointer");

        final FileUploadStatus uploaderStatus = new FileUploadStatus();
        uploader = new SingleUploader(IFileInput.FileInputType.CUSTOM.with(fileUploadImg), uploaderStatus);
        uploader.setAutoSubmit(true);
        uploader.getWidget().setStyleName("uploader_cell");

        uploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath("servlet.gupld?type=bulk_file_upload&sid=" + AppController.sessionId);
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

    public void submitClick() {
        DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false),
                                 fileUploadImg);
    }

    public void addOnFinishUploadHandler(IUploader.OnFinishUploaderHandler onFinishUploaderHandler) {
        uploader.addOnFinishUploadHandler(onFinishUploaderHandler);
    }

    @Override
    public Widget asWidget() {
        return uploader.getWidget();
    }

    public void setHasFile(boolean hasFile) {
        // TODO
        fileUploadImg = ImageUtil.getFileUpload();
        fileUploadImg.setHeight((fileUploadImg.getHeight() - 2) + "px");
        fileUploadImg.setStyleName("cursor_pointer");
    }
}
