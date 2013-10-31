package org.jbei.ice.client.bulkupload.sheet;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.bulkupload.widget.HeaderLockWidget;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Column Header for the bulk import sheet
 *
 * @author Hector Plahar
 */
public class SheetHeaderUtil {

    public static void createHeaders(ArrayList<CellColumnHeader> headers, int headerCol, int row,
            FlexTable headerTable, ServiceDelegate<PreferenceInfo> preferenceInfoServiceDelegate) {
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

            if (header.isCanLock()) {
                html += ("<span id=\"header_lock\"></span> ");
            }

            html += (label);
            if (header.isRequired())
                html += (" <span class=\"required\">*</span>");
            HTMLPanel cell = new HTMLPanel(html);
            cell.setStyleName("cell_column_header");
            if (header.isCanLock() && preferenceInfoServiceDelegate != null) {
                cell.add(new HeaderLockWidget(header, preferenceInfoServiceDelegate).asWidget(), "header_lock");
            }
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