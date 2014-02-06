package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellUploader;
import org.jbei.ice.client.bulkupload.widget.CellWidget;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sheet Cell for file inputs. Since multiple file uploads should be allowed concurrently,
 * cannot have a single instance for the entire column
 *
 * @author Hector Plahar
 */
public class FileInputCell extends SheetCell {

    private final CellUploader uploader;
    private final boolean sequenceUpload;
    private final boolean traceSeqUpload;
    private final EntryInfoDelegate delegate;
    private final EntryType entryType;
    private final Label cellLabel;
    private final HTMLPanel panel;

    public FileInputCell(boolean sequenceUpload, boolean traceSeqUpload,
            EntryInfoDelegate delegate, EntryAddType addType, EntryType type) {
        super();
        cellLabel = new Label();
        cellLabel.setStyleName("display-inline");
        this.sequenceUpload = sequenceUpload;
        this.traceSeqUpload = traceSeqUpload;
        this.delegate = delegate;
        this.entryType = type;
        String html = "<span><span id=\"name_link\"></span> <span id=\"delete_link\"></span></span>";
        panel = new HTMLPanel(html);
        panel.setStyleName("font-75em");

        this.uploader = new CellUploader(delegate, addType, type);
        setUploadHandlers();
    }

    protected void setUploadHandlers() {
        final HTML delete = new HTML("<i class=\"delete_icon " + FAIconType.TRASH.getStyleName() + "\">");
        delete.setStyleName("display-inline");
        delete.setVisible(false);
        panel.add(cellLabel, "name_link");
        panel.add(delete, "delete_link");

        delete.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SheetCellData data = removeDataForRow(uploader.getCurrentRow());
                if (!traceSeqUpload) {
                    if (sequenceUpload)
                        data.setType(EntryField.SEQ_FILENAME);
                    else
                        data.setType(EntryField.ATT_FILENAME);
                } else {
                    data.setType(EntryField.SEQ_TRACE_FILES);
                }

                long entryId = uploader.getCurrentId();
                if (entryId == 0 && delegate != null) {
                    entryId = delegate.getEntryIdForRow(uploader.getCurrentRow());
                } else {
                    Window.alert("Could not delete file");
                    return;
                }

                delegate.deleteUploadedFile(entryId, data);
                uploader.resetPanelWidget();
            }
        });

        uploader.getPanel().addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (cellLabel.getText().trim().isEmpty())
                    return;
                delete.setVisible(true);
            }
        }, MouseOverEvent.getType());

        uploader.getPanel().addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                delete.setVisible(false);
            }
        }, MouseOutEvent.getType());

        uploader.addOnFinishUploadHandler(new FileFinishHandler());
    }

    @Override
    public void reset() {
        super.reset();
        uploader.resetPanelWidget();
    }

    @Override
    public void setText(String text) {
    }

    /**
     * {@inheritDoc}
     *
     * @param {@inheritDoc}
     * @return an empty string
     */
    @Override
    public String setDataForRow(int row) {
        return "";
    }

    @Override
    public void setFocus(int row) {
        uploader.setCurrentRow(row);
        uploader.setSequenceUpload(sequenceUpload);
        uploader.setTraceSequenceUpload(traceSeqUpload);
        uploader.submitClick();
    }

    @Override
    public boolean handlesSelection() {
        return true;
    }

    /**
     * Retrieves the widget to be used for display in the bulk upload cell
     *
     * @param row                bulk upload row
     * @param isCurrentSelection true is selection is current user selection (focus) or previous
     * @param tabIndex           index for tab
     * @return widget for display
     */
    @Override
    public Widget getWidget(int row, boolean isCurrentSelection, int tabIndex) {
        SheetCellData data = getDataForRow(row);
        String value = "";
        if (data != null) {
            value = data.getValue();
            String name = value;
            if (name != null && name.length() > 17)
                name = (name.substring(0, 14) + "...");
            cellLabel.setText(name);
        } else
            cellLabel.setText("");

        if (isCurrentSelection) {
            uploader.setPanelWidget(panel);
            uploader.setCurrentRow(row);
            final FocusPanel focusPanel = new FocusPanel();
            focusPanel.setTabIndex(tabIndex);
            focusPanel.setWidget(uploader);
            focusPanel.setFocus(true);

            focusPanel.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent event) {
                    focusPanel.setStyleName("cell");
                }
            });

            focusPanel.addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    focusPanel.setStyleName("cell_border");
                    uploader.setSequenceUpload(sequenceUpload);
                    uploader.setTraceSequenceUpload(traceSeqUpload);
                }
            });
            return focusPanel;
        } else {
            // if this cell is not the current focused selection, just display
            // a regular cell widget
            CellWidget widget = new CellWidget(value, tabIndex);
            widget.setFocus(false);
            return widget;
        }
    }

    private class FileFinishHandler implements FormPanel.SubmitCompleteHandler {

        @Override
        public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
            String message = event.getResults();
            if (message == null || message.isEmpty() || message.startsWith("Error")) {
                Window.alert("Could not save file");
                return;
            }

            int row = uploader.getCurrentRow();
            String[] split = message.split(",");
            SheetCellData datum = new SheetCellData();
            String name = message;

            // for attachments we expect bid, eid, fileId, filename (without bid and eid if not new)
            // for sequences we expect just filename
            if (split.length == 4) {
                // bid, eid, fileId, fileName
                try {
                    long bulkUploadId = Long.decode(split[0]);
                    long entryId = Long.decode(split[1]);
                    String fileId = split[2];
                    name = split[3];
                    delegate.callBackForLockedColumns(uploader.getCurrentRow(), bulkUploadId, entryId, entryType);
                    datum.setId(fileId);
                    datum.setValue(name);
                } catch (NumberFormatException nfe) {
                    if (message.startsWith("Error")) {
                        Window.alert(message);
                        return;
                    }
                }
            } else if (split.length == 2) {
                // attachment: fileName, fileId
                String fileId = split[0];
                name = split[1];
                datum.setId(fileId);
                datum.setValue(name);
            } else if (split.length == 1) {
                datum.setId(message);
                datum.setValue(message);
            } else {
                Window.alert("There was a problem uploading the file");
                return;
            }

            setWidgetValue(row, datum);
            if (name != null && name.length() > 17)
                name = (name.substring(0, 14) + "...");
            cellLabel.setText(name);
            uploader.resetForm();
            uploader.setPanelWidget(panel);
        }
    }
}
