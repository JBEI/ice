package org.jbei.ice.client.bulkupload.sheet;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * Column Header for the bulk import sheet
 *
 * @author Hector Plahar
 */
public class SheetHeader {

    public static void createHeaders(ArrayList<CellColumnHeader> headers, int headerCol, int row,
            FlexTable headerTable) {
        for (CellColumnHeader header : headers) {

            String label = header.toString();
            String top = label.length() > 15 ? "-2px" : "-8px";
            String html = "";
            if (header.getDescription() != null) {
                html = "<div title=\"" + header.getDescription() + "\" style=\"position: relative; top: " + top
                        + "; left: -3px; float: right; font-size: 11px; "
                        + "border-radius: 1em 1em 1em 1em; color: #333; padding: 0 5px; "
                        + "font-weight: bold; background-color: #f8f8f0; cursor: pointer;\">?</div>";
            }
            html += label;
            if (header.isRequired())
                html += (" <span class=\"required\">*</span>");
            HTML cell = new HTML(html);
            cell.setStyleName("cell_column_header");
            headerTable.setWidget(row, headerCol, cell);
            headerTable.getFlexCellFormatter().setStyleName(row, headerCol, "cell_column_header_td");
            headerCol += 1;
        }
    }

    public static void highlightHeader(int col, FlexTable headerTable) {

        int count = headerTable.getCellCount(0);
        // excludes lead and tail headers which have indexes of '0' of 'count-1'
        for (int i = 1; i < count - 1; i += 1) {

            headerTable.getFlexCellFormatter().setStyleName(0, i, "cell_column_header_td");
            if ((col + 1) == i) {
                headerTable.getFlexCellFormatter().setStyleName(0, i, "cell_column_header_selected_td");
            }
        }
    }
}