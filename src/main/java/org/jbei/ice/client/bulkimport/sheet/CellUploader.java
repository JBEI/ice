package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IFileInput;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;
import org.jbei.ice.client.AppController;

/**
 * @author Hector Plahar
 */
public class CellUploader implements IsWidget {

    private final SingleUploader uploader;

    public CellUploader() {
        Label label = new Label("Upload file"); // TODO : style

        final FileUploadStatus uploaderStatus = new FileUploadStatus();
        uploader = new SingleUploader(IFileInput.FileInputType.CUSTOM.with(label), uploaderStatus);
        uploader.setAutoSubmit(true);
        uploader.getWidget().setStyleName("uploader_cell");

        uploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath(uploader.getServletPath()
                                                + "?type=bulk_attachment&sid=" + AppController.sessionId);
                //                    sheetTable.setWidget(currentRow, currentIndex,
                //                        uploaderStatus.getProgressWidget());
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

    public void addOnFinishUploadHandler(IUploader.OnFinishUploaderHandler onFinishUploaderHandler) {
    }

    @Override
    public Widget asWidget() {
        return uploader.getWidget();
    }
}
