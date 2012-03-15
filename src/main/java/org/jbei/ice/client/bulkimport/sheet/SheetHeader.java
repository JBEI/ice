package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class SheetHeader {

    int headerCol;
    final int row;
    final FlexTable headerTable;

    public SheetHeader(Header[] headers, int col, int row, FlexTable headerTable) {
        this.headerCol = col;
        this.headerTable = headerTable;
        this.row = row;

        for (Header header : headers)
            this.addHeader(header);
    }

    protected void addHeader(Header h) {
        String html = h.toString();
        if (h.isRequired())
            html += (" <span class=\"required\">*</span>");
        HTML cell = new HTML(html);
        cell.setStyleName("cell_column_header");
        headerTable.setWidget(row, headerCol, cell);
        headerTable.getFlexCellFormatter().setStyleName(row, headerCol, "cell_column_header_td");
        headerCol += 1;
    }
}