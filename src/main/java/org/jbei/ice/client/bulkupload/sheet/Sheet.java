package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.bulkupload.SheetPresenter;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.client.bulkupload.widget.CellWidget;
import org.jbei.ice.client.bulkupload.widget.SampleSelectionWidget;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.shared.dto.bulkupload.PreferenceInfo;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class Sheet extends Composite implements SheetPresenter.View {

    protected final FlexTable layout;
    protected FlexTable sheetTable; // table used to represent the spreadsheet
    protected int row; // current row in the spreadsheet

    private int currentRow;
    private int currentIndex;

    private int inputRow;     // row of last cell that was switch to input
    private int inputIndex;   // index of last cell that was switched to input

    protected final FlexTable colIndex;
    protected final ScrollPanel sheetTableFocusPanelWrapper;
    protected final ScrollPanel colIndexWrapper;
    protected final FlexTable header;
    protected final ScrollPanel headerWrapper;
    private CellWidget replaced;

    private int headerCol;

    protected final SheetPresenter presenter;
    private SheetCell newCellSelection;
    private boolean cellHasFocus;  // whether input widget for a cell has focus
    private SampleSelectionWidget sampleSelectionWidget;
    private HandlerRegistration sampleSelectionRegistration;

    public final static int ROW_COUNT = 35;
    private int dragRow = -1;
    private int dragCol = -1;
    private boolean dragging;
    private String startText;

    public Sheet(EntryAddType type, ServiceDelegate<PreferenceInfo> serviceDelegate) {
        this(type, serviceDelegate, null);
    }

    public Sheet(EntryAddType type, ServiceDelegate<PreferenceInfo> serviceDelegate, BulkUploadInfo info) {
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

        // then wrap it in a scroll panel that expands to fill area given by browser
        sheetTableFocusPanelWrapper = new ScrollPanel(sheetTable);
        sheetTableFocusPanelWrapper.setWidth((Window.getClientWidth() - 40) + "px");
        sheetTableFocusPanelWrapper.setHeight((Window.getClientHeight() - 340) + "px");

        colIndex = new FlexTable();
        colIndex.setCellPadding(0);
        colIndex.setCellSpacing(0);
        colIndex.setStyleName("sheet_col_index");
        colIndexWrapper = new ScrollPanel(colIndex);
        colIndexWrapper.setHeight((Window.getClientHeight() - 340 - 15) + "px");

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
        presenter = new SheetPresenter(this, type, info, serviceDelegate);
        init();

//        sheetTable.addDomHandler(new MouseMoveHandler() {
//
//            @Override
//            public void onMouseMove(MouseMoveEvent event) {
//                if (dragging) {
//                        // clear everything from c to maxColumn
////                            CellWidget widget = (CellWidget) sheetTable.getWidget(dragRow + maxColumn, dragCol);
////                            widget.removeStyleName("cell_drag");
//////                            widget.setValue("");
////
////                        CellWidget widget = (CellWidget) sheetTable.getWidget(dragRow + c, dragCol);
//////                        if (startText != null)
//////                            widget.setValue(startText);
////                        widget.addStyleName("cell_drag");
////                        widget.getElement().scrollIntoView();
//                }
//            }
//        }, MouseMoveEvent.getType());
    }

    public SheetPresenter getPresenter() {
        return this.presenter;
    }

    public BulkUploadInfo setUpdatedEntry(BulkUploadAutoUpdate bulkUploadAutoUpdate) {
        return presenter.setUpdateEntry(bulkUploadAutoUpdate);
    }

    // experimental
    public void decreaseWidthBy(int amount) {
        sheetTableFocusPanelWrapper.setWidth((sheetTableFocusPanelWrapper.getOffsetWidth() - amount) + "px");
        headerWrapper.setWidth((headerWrapper.getOffsetWidth() - amount) + "px");
    }

    public void increaseWidthBy(int amount) {
        sheetTableFocusPanelWrapper.setWidth((sheetTableFocusPanelWrapper.getOffsetWidth() + amount) + "px");
        headerWrapper.setWidth((headerWrapper.getOffsetWidth() + amount) + "px");
    }

    public void resetWidth() {
        sheetTableFocusPanelWrapper.setWidth((Window.getClientWidth() - 40) + "px");
        headerWrapper.setWidth((Window.getClientWidth() - 15) + "px");
    }

    private void addWindowResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {

            private int previousWidth = Window.getClientWidth();

            @Override
            public void onResize(ResizeEvent event) {
                // 970 is anticipated width of page window (menu?). "proper" way to do this is detect if
                // window has horizontal scroll bars
                if (Window.getClientWidth() < 970)
                    return;

                int delta = event.getWidth() - previousWidth;
                if (delta < 0)
                    delta = 0;
                previousWidth = event.getWidth();
                sheetTableFocusPanelWrapper.setWidth((sheetTableFocusPanelWrapper.getOffsetWidth() + delta) + "px");
                headerWrapper.setWidth((headerWrapper.getOffsetWidth() + delta) + "px");

                int wrapperHeight = (event.getHeight() - 340);
                if (wrapperHeight >= 0)
                    sheetTableFocusPanelWrapper.setHeight(wrapperHeight + "px");

                int rowIndexHeight = (event.getHeight() - 340 - 15);
                if (rowIndexHeight >= 0)
                    colIndexWrapper.setHeight(rowIndexHeight + "px");
            }
        });
    }

    private void addScrollHandlers() {
        sheetTableFocusPanelWrapper.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                headerWrapper.setHorizontalScrollPosition(sheetTableFocusPanelWrapper.getHorizontalScrollPosition());
                colIndexWrapper.setVerticalScrollPosition(sheetTableFocusPanelWrapper.getVerticalScrollPosition());

                if (row >= 1000)
                    return;

                int vScrollPosition = sheetTableFocusPanelWrapper.getVerticalScrollPosition();
                int maxVscrollPosition = sheetTableFocusPanelWrapper.getMaximumVerticalScrollPosition();

                if (vScrollPosition >= maxVscrollPosition - 100) {
                    presenter.addRow(row);
                    // index col
                    HTML indexCell = new HTML((row + 1) + "");
                    colIndex.setWidget(row, 0, indexCell);
                    indexCell.setStyleName("index_cell");
                    colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");
                    row += 1;
                }
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
        layout.setWidget(1, 1, sheetTableFocusPanelWrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        createHeaderCells(presenter.getPreferenceDelegate());

        int rowCount = presenter.getEntryRowCount() < ROW_COUNT ? ROW_COUNT : presenter.getEntryRowCount();

        long start = System.currentTimeMillis();
        // add rows
        for (row = 0; row < rowCount; row += 1) {
            presenter.addRow(row);
            // index col
            HTML indexCell = new HTML((row + 1) + "");
            colIndex.setWidget(row, 0, indexCell);
            indexCell.setStyleName("index_cell");
            colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");
        }
        long end = System.currentTimeMillis();
        GWT.log("Adding " + rowCount + " took " + (end - start) + "ms");
    }

    private void addPanelHandlers() {
        sheetTable.addDomHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.isUpArrow()) {
                    dealWithUpArrowPress();
                } else if (event.isDownArrow()) {
                    dealWithDownArrowPress();
                    event.preventDefault();
                } else if (event.isRightArrow()) {
                    dealWithRightArrowPress();
//                    event.preventDefault();
                } else if (event.isLeftArrow()) {
                    dealWithLeftArrowPress();
//                    event.preventDefault();
                } else {
                    int code = event.getNativeKeyCode();

                    if (KeyCodes.KEY_TAB == code || KeyCodes.KEY_ENTER == code) {
                        if (currentIndex == presenter.getFieldSize() - 1) {
                            selectCell(currentRow + 1, 0);
                        } else {
                            selectCell(currentRow, currentIndex + 1);
                        }
                        event.preventDefault();
                        return;
                    }

                    if (KeyCodes.KEY_SHIFT == code)
                        return;

                    switchToInput();
                }
            }
        }, KeyDownEvent.getType());
    }

    // header that covers the span of the row index
    private void addLeadHeader(int row) {
        HTML cell = new HTML("&nbsp;");
        cell.setStyleName("leader_cell_column_header");
        header.setWidget(row, headerCol, cell);
        header.getFlexCellFormatter().setStyleName(row, headerCol, "leader_cell_column_header_td");
        headerCol += 1;
    }

    // tail header
    private void addTailHeader(int row) {
        HTML cell = new HTML("&nbsp;");
        cell.setStyleName("tail_cell_column_header");
        header.setWidget(row, headerCol, cell);
        header.getFlexCellFormatter().setStyleName(row, headerCol, "tail_cell_column_header_td");
        headerCol += 1;
    }

    @Override
    public void createHeaderCells(ServiceDelegate<PreferenceInfo> lockUnlockDelegate) {
        headerCol = 0;
        header.clear();
        addLeadHeader(0);
        SheetHeaderUtil.createHeaders(presenter.getAllHeaders(), headerCol, 0, header, lockUnlockDelegate);
        headerCol += (presenter.getFieldSize());
        addTailHeader(0);
    }

    public void setAutoUpdateDelegate(ServiceDelegate<BulkUploadAutoUpdate> delegate) {
        presenter.setAutoUpdateDelegate(delegate);
    }

    @Override
    public boolean clear() {
        if (!Window.confirm("This will clear all data. Continue?"))
            return false;

        for (int row = 0; row < sheetTable.getRowCount(); row += 1) {
            if (presenter.isEmptyRow(row))
                continue;

            for (int col = 0; col < presenter.getFieldSize(); col += 1) {
                Widget widget = sheetTable.getWidget(row, col);
                if (widget instanceof CellWidget) {
                    CellWidget cellWidget = (CellWidget) widget;
                    cellWidget.setValue("");
                }
            }
        }

        presenter.reset();
        return true;
    }

    @Override
    public int getSheetRowCount() {
        return sheetTable.getRowCount();
    }

    @Override
    public int getSheetColumnCount(int row) {
        return sheetTable.getCellCount(row);
    }

    public void highlightHeaders(int row, int col) {
        SheetHeaderUtil.highlightHeader(col, header);

        int count = colIndex.getRowCount();
        for (int i = 0; i < count; i += 1) {
            if (i == row)
                colIndex.getFlexCellFormatter().setStyleName(i, 0, "index_td_selected_cell");
            else
                colIndex.getFlexCellFormatter().setStyleName(i, 0, "index_td_cell");
        }
    }

    public boolean validate() {
        return presenter.validateCells();
    }

    @Override
    public void clearErrorCell(int row, int col) {
        Widget widget = sheetTable.getWidget(row, col);
        if (widget != null && widget instanceof CellWidget) {
            ((CellWidget) widget).clearError();
        }
    }

    @Override
    public void setErrorCell(int row, int col, String errMsg) {
        Widget widget = sheetTable.getWidget(row, col);
        if (widget != null && widget instanceof CellWidget) {
            ((CellWidget) widget).showError(errMsg);
        } else if (replaced != null && replaced.getIndex() == col && replaced.getRow() == row) {
            replaced.showError(errMsg);
        }
    }

    @Override
    public void scrollElementToView(int row, int col) {
        Widget widget = sheetTable.getWidget(row, col);
        if (widget == null)
            return;
        widget.getElement().scrollIntoView();
    }

    public void closeOpenCells() {
        if (!cellHasFocus)
            return;

        presenter.autoUpdate(currentIndex, currentRow);
        cellHasFocus = false;
    }

    /**
     * Replaces the cell with an input widget that is determined by the type of header
     */
    private void switchToInput() {
        if (cellHasFocus)
            return;

        inputIndex = currentIndex;
        inputRow = currentRow;

        Widget widget = sheetTable.getWidget(currentRow, currentIndex);
        int tabIndex = -1;
        if (widget instanceof CellWidget) {
            replaced = (CellWidget) widget;
            tabIndex = replaced.getTabIndex();
        }

        newCellSelection = presenter.setCellInputFocus(currentRow, currentIndex);
        if (newCellSelection == null)
            return;

        sheetTable.setWidget(currentRow, currentIndex, newCellSelection.getWidget(currentRow, true, tabIndex));
        // allow cell to set focus to whatever their input mechanism is.
        // e.g. if an input box, allow focus on that box
        newCellSelection.setFocus(currentRow);
        cellHasFocus = true;
    }

    private void dealWithUpArrowPress() {
        // exit for up arrow press in auto complete box
        CellColumnHeader currentHeader = presenter.getHeaderForIndex(currentIndex);
        if (currentHeader.getCell().hasMultiSuggestions() && cellHasFocus)
            return;

        selectCell(currentRow - 1, currentIndex);
    }

    private void dealWithDownArrowPress() {
        // exit for down arrow press in auto complete box
        CellColumnHeader currentHeader = presenter.getHeaderForIndex(currentIndex);
        if (currentHeader.getCell().hasMultiSuggestions() && cellHasFocus)
            return;

        selectCell(currentRow + 1, currentIndex);
    }

    private void dealWithLeftArrowPress() {
        if (cellHasFocus)
            return;

        if (currentIndex == 0 && currentRow > 0)
            selectCell(currentRow - 1, presenter.getFieldSize() - 1);
        else if (currentIndex > 0)
            selectCell(currentRow, currentIndex - 1);
    }

    private void dealWithRightArrowPress() {
        if (cellHasFocus)
            return;

        if (currentIndex >= presenter.getFieldSize() - 1)
            selectCell(currentRow + 1, 0);
        else
            selectCell(currentRow, currentIndex + 1);
    }

    /**
     * cell selection via click or arrow press
     *
     * @param newRow user selected row
     * @param newCol user selected column
     */
    private void selectCell(int newRow, int newCol) {
        if (currentIndex == newCol && currentRow == newRow)
            return;

        highlightHeaders(newRow, newCol);

        // handle previous selection
        SheetCell prevSelection = newCellSelection;
        if (prevSelection != null) {
            if (cellHasFocus && replaced != null) {
                // switch from input

                // this relies on the fact that on blur, individual sheet cells (or the header responsible for them)
                // set the data so the assumption is that at this point, if there is data, it is already set
                // and so just retrieved here and displayed in the sheet.
                SheetCellData data = prevSelection.getDataForRow(currentRow);
                if (data == null)
                    replaced.setValue("");
                else
                    replaced.setValue(data.getValue());

                // auto updating even for blank cells since data might be cleared
                presenter.autoUpdate(inputIndex, inputRow);

                replaced.hideCorner();
                sheetTable.setWidget(inputRow, inputIndex, replaced);

                // reset
                cellHasFocus = false;
                replaced = null;
                inputRow = inputIndex = -1;
            }

            if (currentIndex != -1 && currentRow != -1) {
                if (prevSelection.handlesSelection()) {
                    //cell has its own widget on select. this needs to be combined at some point to they all have
                    // their own
                    int tabIndex = (currentRow * presenter.getFieldSize()) + currentIndex + 1;
                    Widget widget = prevSelection.getWidget(currentRow, false, tabIndex);
                    sheetTable.setWidget(currentRow, currentIndex, widget);
                } else {
                    Widget cellWidget = sheetTable.getWidget(currentRow, currentIndex);
                    if (cellWidget instanceof CellWidget)
                        ((CellWidget) cellWidget).setFocus(false);
                }
            }
        }

        // handle current selection
        newCellSelection = presenter.getCellForIndex(newCol);
        Widget widget;

        // now deal with current selection
        if (newCellSelection.handlesSelection()) {
            widget = sheetTable.getWidget(newRow, newCol);
            int tabIndex = -1;

            if (widget instanceof CellWidget) {
                tabIndex = ((CellWidget) widget).getTabIndex();
            }
            widget = newCellSelection.getWidget(newRow, true, tabIndex);
            sheetTable.setWidget(newRow, newCol, widget);
            if (widget instanceof FocusPanel) {
                ((FocusPanel) widget).setFocus(true);
            }
        } else {
            widget = sheetTable.getWidget(newRow, newCol);
            if (widget instanceof CellWidget) {
                ((CellWidget) widget).setFocus(true);
            }
        }
        widget.getElement().scrollIntoView();

        // update current
        currentRow = newRow;
        currentIndex = newCol;
    }

    @Override
    public void setCellWidgetForCurrentRow(String value, int row, int col, int size) {
        Widget widget = null;
        int rowSize = sheetTable.getRowCount();
        if (row < rowSize) {
            int colSize = sheetTable.getCellCount(row);
            if (col < colSize) {
                widget = sheetTable.getWidget(row, col);
            }
        }

        if (widget == null || !(widget instanceof CellWidget)) {
            widget = new CellWidget(value, row, col, size);
            ((CellWidget) widget).addWidgetCallback(new SheetCallback());
        } else {
            ((CellWidget) widget).setValue(value);
        }

        sheetTable.setWidget(row, col, widget);
    }

    @Override
    public void removeCellForCurrentRow(int row, int col, int count) {
        sheetTable.removeCells(row, col, count);
    }

    public void setSampleSelection(EntryAddType addType, SampleSelectionWidget sampleSelectionWidget) {
        this.sampleSelectionWidget = sampleSelectionWidget;
        if (sampleSelectionWidget != null) {
            if (sampleSelectionRegistration != null)
                sampleSelectionRegistration.removeHandler();
            sampleSelectionRegistration = presenter.setSampleSelectionHandler(addType, this.sampleSelectionWidget
                    .getSelectionModel());
        }
    }

    @Override
    public SampleLocation getSampleSelectionLocation() {
        if (this.sampleSelectionWidget == null)
            return null;

        return this.sampleSelectionWidget.getCurrentLocation();
    }

    public void selectSample(EntryAddType type, String locationId) {
        presenter.selectSample(type, locationId);
    }

    public BulkUploadInfo setUpdateBulkUploadId(Long result) {
        return presenter.setUpdateBulkUploadId(result);
    }

    //
    // inner classes
    //
    protected class SheetCallback implements CellWidgetCallback {

        @Override
        public void onMouseDown(int row, int col) {
            dragging = true;
            dragRow = row;
            dragCol = col;
            Widget widget = sheetTable.getWidget(row, col);
            if (widget instanceof CellWidget) {
                CellWidget cellWidget = (CellWidget) widget;
                startText = cellWidget.getValue();
            }
        }

        @Override
        public void onMouseUp(int row, int col) {
            if (dragging) {
                dragging = false;

                for (int i = dragRow; i <= row; i += 1) {
                    CellWidget widget = (CellWidget) sheetTable.getWidget(i, dragCol);
                    widget.setValue(startText);
                    if (startText != null && !startText.isEmpty())
                        presenter.autoUpdate(dragCol, i);
                }

                dragRow = dragCol = -1;
                startText = null;
            }
        }

        @Override
        public void onMouseOver(int row, int col) {
            if (!dragging)
                return;

            CellWidget widget = (CellWidget) sheetTable.getWidget(row, dragCol);
            widget.addStyleName("cell_drag");
            if (dragCol > 0) {
                widget = (CellWidget) sheetTable.getWidget(row, dragCol - 1);
                widget.addStyleName("cell_drag");
            }
        }
    }

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