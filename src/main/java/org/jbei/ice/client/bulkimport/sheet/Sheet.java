package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.jbei.ice.client.bulkimport.SheetPresenter;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;

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
    private final TextBox input;
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

    public Sheet(EntryAddType type) {

        this.type = type;
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

        createHeader();

        // get header
        layout.setWidget(0, 0, headerWrapper);
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.setWidget(1, 0, rowIndexWrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(1, 1, wrapper);
        layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);

        // add rows
        int count = 50;
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

    protected Widget createHeader() {
        addLeadHeader();

        Header[] headers = ImportTypeHeaders.getHeadersForType(type);
        new SheetHeader(headers, headerCol, row, header);
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
    public HashMap<Integer, String[]> getCellData() {
        HashMap<Integer, String[]> cellData = new HashMap<Integer, String[]>();

        int headerLength = ImportTypeHeaders.getHeadersForType(type).length;

        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            String[] row = cellData.get(i);
            if (row == null) {
                row = new String[headerLength];

                cellData.put(i, row);
            }

            for (Header header : ImportTypeHeaders.getHeadersForType(type)) {
                HasText widget = (HasText) sheetTable.getWidget(i, header.ordinal());
                row[header.ordinal()] = widget.getText();
            }
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
            for (Header header : ImportTypeHeaders.getHeadersForType(type)) {
                Widget widget = sheetTable.getWidget(i, header.ordinal());
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
            }
        }

        return validates;
    }

    // put textinput in cell
    private void switchToInput() {

        Widget widget = sheetTable.getWidget(currentRow, currentIndex);
        if (!(widget instanceof Label))
            return;

        if (lastReplaced != null) {
            String lastText = input.getText();
            sheetTable.setWidget(inputRow, inputIndex, lastReplaced);
            if (lastText != null && !lastText.isEmpty()) {
                Label replaced = lastReplaced;
                replaced.setText(lastText);
            }
        }

        // cache 
        lastReplaced = (Label) sheetTable.getWidget(currentRow, currentIndex);
        lastReplaced.removeStyleName("cell_selected");
        inputIndex = currentIndex;
        inputRow = currentRow;

        // replace
        Header currentHeader = ImportTypeHeaders.getHeadersForType(this.type)[currentIndex];
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
                SuggestBox box = new SuggestBox(oracle, textBox);
                box.setStyleName("cell_input");
                box.setWidth("129px");
                box.setText(text);
                sheetTable.setWidget(currentRow, currentIndex, box);
                textBox.setFocus(true);
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

        selectCell(currentRow, currentIndex, currentRow - 1, currentIndex);
    }

    private void dealWithDownArrowPress() {
        if (currentRow == -1 && currentIndex == -1)
            return;

        if (!isRowInBounds(currentRow + 1))
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

    private void selectCell(int row, int col, int newRow, int newCol) {
        if (lastReplaced != null) {
            String inputText = input.getText();
            input.setText("");
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
        int headerFields = ImportTypeHeaders.getHeadersForType(this.type).length;

        for (int i = 0; i < headerFields; i += 1) {
            Widget widget = new HTML("");
            widget.setStyleName("cell");

            sheetTable.setWidget(row, i, widget);
            sheetTable.getFlexCellFormatter().setStyleName(row, i, "td_cell");
        }
        row += 1;
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

            // clear previously selected cell, if any
            if (currentRow >= 0 && currentIndex >= 0) {
                sheetTable.getWidget(currentRow, currentIndex).removeStyleName("cell_selected");
            }

            // reset and remove input
            if (lastReplaced != null) {
                String inputText = input.getText();
                input.setText("");
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