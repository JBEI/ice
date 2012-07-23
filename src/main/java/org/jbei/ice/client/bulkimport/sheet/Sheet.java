package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.SheetPresenter;
import org.jbei.ice.client.bulkimport.model.SheetCellData;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class Sheet extends Composite implements SheetPresenter.View {

    private final FocusPanel focusPanel;
    protected final FlexTable layout;
    protected FlexTable sheetTable; // table used to represent the spreadsheet
    protected int row; // current row in the spreadsheet

    private Label lastReplaced; // cache of the last widget that was replaced. it is set when a switch to input occurs
    private int currentRow;
    private int currentIndex;

    private int inputRow;     // row of last cell that was switch to input
    private int inputIndex;   // index of last cell that was switched to input

    protected final FlexTable colIndex;
    protected final ScrollPanel sheetTableFocusPanelwrapper;
    protected final ScrollPanel colIndexWrapper;
    protected final FlexTable header;
    protected final ScrollPanel headerWrapper;

    private int headerCol;

    protected final SheetPresenter presenter;
    private SheetCell newCellSelection;
    private boolean cellHasFocus;

    public final static int ROW_COUNT = 50;

    public Sheet(EntryAddType type) {
        this(type, null);
    }

    public Sheet(EntryAddType type, BulkImportInfo info) {

        headerCol = 0;

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

        // placing sheet in focus focusPanel to be able to add handlers for mouse and keyboard events on the sheet
        focusPanel = new FocusPanel(sheetTable);
        focusPanel.setWidth("100%");
        focusPanel.setHeight("100%");
        focusPanel.setStyleName("focus_panel");

        // then wrap it in a scroll focusPanel that expands to fill area given by browser
        sheetTableFocusPanelwrapper = new ScrollPanel(focusPanel);
        sheetTableFocusPanelwrapper.setWidth((Window.getClientWidth() - 40) + "px");
        sheetTableFocusPanelwrapper.setHeight((Window.getClientHeight() - 340 - 30) + "px");

        colIndex = new FlexTable();
        colIndex.setCellPadding(0);
        colIndex.setCellSpacing(0);
        colIndex.setStyleName("sheet_col_index");
        colIndexWrapper = new ScrollPanel(colIndex);
        colIndexWrapper.setHeight((Window.getClientHeight() - 340 - 30 - 15) + "px");

        addPanelHandlers();
        addWindowResizeHandler();

        currentRow = inputRow = -1;
        currentIndex = inputIndex = -1;

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

    public void setCurrentInfo(BulkImportInfo info) {
        presenter.setCurrentInfo(info);
    }

    // experimental
    public void decreaseWidthBy(int amount) {
        sheetTableFocusPanelwrapper.setWidth((sheetTableFocusPanelwrapper.getOffsetWidth() - amount) + "px");
        headerWrapper.setWidth((headerWrapper.getOffsetWidth() - amount) + "px");
    }

    public void increaseWidthBy(int amount) {
        sheetTableFocusPanelwrapper.setWidth((sheetTableFocusPanelwrapper.getOffsetWidth() + amount) + "px");
        headerWrapper.setWidth((headerWrapper.getOffsetWidth() + amount) + "px");
    }

    private void addWindowResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {

            private int previousWidth = Window.getClientWidth();

            @Override
            public void onResize(ResizeEvent event) {
                int delta = event.getWidth() - previousWidth;
                previousWidth = event.getWidth();
                sheetTableFocusPanelwrapper.setWidth((sheetTableFocusPanelwrapper.getOffsetWidth() + delta) + "px");
                headerWrapper.setWidth((headerWrapper.getOffsetWidth() + delta) + "px");

                int wrapperHeight = (event.getHeight() - 340 - 30);
                if (wrapperHeight >= 0)
                    sheetTableFocusPanelwrapper.setHeight(wrapperHeight + "px");

                int rowIndexHeight = (event.getHeight() - 340 - 30 - 15);
                if (rowIndexHeight >= 0)
                    colIndexWrapper.setHeight(rowIndexHeight + "px");
            }
        });
    }

    private void addScrollHandlers() {
        sheetTableFocusPanelwrapper.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                headerWrapper.setHorizontalScrollPosition(sheetTableFocusPanelwrapper.getHorizontalScrollPosition());
                colIndexWrapper.setVerticalScrollPosition(sheetTableFocusPanelwrapper.getVerticalScrollPosition());
            }
        });
    }

    protected void init() {
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowX", "hidden");

        DOM.setStyleAttribute(colIndexWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(colIndexWrapper.getElement(), "overflowX", "hidden");


        // get header
        layout.setWidget(0, 0, headerWrapper);
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.setWidget(1, 0, colIndexWrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(1, 1, sheetTableFocusPanelwrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        createHeaderCells();

        // add rows
        int count = ROW_COUNT;
        int i = 1;
        row = 0;

        while (count > 0) {

            presenter.addRow(row);

            // index col
            HTML indexCell = new HTML(i + "");

            // TODO : right click menu here

            colIndex.setWidget(row, 0, indexCell);
            indexCell.setStyleName("index_cell");
            colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");

            count -= 1;
            i += 1;
            row += 1;
        }
    }

    public void setAutoCompleteData(HashMap<AutoCompleteField, ArrayList<String>> data) {
        presenter.setAutoCompleteData(data);
    }

    private void addPanelHandlers() {

        focusPanel.addKeyDownHandler(new KeyDownHandler() {

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

        focusPanel.addBlurHandler(new BlurHandler() {

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
            Header[] headers = presenter.getTypeHeaders();

            for (int j = 0; j < FIELDS; j += 1) {
                HasText widget = (HasText) sheetTable.getWidget(row, i);
                widget.setText("");
                ((Widget) widget).setStyleName("cell");
                headers[j].getCell().reset();
            }
        }
    }

    public ArrayList<EntryInfo> getCellData(String ownerEmail, String owner) {
        return presenter.getCellEntryList(ownerEmail, owner);
    }

    @Override
    public int getSheetRowCount() {
        return sheetTable.getRowCount();
    }

    @Override
    public void highlightHeaders(int row, int col) {
        // TODO Auto-generated method stub
    }

    // currently goes through each row and cell and checks to cell value
    public boolean isEmptyRow(int row) {

        Header[] headers = presenter.getTypeHeaders();
        int cellCount = headers.length;

        for (int i = 0; i < cellCount; i += 1) {
            Header header = headers[i];

            if (header.getCell().getDataForRow(row) != null)
                return false;
        }

        return true;
    }

    public boolean validate() {
        return presenter.validateCells();
    }

    @Override
    public void clearErrorCell(int row, int col) {
        Widget widget = sheetTable.getWidget(row, col);
        if (widget != null && widget instanceof Label) {
            Label label = (Label) widget;
            label.removeStyleName("cell_error");
            label.setTitle("");
        }
    }

    @Override
    public void setErrorCell(int row, int col, String errMsg) {

        Widget widget = sheetTable.getWidget(row, col);
        if (widget != null && widget instanceof Label) {
            Label label = (Label) widget;
            label.addStyleName("cell_error");
            label.setTitle(errMsg);
        }
    }

    /**
     * Replaces the cell with an input widget that is determined by the type of header
     */
    private void switchToInput() {
        if (cellHasFocus)
            return;

        Widget widget = sheetTable.getWidget(currentRow, currentIndex);
        if ((widget instanceof Label)) {

            // cache the current label we are replacing
            lastReplaced = (Label) widget;
            lastReplaced.removeStyleName("cell_selected");
            inputIndex = currentIndex;
            inputRow = currentRow;
        }

        newCellSelection = presenter.setCellInputFocus(currentRow, currentIndex);
        if (newCellSelection == null)
            return;

        sheetTable.setWidget(currentRow, currentIndex, newCellSelection);
        // all cell to set focus to whatever their input mechanism is.
        // e.g. if an input box, allow focus on that box
        newCellSelection.setFocus();
        cellHasFocus = true;
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
        if (currentHeader.getCell().hasMultiSuggestions())
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
        if (currentHeader.getCell().hasMultiSuggestions())
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
        int max = sheetTableFocusPanelwrapper.getMaximumHorizontalScrollPosition();
        int width = sheetTableFocusPanelwrapper.getOffsetWidth();
        int nextIndex = currentIndex + 1;

        // 130 is the width of the cell
        if (130 * (nextIndex + 1) > width) {
            int nextScrollPosition = sheetTableFocusPanelwrapper.getHorizontalScrollPosition() + 130;
            if (nextScrollPosition > max)
                nextScrollPosition = max;
            sheetTableFocusPanelwrapper.setHorizontalScrollPosition(nextScrollPosition);
        }

        selectCell(currentRow, currentIndex + 1);
    }

    private void dealWithLeftArrowPress() {

        if ((currentRow == -1 && currentIndex == -1))
            return;

        int nextIndex = currentIndex - 1;
        if (nextIndex < 0)
            return;

        int min = sheetTableFocusPanelwrapper.getMinimumHorizontalScrollPosition();
        int current = sheetTableFocusPanelwrapper.getHorizontalScrollPosition();

        if (130 * (nextIndex - 1) < current) {
            int nextScrollPosition = sheetTableFocusPanelwrapper.getHorizontalScrollPosition() - 130;
            if (nextScrollPosition < min)
                nextScrollPosition = min;
            sheetTableFocusPanelwrapper.setHorizontalScrollPosition(nextScrollPosition);
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
        //                "<div style=\"position: relative; width: 5px; height: 5px; background-color: blue; top: "
        //                        + "12px; right: -122px; border: 3px solid white\"></div>");

        // check if user is clicking on the same cell
        if (currentRow == newRow && currentIndex == newCol)
            return;

        SheetCell prevSelection = newCellSelection;
        newCellSelection = presenter.getTypeHeaders()[newCol].getCell();
        cellHasFocus = false;

        if (currentRow >= 0 && currentIndex >= 0) {
            // remove the blue border around the cell
            Widget widget = sheetTable.getWidget(currentRow, currentIndex);
            if (widget != null)
                widget.removeStyleName("cell_selected");
        }

        // determine if user interacted with a cell prior to this cell
        // if so then retrieve the text for the data and set it to the
        // label widget, and display that label widget
        if (lastReplaced != null) {
            String inputText = "";

            if (prevSelection.handlesDataSet()) {
                SheetCellData data = prevSelection.getDataForRow(inputRow);
                if (data != null)
                    inputText = data.getValue();
            } else {
                inputText = prevSelection.setDataForRow(inputRow);
            }

            lastReplaced.setTitle(inputText);

            if (inputText != null && inputText.length() > 18)
                inputText = (inputText.substring(0, 16) + "...");
            lastReplaced.setText(inputText);

            sheetTable.setWidget(inputRow, inputIndex, lastReplaced);
            inputRow = inputIndex = -1; // lastReplaced not visible
            lastReplaced = null;
            focusPanel.setFocus(true); // not in click
        }

        // now deal with current selection
        // check if cell handles selection
        if (newCellSelection.cellSelected(newRow, newCol)) {

            // cell handles selection
            lastReplaced = (Label) sheetTable.getWidget(newRow, newCol);
            lastReplaced.removeStyleName("cell_selected");
            inputIndex = newCol;
            inputRow = newRow;
            sheetTable.setWidget(newRow, newCol, newCellSelection);

        } else {
            Widget cellWidget = sheetTable.getWidget(newRow, newCol);
            cellWidget.addStyleName("cell_selected");
        }

        currentRow = newRow;
        currentIndex = newCol;

        //        if (cellWidget instanceof Label) {
        //            Label label = (Label) cellWidget;
        //
        //            HTMLPanel panel;
        //            if (label.getText().isEmpty())
        //                panel = new HTMLPanel(
        //                        "<div class=\"cell cell_selected\"><div style=\"position: relative; width: 5px;
        // height: 5px; background-color: #0082C0; top: "
        //                                + "12px; right: -122px; border: 3px solid white\"></div></div>");
        //            else
        //                panel = new HTMLPanel(
        //                        "<div class=\"cell cell_selected\">"
        //                                + label.getText()
        //                                + "<div style=\"position: relative; width: 5px; height: 5px;
        // background-color: #0082C0; top: "
        //                                + "-2px; right: -124px; border: 3px solid white\"></div></div>");
        //            sheetTable.setWidget(newRow, newCol, panel);
        //
        //        } else {

        //        }

    }

    @Override
    public void setCellWidgetForCurrentRow(Header header, String value, int row, int col) {
        if (value == null)
            value = "";

        String display = value;
        String title = value;
        if (value.length() > 20)
            display = (value.substring(0, 18) + "...");

        Widget widget = new HTML(display);
        widget.setTitle(title);
        widget.setStyleName("cell");
        sheetTable.setWidget(row, col, widget);
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