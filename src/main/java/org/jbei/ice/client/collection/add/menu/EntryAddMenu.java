package org.jbei.ice.client.collection.add.menu;

import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class EntryAddMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private EntryAddType currentSelected;

    public EntryAddMenu() {
        table = new FlexTable();
        initWidget(table);

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        table.setHTML(0, 0, "SELECT NEW ENTRY TYPE");
        table.getFlexCellFormatter().setStyleName(0, 0, "collections_menu_header");

        int row = 1;
        // set menu options
        for (EntryAddType type : EntryAddType.values()) {
            final MenuCell cell = new MenuCell(type);
            cell.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    currentSelected = cell.getAddType();
                }
            });
            table.setWidget(row, 0, cell);
            row += 1;
        }
    }

    public EntryAddType getCurrentSelection() {
        return this.currentSelected;
    }

    // TODO : when this is combined with other menus 
    // TODO : have a common header that is not under the umbrella of the menu class and therefore is not a part of HASA clickhandler
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
        private final EntryAddType addType;

        public MenuCell(EntryAddType addType) {

            this.addType = addType;

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + addType.toString() + "</span>";
            panel = new HTMLPanel(html);
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);
        }

        public EntryAddType getAddType() {
            return this.addType;
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }

}
