package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;

/**
 * Sheet Cell for file inputs
 *
 * @author Hector Plahar
 */
public class FileInputCell extends SheetCell {

    private String fileName = "";
    private String fileId = "";
    private final FlexTable table;
    private int setDataRow = -1;
    private final CellUploader cellUploader;

    public FileInputCell() {
        super();
        cellUploader = new CellUploader();
        cellUploader.addOnFinishUploadHandler(new FileFinishHandler());
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        table.setWidget(0, 0, cellUploader.asWidget());
        initWidget(table);
    }

    public boolean handlesDataSet() {
        return true;
    }

    @Override
    public void setText(String text) {
        GWT.log(text);
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
        cellUploader.submitClick();
    }

    @Override
    public boolean cellSelected(int row, int col) {
        setDataRow = row;
        return true;
    }

    private class FileFinishHandler implements IUploader.OnFinishUploaderHandler {
        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                IUploader.UploadedInfo info = uploader.getServerInfo();
                fileId = info.message;
                if (fileId.isEmpty()) {
                    Window.alert("Could not save file");
                    return;
                }

                fileName = info.name;
                if (setDataRow != -1) {
                    setDataForRow(setDataRow);
                    setDataRow = -1;
                }

//                table.setWidget(0, 0, ImageUtil.getAttachment());
            } else {
                Window.alert("Error uploading file");
            }
        }
    }
}
