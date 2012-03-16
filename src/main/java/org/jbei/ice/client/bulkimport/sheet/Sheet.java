package org.jbei.ice.client.bulkimport.sheet;

import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkimport.SheetPresenter;
import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class Sheet extends Composite implements SheetPresenter.View {

    private final FocusPanel panel;
    protected final FlexTable layout;
    protected FlexTable sheetTable; // table use to represent the spreadsheet
    protected int row; // current row in the spreadsheet

    private Label lastReplaced; // cache of the last widget that was replaced
    private int currentRow;
    private int currentIndex;

    private int inputRow;
    private int inputIndex;

    protected final FlexTable colIndex;
    protected final ScrollPanel wrapper;
    protected final ScrollPanel rowIndexWrapper;
    protected final FlexTable header;
    protected final ScrollPanel headerWrapper;

    private final int WIDTH = 40; //300;
    private final int HEIGHT = 320;

    private final EntryAddType type;
    private int headerCol;

    protected final SheetPresenter presenter;

    private final TextBox input;
    private SuggestBox box;
    private String filename;
    private SingleUploader uploader;
    private HashMap<Integer, String> attachmentRowFileIds;
    private HashMap<Integer, String> sequenceRowFileIds;
    private BulkImportDraftInfo info;

    private final static int ROW_COUNT = 100;

    public Sheet(EntryAddType type, BulkImportDraftInfo info) {

        this.type = type;
        this.info = info;

        headerCol = 0;
        attachmentRowFileIds = new HashMap<Integer, String>();
        sequenceRowFileIds = new HashMap<Integer, String>();

        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        initWidget(layout);

        header = new FlexTable();
        header.setCellPadding(0);
        header.setCellSpacing(0);
        header.setWidth("100%");
        header.addStyleName("sheet_header_table");

        sheetTable = new FlexTable();
        sheetTable.setCellPadding(0);
        sheetTable.setCellSpacing(0);
        sheetTable.setStyleName("sheet_table");
        sheetTable.setWidth("100%");

        // placing sheet in focus panel to be able to add handlers for mouse and keyboard events on the sheet
        panel = new FocusPanel(sheetTable);
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setStyleName("focus_panel");

        // then wrap it in a scroll panel that expands to fill area given by browser
        wrapper = new ScrollPanel(panel);
        wrapper.setWidth((Window.getClientWidth() - WIDTH - 25) + "px");
        wrapper.setHeight((Window.getClientHeight() - HEIGHT) + "px");

        colIndex = new FlexTable();
        colIndex.setCellPadding(0);
        colIndex.setCellSpacing(0);
        colIndex.setStyleName("sheet_col_index");
        rowIndexWrapper = new ScrollPanel(colIndex);
        rowIndexWrapper.setHeight((Window.getClientHeight() - HEIGHT - 14) + "px");

        addPanelHandlers();
        addWindowResizeHandler();

        currentRow = inputRow = -1;
        currentIndex = inputIndex = -1;

        input = new TextBox();
        input.setStyleName("cell_input");

        sheetTable.addDoubleClickHandler(new CellDoubleClick());
        sheetTable.addClickHandler(new CellClick());

        // init
        headerWrapper = new ScrollPanel(header);
        init();
        addScrollHandlers();
        addResizeHandler();

        // presenter
        presenter = new SheetPresenter(this);
    }

    private void addResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                headerWrapper.setWidth((wrapper.getOffsetWidth() - 15 + 40) + "px"); // TODO : ditto on 15px here also and the 40 is for the leader_header
            }
        });
    }

    private void addScrollHandlers() {
        wrapper.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                headerWrapper.setHorizontalScrollPosition(wrapper.getHorizontalScrollPosition());
                rowIndexWrapper.setVerticalScrollPosition(wrapper.getVerticalScrollPosition());
            }
        });
    }

    protected void init() {
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowX", "hidden");

        DOM.setStyleAttribute(rowIndexWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(rowIndexWrapper.getElement(), "overflowX", "hidden");

        //  - 260 accounts for left menu bar. get actual width
        headerWrapper.setWidth((Window.getClientWidth() - 20 - 15) + "px"); // TODO : the 15px accounts for the scroll bar. Not sure yet how to get the scrollbar width

        createHeaderCells();

        // get header
        layout.setWidget(0, 0, headerWrapper);
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.setWidget(1, 0, rowIndexWrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(1, 1, wrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        // add rows
        int count = ROW_COUNT;
        int i = 1;

        while (count > 0) {

            this.addRow();

            // index col
            HTML indexCell = new HTML(i + "");
            colIndex.setWidget(row, 0, indexCell);
            indexCell.setStyleName("index_cell");
            colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");

            count -= 1;
            i += 1;
        }
    }

    public void setAutoCompleteData(HashMap<AutoCompleteField, ArrayList<String>> data) {
        presenter.setAutoCompleteData(data);
    }

    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        switch (DOM.eventGetType(event)) {
        case Event.ONDBLCLICK:
            Window.alert("double click");
            break;

        case Event.ONMOUSEUP:
            if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {

                GWT.log("Event.BUTTON_LEFT", null);
                //                listener.onClick(this, event);
            }

            if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
                GWT.log("Event.BUTTON_RIGHT", null);
                event.stopPropagation();
                event.preventDefault();
            }
        }
    }

    private void addWindowResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {

                int wrapperWidth = (event.getWidth() - WIDTH);
                if (wrapperWidth >= 0)
                    wrapper.setWidth(wrapperWidth + "px");

                int wrapperHeight = (event.getHeight() - HEIGHT - 30);
                if (wrapperHeight >= 0)
                    wrapper.setHeight(wrapperHeight + "px");

                int rowIndexHeight = (event.getHeight() - HEIGHT - 30 - 15);
                if (rowIndexHeight >= 0)
                    rowIndexWrapper.setHeight(rowIndexHeight + "px");
            }
        });
    }

    private void addPanelHandlers() {

        panel.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {

                if (event.isUpArrow()) {
                    dealWithUpArrowPress();
                    event.preventDefault();
                } else if (event.isDownArrow()) {
                    dealWithDownArrowPress();
                    event.preventDefault();
                } else if (event.isRightArrow()) {
                    dealWithRightArrowPress();
                    event.preventDefault();
                } else if (event.isLeftArrow()) {
                    dealWithLeftArrowPress();
                    event.preventDefault();
                } else {

                    int code = event.getNativeKeyCode();

                    if (KeyCodes.KEY_TAB == code || KeyCodes.KEY_ENTER == code) {
                        dealWithRightArrowPress();
                        event.preventDefault();
                        return;
                    }

                    switchToInput();
                }
            }
        });

        panel.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if (lastReplaced != null)
                    return;

                if (currentIndex >= 0 && currentRow >= 0) {
                    Widget widget = sheetTable.getWidget(currentRow, currentIndex);
                    widget.removeStyleName("cell_selected");
                    currentRow = currentIndex = -1;
                }
            }
        });

        panel.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                if (event.getNativeButton() == Event.BUTTON_RIGHT) {

                }
            }
        });
    }

    protected Widget createHeaderCells() {
        addLeadHeader();

        Header[] headers = ImportTypeHeaders.getHeadersForType(type);
        new SheetHeader(headers, headerCol, row, header);

        headerCol += headers.length;
        addTailHeader();

        row += 1;
        return header;
    }

    // header that covers the span of the row index
    private void addLeadHeader() {
        HTML cell = new HTML("&nbsp;");
        cell.setStyleName("leader_cell_column_header");
        header.setWidget(row, headerCol, cell);
        header.getFlexCellFormatter().setStyleName(row, headerCol, "leader_cell_column_header_td");
        headerCol += 1;
    }

    // tail header 
    private void addTailHeader() {
        HTML cell = new HTML("&nbsp;");
        cell.setStyleName("tail_cell_column_header");
        header.setWidget(row, headerCol, cell);
        header.getFlexCellFormatter().setStyleName(row, headerCol, "tail_cell_column_header_td");
        headerCol += 1;
    }

    @Override
    public void clear() {
        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            final int FIELDS = ImportTypeHeaders.getHeadersForType(type).length;

            for (int j = 0; j < FIELDS; j += 1) {
                HasText widget = (HasText) sheetTable.getWidget(row, i);
                widget.setText("");
                ((Widget) widget).setStyleName("cell");
            }
        }
    }

    @Override
    public ArrayList<SheetFieldData[]> getCellData() {
        ArrayList<SheetFieldData[]> cellData = new ArrayList<SheetFieldData[]>();

        Header[] headers = ImportTypeHeaders.getHeadersForType(type);
        SheetFieldData[] row = null;

        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            row = new SheetFieldData[headers.length];

            int y = 0;
            for (Header header : headers) {

                String id;
                if (header == Header.ATT_FILENAME) {
                    id = attachmentRowFileIds.get(i);
                } else if (header == Header.SEQ_FILENAME) {
                    id = sequenceRowFileIds.get(i);
                } else
                    id = "";

                HasText widget = (HasText) sheetTable.getWidget(i, y);
                row[y] = new SheetFieldData(header, id, widget.getText());
                y += 1;
            }

            cellData.add(row);
        }

        return cellData;
    }

    @Override
    public void highlightHeaders(int row, int col) {
        // TODO Auto-generated method stub
    }

    // TODO : use a bit map or bit arrays to track user entered values for more efficient lookup
    @Override
    public boolean isEmptyRow(int row) {
        int cellCount = sheetTable.getCellCount(row);

        for (int i = 0; i < cellCount; i += 1) {
            HasText widget = (HasText) sheetTable.getWidget(row, i);
            if (!widget.getText().isEmpty())
                return false;
        }

        return true;
    }

    public boolean validate() {

        boolean validates = true;

        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            // TODO : sometimes input box is active and user clicks submit
            int y = 0;
            for (Header header : ImportTypeHeaders.getHeadersForType(type)) {
                Widget widget = sheetTable.getWidget(i, y);
                if (widget instanceof Label) {
                    Label label = (Label) widget;
                    boolean isEmpty = label.getText().trim().isEmpty();
                    if (isEmpty && header.isRequired()) {
                        label.setStyleName("cell_error");
                        label.setTitle("Required field");
                        validates = false;
                    } else {
                        label.setStyleName("cell");
                        label.setTitle("");
                    }
                }
                y += 1;
            }
        }

        return validates;
    }

    // put textinput in cell
    private void switchToInput() {

        // get widget at current position. expect it to be a label
        Widget widget = sheetTable.getWidget(currentRow, currentIndex);
        if (!(widget instanceof Label))
            return;

        // if a switch has occurred, then set the text for it
        if (lastReplaced != null) {
            String lastText = input.getText(); // TODO : problem here. this is not always input
            sheetTable.setWidget(inputRow, inputIndex, lastReplaced);
            if (lastText != null && !lastText.isEmpty()) {
                Label replaced = lastReplaced;
                replaced.setText(lastText);
            }
        }

        // cache the current label we are replacing 
        lastReplaced = (Label) sheetTable.getWidget(currentRow, currentIndex);
        lastReplaced.removeStyleName("cell_selected");
        inputIndex = currentIndex;
        inputRow = currentRow;

        // replace
        final Header currentHeader = ImportTypeHeaders.getHeadersForType(this.type)[currentIndex];
        FieldType fieldType = currentHeader.geFieldType();
        String text = sheetTable.getText(currentRow, currentIndex);

        if (fieldType == null) {
            input.setText(text);
            sheetTable.setWidget(currentRow, currentIndex, input);
            input.setFocus(true);
        } else {
            // TODO : cache 
            switch (fieldType) {
            case AUTO_COMPLETE:
                AutoCompleteField field = AutoCompleteField.fieldValue(currentHeader.name());
                ArrayList<String> list = presenter.getAutoCompleteData(field);

                MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
                oracle.addAll(new TreeSet<String>(list));
                MultipleTextBox textBox = new MultipleTextBox();
                box = new SuggestBox(oracle, textBox);
                box.setStyleName("cell_input");
                box.setWidth("129px");
                box.setText(text);
                sheetTable.setWidget(currentRow, currentIndex, box);
                textBox.setFocus(true);
                break;

            case FILE_INPUT:
                uploader = new SingleUploader(FileInputType.LABEL);
                uploader.setAutoSubmit(true);
                uploader.getWidget().setSize("129px", "26px");

                //                uploader.getWidget().setStyleName();

                uploader.addOnStartUploadHandler(new OnStartUploaderHandler() {

                    @Override
                    public void onStart(IUploader uploader) {
                        uploader.setServletPath(uploader.getServletPath()
                                + "?type=bulk_attachment&sid=" + AppController.sessionId);
                    }
                });

                uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
                    public void onFinish(IUploader uploader) {
                        if (uploader.getStatus() == Status.SUCCESS) {
                            UploadedInfo info = uploader.getServerInfo();
                            if (info.message.isEmpty())
                                return; // TODO : hook into error message

                            // attachment or 
                            if (currentHeader == Header.ATT_FILENAME) {
                                attachmentRowFileIds.put(currentRow, info.message);
                            } else if (currentHeader == Header.SEQ_FILENAME) {
                                sequenceRowFileIds.put(currentRow, info.message);
                            }

                            filename = info.name;
                            selectCell(currentRow, currentIndex, currentRow, currentIndex);
                        } else {
                            // TODO : notify user of error
                        }
                    }
                });
                sheetTable.setWidget(currentRow, currentIndex, uploader.getWidget());
                break;

            case DATE:
                break;
            }

            // TODO : handle other field types
        }
    }

    private boolean isRowInBounds(int row) {
        int rowSize = sheetTable.getRowCount();
        if ((row >= rowSize) || (row < 0))
            return false;
        return true;
    }

    private void dealWithUpArrowPress() {
        if ((currentRow == -1 && currentIndex == -1) || !isRowInBounds(currentRow - 1))
            return;

        // exit for up arrow press in auto complete box
        Header currentHeader = ImportTypeHeaders.getHeadersForType(this.type)[inputIndex];
        FieldType fieldType = currentHeader.geFieldType();
        if (fieldType == FieldType.AUTO_COMPLETE)
            return;

        selectCell(currentRow, currentIndex, currentRow - 1, currentIndex);
    }

    private void dealWithDownArrowPress() {
        if (currentRow == -1 && currentIndex == -1)
            return;

        if (!isRowInBounds(currentRow + 1))
            return;

        // exit for down arrow press in auto complete box
        Header currentHeader = ImportTypeHeaders.getHeadersForType(this.type)[inputIndex];
        FieldType fieldType = currentHeader.geFieldType();
        if (fieldType == FieldType.AUTO_COMPLETE)
            return;

        selectCell(currentRow, currentIndex, currentRow + 1, currentIndex);
    }

    private void dealWithRightArrowPress() {
        if ((currentRow == -1 && currentIndex == -1))
            return;

        int cellCount = sheetTable.getCellCount(currentRow);
        if (currentIndex + 1 >= cellCount)
            return;

        selectCell(currentRow, currentIndex, currentRow, currentIndex + 1);
    }

    private void dealWithLeftArrowPress() {

        if ((currentRow == -1 && currentIndex == -1))
            return;

        if (currentIndex - 1 < 0)
            return;

        selectCell(currentRow, currentIndex, currentRow, currentIndex - 1);
    }

    private void selectCell(int row, int col, int newRow, int newCol) { // TODO : similar in functionality to cell click
        if (lastReplaced != null) {

            String inputText = getLastWidgetText();
            lastReplaced.setTitle(inputText);
            if (inputText.length() > 15)
                inputText = (inputText.substring(0, 13) + "...");
            lastReplaced.setText(inputText);

            sheetTable.setWidget(inputRow, inputIndex, lastReplaced);
            inputRow = inputIndex = -1; // lastReplaced not visible
            lastReplaced = null;
            panel.setFocus(true);
        } else {
            Widget widget = sheetTable.getWidget(row, col);
            widget.removeStyleName("cell_selected");
        }

        sheetTable.getWidget(newRow, newCol).addStyleName("cell_selected");
        currentRow = newRow;
        currentIndex = newCol;
    }

    public void addRow() {
        int index = row - 1; // row includes the headers but this is 0-indexed

        // type is already set in the constructor 
        Header[] headers = ImportTypeHeaders.getHeadersForType(this.type);
        int headersSize = headers.length;

        for (int i = 0; i < headersSize; i += 1) {
            Widget widget;
            if (info != null && info.getCount() >= row) {
                EntryInfo primaryInfo = info.getPrimary().get(index);
                EntryInfo secondaryInfo = null;
                if (info.getSecondary() != null)
                    secondaryInfo = info.getSecondary().get(index);

                String value = InfoValueExtractorFactory.extractValue(this.type, headers[i],
                    primaryInfo, secondaryInfo, index, attachmentRowFileIds, sequenceRowFileIds);
                if (value == null)
                    value = "";

                String display = value;
                if (value.length() > 15)
                    display = (value.substring(0, 13) + "...");
                widget = new HTML(display);
                widget.setTitle(value);
            } else
                widget = new HTML("");
            widget.setStyleName("cell");

            sheetTable.setWidget(row, i, widget);
            sheetTable.getFlexCellFormatter().setStyleName(row, i, "td_cell");
        }
        row += 1;
    }

    /**
     * poor method name. what this attempts to do is
     * determine what the last widget was that the user interacted with
     * and return the text for that. E.g. if the user entered a value in the suggest
     * box (for auto complete boxes), this method determines that and returns the value
     * for the box instead of the value for the input (Textbox), which is for regular fields.
     * 
     * @return
     */
    private String getLastWidgetText() {
        Header currentHeader = ImportTypeHeaders.getHeadersForType(this.type)[inputIndex];
        FieldType fieldType = currentHeader.geFieldType();
        String ret = "";
        if (fieldType == null) {
            ret = input.getText();
            input.setText("");
            return ret;
        }

        switch (fieldType) {

        case AUTO_COMPLETE:
            ret = box.getText();
            box.setText("");
            break;

        case FILE_INPUT:
            ret = filename;
            filename = "";
            break;

        default:
            ret = input.getText();
            input.setText("");
            break;
        }

        return ret;
    }

    //
    // inner classes
    //

    protected class CellClick implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            Cell cell = sheetTable.getCellForEvent(event);
            if (cell == null)
                return;

            // if we are clicking on the same cell, do nothing
            if (cell.getRowIndex() == currentRow && cell.getCellIndex() == currentIndex) {
                return;
            }

            // clear previously selected cell, if any
            if (currentRow >= 0 && currentIndex >= 0) {
                sheetTable.getWidget(currentRow, currentIndex).removeStyleName("cell_selected");
            }

            // reset and remove input
            if (lastReplaced != null) {
                String inputText = getLastWidgetText();
                lastReplaced.setTitle(inputText);
                if (inputText.length() > 15)
                    inputText = (inputText.substring(0, 13) + "...");
                lastReplaced.setText(inputText);

                sheetTable.setWidget(inputRow, inputIndex, lastReplaced);
                inputRow = inputIndex = -1; // lastReplaced not visible
                lastReplaced = null;
            }

            // highlight currently clicked cell
            currentRow = cell.getRowIndex();
            currentIndex = cell.getCellIndex();
            sheetTable.getWidget(currentRow, currentIndex).addStyleName("cell_selected");
        }
    }

    protected class CellDoubleClick implements DoubleClickHandler {

        @Override
        public void onDoubleClick(DoubleClickEvent event) {
            switchToInput();
        }
    }
}