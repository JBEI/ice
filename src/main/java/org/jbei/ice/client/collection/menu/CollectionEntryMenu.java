package org.jbei.ice.client.collection.menu;

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

public class CollectionEntryMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private MenuItem currentSelected;
    private MenuCell previousSelected;

    public CollectionEntryMenu() {
        table = new FlexTable();
        initWidget(table);

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        table.setHTML(0, 0, "COLLECTIONS");
        table.getFlexCellFormatter().setStyleName(0, 0, "collections_menu_header");
    }

    public MenuItem getCurrentSelection() {
        return this.currentSelected;
    }

    public void setMenuItems(ArrayList<MenuItem> items) {
        if (items == null || items.isEmpty())
            return;

        int row = 1;

        for (MenuItem item : items) {
            final MenuCell cell = new MenuCell(item);
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
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        return addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!isValidClick(event))
                    return;

                handler.onClick(event);
            }
        }, ClickEvent.getType());
    }

    // inner class
    class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final MenuItem item;

        public MenuCell(MenuItem item) {

            this.item = item;

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + item.getName() + "</span><span class=\"menu_count\">"
                    + formatNumber(item.getCount()) + "</span>";
            panel = new HTMLPanel(html);
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);

            this.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (previousSelected != null)
                        previousSelected.removeStyleName("collection_user_menu_row_selected");

                    currentSelected = getItem();
                    MenuCell.this.addStyleName("collection_user_menu_row_selected");
                    previousSelected = MenuCell.this;
                }
            });
        }

        public MenuItem getItem() {
            return this.item;
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }
}
