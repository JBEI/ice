package org.jbei.ice.client.profile;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class ProfileViewMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private CellEntry currentSelected;

    public ProfileViewMenu() {
        table = new FlexTable();
        initWidget(table);

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        table.setHTML(0, 0, "USER PROFILE");
        table.getFlexCellFormatter().setStyleName(0, 0, "collections_menu_header");
    }

    public CellEntry getSelection() {
        return this.currentSelected;
    }

    public void setRowData(ArrayList<CellEntry> content) {

        int row = 1;
        for (CellEntry entry : content) {
            final MenuCell cell = new MenuCell(entry);
            cell.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    currentSelected = cell.getCell();
                }
            });
            table.setWidget(row, 0, cell);
            row += 1;
        }
    }

    private String formatNumber(long l) {
        NumberFormat format = NumberFormat.getFormat("##,###");
        return format.format(l);
    }

    public boolean isValidClick(ClickEvent event) {
        if (event == null)
            return false;

        Cell cell = this.table.getCellForEvent(event);
        return (cell.getCellIndex() != 0 || cell.getRowIndex() != 0);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    // inner class
    private class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final CellEntry cell;

        public MenuCell(CellEntry cell) {

            this.cell = cell;

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + cell.getType().getDisplay() + "</span>";
            if (cell.getCount() >= 0)
                html += "<span class=\"menu_count\">" + formatNumber(cell.getCount()) + "</span>";
            panel = new HTMLPanel(html);
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);
        }

        public CellEntry getCell() {
            return this.cell;
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }
}
