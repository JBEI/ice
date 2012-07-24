package org.jbei.ice.client.bulkimport.sheet;

import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.SheetCellData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;

/**
 * Sheet Cell for file inputs. Since multiple file uploads should be allowed concurrently,
 * cannot have a single instance for the entire column
 *
 * @author Hector Plahar
 */
public class FileInputCell extends SheetCell {

    private int setDataRow = -1;
    private final FlexTable table;
    private final HashMap<Integer, CellUploader> rowUploaderMap; // each cell has its own uploader

    public FileInputCell() {
        super();
        rowUploaderMap = new HashMap<Integer, CellUploader>();
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);
    }

    public boolean handlesDataSet() {
        return true;
    }

    @Override
    public void setText(String text) {
    }

    /**
     * Sets data for row specified in the param, using the user entered value in the input widget
     *
     * @param row current row user is working on
     * @return display for user entered value
     */
    @Override
    public String setDataForRow(int row) {
        return "";
    }

    @Override
    public void setFocus(int row) {
        // typically called when user wants to edit cell (e.g. by starting to type)
        CellUploader cellUploader = rowUploaderMap.get(row);
        if (cellUploader == null) {
            cellUploader = new CellUploader();
            cellUploader.addOnFinishUploadHandler(new FileFinishHandler());
            rowUploaderMap.put(row, cellUploader);
        }

        cellUploader.submitClick();
    }

    @Override
    public boolean cellSelected(int row, int col) {
        setDataRow = row;
        return true;
    }

    @Override
    public Widget getWidget(int row, boolean isCurrentSelection) {

        CellUploader cellUploader = rowUploaderMap.get(row);

        if (cellUploader == null) {
            cellUploader = new CellUploader();
            cellUploader.addOnFinishUploadHandler(new FileFinishHandler());
            rowUploaderMap.put(row, cellUploader);
        }

        GWT.log(cellUploader.getStatus().toString());
        switch (cellUploader.getStatus()) {
            case UNINITIALIZED:
            default:
                if (isCurrentSelection) {
                    table.setWidget(0, 0, cellUploader.asWidget());
                    break;
                } else {
                    Label label;
                    SheetCellData data = getDataForRow(row);
                    if (data != null) {
                        String text = data.getValue();
                        if (text != null && text.length() > 18)
                            text = (text.substring(0, 16) + "...");

                        label = new Label(text);
                    } else
                        label = new Label();
                    label.setStyleName("cell");
                    return label;
                }

            case INPROGRESS:
                if (isCurrentSelection)
                    cellUploader.asWidget().setStyleName("uploader_cell_selected");
                else
                    cellUploader.asWidget().setStyleName("uploader_cell");
                break;

            case DONE:
            case SUCCESS:
                String text = "";
                SheetCellData data = getDataForRow(row);
                if (data != null) {
                    text = data.getValue();

                    if (text != null && text.length() > 18)
                        text = (text.substring(0, 16) + "...");
                }
                table.setHTML(0, 0, text);
                break;
        }
        return table;
    }

    private class FileFinishHandler implements IUploader.OnFinishUploaderHandler {
        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                IUploader.UploadedInfo info = uploader.getServerInfo();
                String fileId = info.message;
                if (fileId.isEmpty()) {
                    Window.alert("Could not save file");
                    return;
                }

                if (setDataRow != -1) {
                    setWidgetValue(setDataRow, info.name, fileId);
                    setDataRow = -1;
                }

                String name = info.name;
                if (name != null && name.length() > 18)
                    name = (name.substring(0, 16) + "...");
                table.setHTML(0, 0, name);
                table.setStyleName("cell");

            } else {
                Window.alert("Error uploading file");
            }
        }
    }
}
