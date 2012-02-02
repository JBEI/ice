package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;
import java.util.TreeSet;

import org.jbei.ice.client.bulkimport.sheet.StrainHeaders.Header;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class StrainSheet extends Sheet {

    private final static int FIELDS = 18; // number of fields in a strain

    private int headerCol;
    private final FlexTable header;
    private final ScrollPanel headerWrapper;
    private SheetHeader headers;

    public StrainSheet() {
        super();
        headerCol = 0;
        header = new FlexTable();
        header.setCellPadding(0);
        header.setCellSpacing(0);
        header.setWidth("100%");
        header.addStyleName("sheet_header_table");

        headerWrapper = new ScrollPanel(header);

        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(headerWrapper.getElement(), "overflowX", "hidden");

        DOM.setStyleAttribute(rowIndexWrapper.getElement(), "overflowY", "hidden");
        DOM.setStyleAttribute(rowIndexWrapper.getElement(), "overflowX", "hidden");

        headerWrapper.setWidth((Window.getClientWidth() - 260 - 15) + "px"); // TODO : the 15px accounts for the scroll bar. Not sure yet how to get the scrollbar width

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

            // TODO Performance issue here
            this.addRow();

            // index col
            HTML indexCell = new HTML(i + "");
            colIndex.setWidget(row, 0, indexCell);
            indexCell.setStyleName("index_cell");
            colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");

            count -= 1;
            i += 1;
        }

        addScrollHandlers();
        addResizeHandler();
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

    @Override
    protected Widget createHeader() {
        addLeadHeader();
        headers = new StrainHeaders(headerCol, row, header);
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

    protected Widget createStatusBox() {
        TreeSet<String> data = new TreeSet<String>();
        for (StatusType type : StatusType.values()) {
            data.add(type.getDisplayName());
        }

        return createSuggestBox(data);
    }

    private Widget createSuggestBox(TreeSet<String> data) {
        return new HTML("&nbsp;");
        // TODO : instead of a multi-suggest, show the options on focus
        //        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        //        oracle.addAll(data);
        //        SuggestBox box = new SuggestBox(oracle, new MultipleTextBox());
        //        return box;
    }

    @Override
    public void addRow() {
        for (int i = 0; i < FIELDS; i += 1) {
            Widget widget = new HTML("");
            widget.setStyleName("cell");

            sheetTable.setWidget(row, i, widget);
            sheetTable.getFlexCellFormatter().setStyleName(row, i, "td_cell");
        }
        row += 1;
    }

    @Override
    public void reset() {
        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            for (int j = 0; j < FIELDS; j += 1) {
                HasText widget = (HasText) sheetTable.getWidget(row, i);
                widget.setText("");
                ((Widget) widget).setStyleName("cell");
            }
        }
    }

    @Override
    public boolean validate() {
        for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
            if (isEmptyRow(i))
                continue;

            for (Header header : Header.values()) {
                Label widget = (Label) sheetTable.getWidget(i, header.ordinal());
                boolean isEmpty = widget.getText().trim().isEmpty();
                if (isEmpty && header.isRequired()) {
                    widget.setStyleName("cell_error");
                    widget.setTitle("Required field");
                } else {
                    widget.setStyleName("cell");
                    widget.setTitle("");
                }
            }
        }

        return false;
    }

    @Override
    public ArrayList<EntryInfo> getInfos() {
        ArrayList<EntryInfo> infos = new ArrayList<EntryInfo>();
        return infos;
    }

    // TODO : use a bit map or bit arrays to track user entered values for more efficient lookup
    private boolean isEmptyRow(int row) {
        int cellCount = sheetTable.getCellCount(row); // TODO : this should equal FIELDS value or else there is a big problem

        for (int i = 0; i < cellCount; i += 1) {
            HasText widget = (HasText) sheetTable.getWidget(row, i);
            if (!widget.getText().isEmpty())
                return false;
        }

        return true;
    }
}
