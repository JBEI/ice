package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import org.jbei.ice.client.bulkimport.SheetPresenter;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Sheet extends Composite implements SheetPresenter.View {

    private final FocusPanel panel;
    protected final FlexTable layout;
    protected FlexTable sheetTable; // table used to represent the spreadsheet
    protected int row; // current row in the spreadsheet

    private Label lastReplaced; // cache of the last widget that was replaced
    private int currentRow;
    private int currentIndex;

    private int inputRow;
    private int inputIndex;

    protected final FlexTable colIndex;
    protected final ScrollPanel wrapper;
    protected final ScrollPanel colIndexWrapper;
    protected final FlexTable header;
    protected final ScrollPanel headerWrapper;

    private int headerCol;

    protected final SheetPresenter presenter;

    private final TextBox input;
    private SuggestBox box;
    private String filename;
    private final HashMap<Integer, String> attachmentRowFileIds;
    private final HashMap<Integer, String> sequenceRowFileIds;

    private final static int ROW_COUNT = 50;

    public Sheet(EntryAddType type) {
        this(type, null);
    }

    public Sheet(EntryAddType type, BulkImportDraftInfo info) {

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
        wrapper.setWidth((Window.getClientWidth() - 40) + "px");
        wrapper.setHeight((Window.getClientHeight() - 340 - 25) + "px");

        colIndex = new FlexTable();
        colIndex.setCellPadding(0);
        colIndex.setCellSpacing(0);
        colIndex.setStyleName("sheet_col_index");
        colIndexWrapper = new ScrollPanel(colIndex);
        colIndexWrapper.setHeight((Window.getClientHeight() - 340 - 25) + "px");

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
        headerWrapper.setWidth((Window.getClientWidth() - 15) + "px");

        addScrollHandlers();

        // presenter
        presenter = new SheetPresenter(this, type, info);
        init();
    }

    // experimental
    public void decreaseWidthBy(int amount) {
        wrapper.setWidth((wrapper.getOffsetWidth() - amount) + "px");
        headerWrapper.setWidth((headerWrapper.getOffsetWidth() - amount) + "px");
    }

    public void increaseWidthBy(int amount) {
        wrapper.setWidth((wrapper.getOffsetWidth() + amount) + "px");
        headerWrapper.setWidth((headerWrapper.getOffsetWidth() + amount) + "px");
    }

    private void addWindowResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {

            private int previousWidth = Window.getClientWidth();

            @Override
            public void onResize(ResizeEvent event) {
                int delta = event.getWidth() - previousWidth;
                previousWidth = event.getWidth();
                if (delta < 0) {
                    delta *= -1;
                    wrapper.setWidth((wrapper.getOffsetWidth() - delta) + "px");
                    headerWrapper.setWidth((headerWrapper.getOffsetWidth() - delta) + "px");
                } else {
                    wrapper.setWidth((wrapper.getOffsetWidth() + delta) + "px");
                    headerWrapper.setWidth((headerWrapper.getOffsetWidth() + delta) + "px");
                }

                int wrapperHeight = (event.getHeight() - 340 - 30);
                if (wrapperHeight >= 0)
                    wrapper.setHeight(wrapperHeight + "px");

                int rowIndexHeight = (event.getHeight() - 340 - 30 - 15);
                if (rowIndexHeight >= 0)
                    colIndexWrapper.setHeight(rowIndexHeight + "px");
            }
        });
    }

    private void addScrollHandlers() {
        wrapper.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                headerWrapper.setHorizontalScrollPosition(wrapper.getHorizontalScrollPosition());
                colIndexWrapper.setVerticalScrollPosition(wrapper.getVerticalScrollPosition());
            }
        });
    }

    protected void init() {
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowX", "hidden");

        DOM.setStyleAttribute(colIndexWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(colIndexWrapper.getElement(), "overflowX", "hidden");

        createHeaderCells();

        // get header
        layout.setWidget(0, 0, headerWrapper);
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.setWidget(1, 0, colIndexWrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(1, 1, wrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        // add rows
        int count = ROW_COUNT;
        int i = 1;

        while (count > 0) {

            presenter.addRow();

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

        Header[] headers = presenter.getTypeHeaders();
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
//        header.getFlexCellFormatter().setStyleName(row, headerCol, "tail_cell_column_header_td");
        headerCol += 1;
    }

    @Override
    public void clear() {
        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            final int FIELDS = presenter.getFieldSize();

            for (int j = 0; j < FIELDS; j += 1) {
                HasText widget = (HasText) sheetTable.getWidget(row, i);
                widget.setText("");
                ((Widget) widget).setStyleName("cell");
            }
        }
    }

    public ArrayList<EntryInfo> getCellData() {
        return presenter.getCellEntryList();

//        ArrayList<SheetFieldData[]> cellData = new ArrayList<SheetFieldData[]>();
//
//        Header[] headers = presenter.getTypeHeaders();
//        SheetFieldData[] row;
//
//        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
//            if (isEmptyRow(i))
//                continue;
//
//            row = new SheetFieldData[headers.length];
//
//            int y = 0;
//            for (Header header : headers) {
//
//                String id = "";
//                switch (header) {
//                    case ATT_FILENAME:
//                        id = attachmentRowFileIds.get(i);
//                        break;
//
//                    case SEQ_FILENAME:
//                        id = sequenceRowFileIds.get(i);
//                        break;
//                }
//
//                HasText widget = (HasText) sheetTable.getWidget(i, y);
//                row[y] = new SheetFieldData(header, id, widget.getText());
//                y += 1;
//            }
//
//            cellData.add(row);
//        }
//
//        return cellData;
    }

    @Override
    public int getSheetRowCount() {
        return sheetTable.getRowCount();
    }

    @Override
    public String getCellText(int row, int col) {
        HasText widget = (HasText) sheetTable.getWidget(row, col);
        return widget.getText().trim();
    }

    @Override
    public void highlightHeaders(int row, int col) {
        // TODO Auto-generated method stub
    }

    // currently goes through each row and cell and checks to cell value
    @Override
    public boolean isEmptyRow(int row) {
        int cellCount = sheetTable.getCellCount(row);

        for (int i = 0; i < cellCount; i += 1) {
            HasText widget = (HasText) sheetTable.getWidget(row, i);
            if (widget == null)
                continue;

            String text = widget.getText();

            if (text != null && !text.trim().isEmpty())
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
            for (Header header : presenter.getTypeHeaders()) {
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

    /**
     * Replaces the cell with an input widget that is determined by the type of header
     */
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
        final Header currentHeader = presenter.getTypeHeaders()[currentIndex];
        String text = sheetTable.getText(currentRow, currentIndex);

        // TODO : cache. this is called repeatedly for each click, resulting in the objects in here being created 
        switch (currentHeader) {
            case BIOSAFETY:
                MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

                oracle.addAll(BioSafetyOptions.getDisplayList());
                MultipleTextBox textBox = new MultipleTextBox();
                box = new SuggestBox(oracle, textBox);
                box.setStyleName("cell_input");
                box.setWidth("129px");
                box.setText(text);
                sheetTable.setWidget(currentRow, currentIndex, box);
                textBox.setFocus(true);
                break;

            case SELECTION_MARKERS:
                AutoCompleteField field = AutoCompleteField.fieldValue(currentHeader.name());
                ArrayList<String> list = presenter.getAutoCompleteData(field);

                oracle = new MultiWordSuggestOracle();
                oracle.addAll(new TreeSet<String>(list));
                textBox = new MultipleTextBox();
                box = new SuggestBox(oracle, textBox);
                box.setStyleName("cell_input");
                box.setWidth("129px");
                box.setText(text);
                sheetTable.setWidget(currentRow, currentIndex, box);
                textBox.setFocus(true);
                break;

            case ATT_FILENAME:
            case SEQ_FILENAME:
                CellUploader uploader = new CellUploader();
                uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
                    @Override
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
                            selectCell(currentRow, currentIndex);
                        } else {
                            // TODO : notify user of error
                        }
                    }
                });

                sheetTable.setWidget(currentRow, currentIndex, uploader.asWidget());
                break;

            default:
                input.setText(text);
                sheetTable.setWidget(currentRow, currentIndex, input);
                input.setFocus(true);
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
        Header currentHeader = presenter.getTypeHeaders()[currentIndex];
        if (currentHeader.hasAutoComplete())
            return;

        selectCell(currentRow - 1, currentIndex);
    }

    private void dealWithDownArrowPress() {
        if (currentRow == -1 && currentIndex == -1)
            return;

        if (!isRowInBounds(currentRow + 1))
            return;

        // exit for down arrow press in auto complete box
        Header currentHeader = presenter.getTypeHeaders()[currentIndex];
        if (currentHeader.hasAutoComplete())
            return;

        selectCell(currentRow + 1, currentIndex);
    }

    private void dealWithRightArrowPress() {
        if ((currentRow == -1 && currentIndex == -1))
            return;

        int cellCount = sheetTable.getCellCount(currentRow);
        if (currentIndex + 1 >= cellCount)
            return;

        // auto scroll wrapper
        int max = wrapper.getMaximumHorizontalScrollPosition();
        int width = wrapper.getOffsetWidth();
        int nextIndex = currentIndex + 1;

        // 130 is the width of the cell
        if (130 * (nextIndex + 1) > width) {
            int nextScrollPosition = wrapper.getHorizontalScrollPosition() + 130;
            if (nextScrollPosition > max)
                nextScrollPosition = max;
            wrapper.setHorizontalScrollPosition(nextScrollPosition);
        }

        selectCell(currentRow, currentIndex + 1);
    }

    private void dealWithLeftArrowPress() {

        if ((currentRow == -1 && currentIndex == -1))
            return;

        int nextIndex = currentIndex - 1;
        if (nextIndex < 0)
            return;

        int min = wrapper.getMinimumHorizontalScrollPosition();
        int current = wrapper.getHorizontalScrollPosition();

        if (130 * (nextIndex - 1) < current) {
            int nextScrollPosition = wrapper.getHorizontalScrollPosition() - 130;
            if (nextScrollPosition < min)
                nextScrollPosition = min;
            wrapper.setHorizontalScrollPosition(nextScrollPosition);
        }
        selectCell(currentRow, nextIndex);
    }

    /**
     * cell selection via click or arrow press
     *
     * @param newRow user selected row
     * @param newCol user selected column
     */
    private void selectCell(int newRow, int newCol) {
        //        HTML corner = new HTML(
        //                "<div style=\"position: relative; width: 5px; height: 5px; background-color: blue; top:
        // 12px; right: -122px; border: 3px solid white\"></div>");

        if (currentRow == newRow && currentIndex == newCol)
            return;

        if (currentRow >= 0 && currentIndex >= 0) {
            Widget widget = sheetTable.getWidget(currentRow, currentIndex);
            if (widget != null)
                widget.removeStyleName("cell_selected");
        }

        if (lastReplaced != null) {

            String inputText = getLastWidgetText();
            lastReplaced.setTitle(inputText);
            if (inputText != null && inputText.length() > 15)
                inputText = (inputText.substring(0, 13) + "...");
            lastReplaced.setText(inputText);

            sheetTable.setWidget(inputRow, inputIndex, lastReplaced);
            inputRow = inputIndex = -1; // lastReplaced not visible
            lastReplaced = null;
            panel.setFocus(true);          // not in click
        }

        sheetTable.getWidget(newRow, newCol).addStyleName("cell_selected");
        currentRow = newRow;
        currentIndex = newCol;
    }

    public int getRow() {
        return this.row;
    }

    @Override
    public HashMap<Integer, String> getAttachmentRowFileIds() {
        return this.attachmentRowFileIds;
    }

    @Override
    public HashMap<Integer, String> getSequenceRowFileIds() {
        return this.sequenceRowFileIds;
    }

    @Override
    public void setCellWidgetForCurrentRow(Header header, String display, String title, int col) {
        Widget widget = new HTML(display);
        widget.setTitle(title);
        widget.setStyleName("cell");
        sheetTable.setWidget(row, col, widget);
    }

    public void setRow(int row) {
        this.row = row;
    }

    /**
     * poor method name. what this attempts to do is
     * determine what the last widget was that the user interacted with
     * and return the text for that. E.g. if the user entered a value in the suggest
     * box (for auto complete boxes), this method determines that and returns the value
     * for the box instead of the value for the input (Textbox), which is for regular fields.
     *
     * @return text of last widget user interacted with
     */
    private String getLastWidgetText() {
        Header currentHeader = presenter.getTypeHeaders()[inputIndex];
        String ret;

        switch (currentHeader) {

            case SELECTION_MARKERS:
            case BIOSAFETY:
                ret = ((MultipleTextBox) box.getTextBox()).getWholeText();
                box.setText("");
                return ret;

            case ATT_FILENAME:
            case SEQ_FILENAME:
                ret = filename;
                filename = "";
                return ret;

            default:
                ret = input.getText();
                input.setText("");
                return ret;
        }
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

            selectCell(cell.getRowIndex(), cell.getCellIndex());
        }
    }

    protected class CellDoubleClick implements DoubleClickHandler {

        @Override
        public void onDoubleClick(DoubleClickEvent event) {
            switchToInput();
        }
    }
}