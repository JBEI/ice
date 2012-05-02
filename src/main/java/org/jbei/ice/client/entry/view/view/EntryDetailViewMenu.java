package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import org.jbei.ice.client.entry.view.view.MenuItem.Menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EntryDetailViewMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private MenuItem currentSelected;
    private Label permissionLink;

    public EntryDetailViewMenu() {
        table = new FlexTable();
        initWidget(table);
        permissionLink = new Label("Permissions");

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("entry_view_left_menu");
    }

    public MenuItem getCurrentSelection() {
        return this.currentSelected;
    }

    public void setSelection(Menu menu) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (menu == cell.getMenuItem().getMenu())
                cell.setSelected(true);
            else
                cell.setSelected(false);
        }
    }

    public Label getPermissionLink() {
        return this.permissionLink;
    }

    void setMenuItems(ArrayList<MenuItem> items) {

        int row = 0;

        for (MenuItem item : items) {
            final MenuCell cell = new MenuCell(item);
            cell.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    currentSelected = cell.getMenuItem();
                }
            });
            table.setWidget(row, 0, cell);
            row += 1;
        }
    }

    private String formatNumber(long l) {
        if (l < 0)
            return "";

        NumberFormat format = NumberFormat.getFormat("##,###");
        return format.format(l);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    // inner class
    private class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final MenuItem item;

        public MenuCell(MenuItem item) {

            this.item = item;

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + item.getMenu().toString() + "</span><span class=\"menu_count\">"
                    + formatNumber(item.getCount()) + "</span>";
            panel = new HTMLPanel(html);
            panel.setStyleName("entry_detail_view_row");
            initWidget(panel);
        }

        public void setSelected(boolean selected) {
            if (selected)
                this.addStyleName("entry_detail_view_row_selected");
            else
                this.removeStyleName("entry_detail_view_row_selected");
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        private MenuItem getMenuItem() {
            return this.item;
        }
    }
}
