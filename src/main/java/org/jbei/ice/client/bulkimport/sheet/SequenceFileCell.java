package org.jbei.ice.client.bulkimport.sheet;

import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;

/**
 * @author Hector Plahar
 */
public class SequenceFileCell extends SheetCell {

    private String fileName = "";
    private final CellUploader uploader;

    public SequenceFileCell() {
        super();
        uploader = new CellUploader();
        uploader.addOnFinishUploadHandler(new FileFinishHandler());
        initWidget(uploader.asWidget());
    }

    @Override
    public void setText(String text) {
    }

    @Override
    public String getWidgetText() {
        String ret = fileName;
        fileName = "";
        return ret;
    }

    @Override
    public void setFocus() {
//        Window.alert("focus");
    }

    private class FileFinishHandler implements IUploader.OnFinishUploaderHandler {
        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                IUploader.UploadedInfo info = uploader.getServerInfo();
                String fileId = info.message;
                if (fileId.isEmpty())
                    return; // TODO : hook into error message

                // attachment or
//                attachmentRowFileIds.put(currentRow, info.message);

                fileName = info.name;
//                selectCell(currentRow, currentIndex);
            } else {
                // TODO : notify user of error
            }
        }
    }
}
