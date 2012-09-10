package org.jbei.ice.client.bulkupload.sheet.cell;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellUploader;
import org.jbei.ice.client.bulkupload.widget.CellWidget;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
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
    private final HashMap<Integer, CellWidget> widgetHashMap;
    private final boolean sequenceUpload;

    public FileInputCell(boolean sequenceUpload) {
        super();
        rowUploaderMap = new HashMap<Integer, CellUploader>();
        widgetHashMap = new HashMap<Integer, CellWidget>();
        this.sequenceUpload = sequenceUpload;
    }

    @Override
    public void reset() {
        super.reset();
        rowUploaderMap.clear();
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
            cellUploader = new CellUploader(sequenceUpload);
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
    public Widget getWidget(int row, boolean isCurrentSelection, int tabIndex) {

        CellUploader cellUploader = rowUploaderMap.get(row);

        if (cellUploader == null) {
            cellUploader = new CellUploader(sequenceUpload);

            final SheetCellData datum = getDataForRow(row);
            if (datum != null) {
                String name = datum.getValue();
                if (name != null && name.length() > 13)
                    name = (name.substring(0, 10) + "...");

                Label label = new Label(name);
                label.setStyleName("display-inline");
                label.addStyleName("font-85em");

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
            }

            FileFinishHandler handler = new FileFinishHandler(cellUploader, row);
            cellUploader.addOnFinishUploadHandler(handler);
            rowUploaderMap.put(row, cellUploader);
        }

        if (isCurrentSelection) {
            final FocusPanel panel = new FocusPanel();
            panel.setTabIndex(tabIndex);
            panel.setWidget(cellUploader.asWidget());
            panel.setFocus(isCurrentSelection);

            panel.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent event) {
                    panel.setStyleName("cell");
                }
            });

            panel.addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    panel.setStyleName("cell_border");
                }
            });
            return panel;
        } else {
            SheetCellData data = getDataForRow(row);
            String value = "";
            if (data != null)
                value = data.getValue();
            CellWidget widget = widgetHashMap.get(row);
            if (widget == null) {
                widget = new CellWidget(value, tabIndex);
                if (tabIndex != -1)
                    widgetHashMap.put(row, widget);
            } else {
                widget.setValue(value);
            }
            return widget;
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

                SheetCellData datum = new SheetCellData();
                datum.setId(fileId);
                datum.setValue(info.name);
                setWidgetValue(row, datum);
                String name = info.name;
                if (name != null && name.length() > 13)
                    name = (name.substring(0, 10) + "...");

                Label label = new Label(name);
                label.setStyleName("display-inline");
                label.addStyleName("font-85em");

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
