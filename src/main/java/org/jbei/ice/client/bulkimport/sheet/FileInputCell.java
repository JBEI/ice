package org.jbei.ice.client.bulkimport.sheet;

import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;

import com.google.gwt.user.client.ui.FlexTable;

/**
 * Sheet Cell for file inputs
 * 
 * @author Hector Plahar
 */
public class FileInputCell extends SheetCell {

    private String fileName = "";
    private String fileId = "";
    private final FlexTable table;

    public FileInputCell() {
        super();
        CellUploader uploader = new CellUploader();
        uploader.addOnFinishUploadHandler(new FileFinishHandler());
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        table.setWidget(0, 0, uploader.asWidget());
        initWidget(table);
    }

    @Override
    public void setText(String text) {
    }

    @Override
    public String setDataForRow(int row) {
        String ret = fileName;
        setWidgetValue(row, fileName, fileId);
        fileName = "";
        fileId = "";
        return ret;
    }

    @Override
    public void setFocus() {
    }

    private class FileFinishHandler implements IUploader.OnFinishUploaderHandler {
        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                IUploader.UploadedInfo info = uploader.getServerInfo();
                fileId = info.message;
                if (fileId.isEmpty())
                    return; // TODO : hook into error message

                fileName = info.name;
            } else {
                // TODO : notify user of error
            }
        }
    }
}
