package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.client.bulkimport.sheet.StrainHeaders.Header;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class SheetHeader {

    int headerCol;
    final int row;
    final FlexTable headerTable;

    public SheetHeader(int col, int row, FlexTable headerTable) {
        this.headerCol = col;
        this.headerTable = headerTable;
        this.row = row;

        for (Header header : Header.values())
            this.addHeader(header);
    }

    protected void addHeader(Header h) {
        HTML cell = new HTML(h.toString());
        cell.addStyleName("cell");
        headerTable.setWidget(row, headerCol, cell);
        headerTable.getFlexCellFormatter().setStyleName(row, headerCol, "cell_column_header");
        headerCol += 1;
    }

    enum IHeader {

    }

}