package org.jbei.ice.client.bulkimport.sheet;

import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.SheetCellData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
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

    private final HashMap<Integer, CellUploader> rowUploaderMap; // each cell has its own uploader

    public FileInputCell() {
        super();
        rowUploaderMap = new HashMap<Integer, CellUploader>();
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
            FileFinishHandler handler = new FileFinishHandler(cellUploader, row);
            cellUploader.addOnFinishUploadHandler(handler);
            rowUploaderMap.put(row, cellUploader);
        }

        cellUploader.submitClick();
    }

    @Override
    public boolean handlesSelection() {
        return true;
    }

    @Override
    public Widget getWidget(int row, boolean isCurrentSelection) {

        CellUploader cellUploader = rowUploaderMap.get(row);

        if (cellUploader == null) {
            cellUploader = new CellUploader();
            FileFinishHandler handler = new FileFinishHandler(cellUploader, row);
            cellUploader.addOnFinishUploadHandler(handler);
            rowUploaderMap.put(row, cellUploader);
        }

        GWT.log(cellUploader.getStatus().toString());
        if (isCurrentSelection) {
            cellUploader.asWidget().setStyleName("uploader_cell_selected");
            return cellUploader.asWidget();
        } else {
            Label label;
            SheetCellData data = getDataForRow(row);
            if (data != null) {
                String text = data.getValue();
                if (text != null && text.length() > 12)
                    text = (text.substring(0, 9) + "...");

                label = new Label(text);
            } else
                label = new Label();
            label.setStyleName("cell");
            return label;
        }
    }

    private class FileFinishHandler implements IUploader.OnFinishUploaderHandler {

        private final CellUploader cellUploader;
        private final int row;

        public FileFinishHandler(CellUploader uploader, int row) {
            this.cellUploader = uploader;
            this.row = row;
        }

        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                IUploader.UploadedInfo info = uploader.getServerInfo();
                String fileId = info.message;
                if (fileId.isEmpty()) {
                    Window.alert("Could not save file");
                    return;
                }

                setWidgetValue(row, info.name, fileId);
                String name = info.name;
                if (name != null && name.length() > 16)
                    name = (name.substring(0, 14) + "...");

                Label label = new Label(name);
                label.setStyleName("display-inline");

                final Label delete = new Label("x");
                delete.setStyleName("x-delete");

                cellUploader.getPanel().addDomHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        delete.setVisible(true);
                    }
                }, MouseOverEvent.getType());

                cellUploader.getPanel().addDomHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        delete.setVisible(false);
                    }
                }, MouseOutEvent.getType());

                String html = "<span><span id=\"name_link\"></span> <span id=\"delete_link\"></span></span>";
                HTMLPanel panel = new HTMLPanel(html);

                delete.addStyleName("display-inline");
                delete.setVisible(false);
                panel.add(label, "name_link");
                panel.add(delete, "delete_link");
                delete.addClickHandler(new DeleteFileClickHandler(cellUploader, row));

                cellUploader.setPanelWidget(panel);
                cellUploader.reset();

            } else {
                Window.alert("Error uploading file");
            }
        }
    }

    private class DeleteFileClickHandler implements ClickHandler {

        private final CellUploader cellUploader;
        private final int row;

        public DeleteFileClickHandler(CellUploader cellUploader, int row) {
            this.cellUploader = cellUploader;
            this.row = row;
        }

        /**
         * Called when a native click event is fired.
         *
         * @param event the {@link com.google.gwt.event.dom.client.ClickEvent} that was fired
         */
        @Override
        public void onClick(ClickEvent event) {
            SheetCellData removed = FileInputCell.this.removeDataForRow(row);
            cellUploader.resetPanelWidget();
        }
    }
}
