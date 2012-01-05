package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class Sheet extends Composite {

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

    public Sheet() {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        initWidget(layout);

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
        wrapper.setWidth((Window.getClientWidth() - 300) + "px");
        wrapper.setHeight((Window.getClientHeight() - 320) + "px");

        colIndex = new FlexTable();
        colIndex.setCellPadding(0);
        colIndex.setCellSpacing(0);
        rowIndexWrapper = new ScrollPanel(colIndex);
        rowIndexWrapper.setHeight((Window.getClientHeight() - 320 - 14) + "px");

        addPanelHandlers();

        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {

                int wrapperWidth = (event.getWidth() - 300);
                if (wrapperWidth >= 0)
                    wrapper.setWidth(wrapperWidth + "px");

                int wrapperHeight = (event.getHeight() - 350);
                if (wrapperHeight >= 0)
                    wrapper.setHeight(wrapperHeight + "px");

                int rowIndexHeight = (event.getHeight() - 350 - 15);
                if (rowIndexHeight >= 0)
                    rowIndexWrapper.setHeight(rowIndexHeight + "px");
            }
        });

        currentRow = inputRow = -1;
        currentIndex = inputIndex = -1;

        input = new TextBox();
        input.setStyleName("cell_input");

        sheetTable.addDoubleClickHandler(new CellDoubleClick());
        sheetTable.addClickHandler(new CellClick());
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
    }

    protected abstract Widget createHeader();

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
        String text = sheetTable.getText(currentRow, currentIndex);
        input.setText(text);
        sheetTable.setWidget(currentRow, currentIndex, input);
        input.setFocus(true);
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
        for (int i = 0; i < 18; i += 1) {
            FocusPanel cell = new FocusPanel();
            cell.setStyleName("cell");
            sheetTable.setWidget(row, i, cell);
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