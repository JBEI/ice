package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import org.jbei.ice.client.entry.view.EntryPresenter.MenuSelectionHandler;
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
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;

public class EntryDetailViewMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private MenuItem currentSelected;
    private Label permissionLink;
    private final SingleSelectionModel<MenuItem> selectionModel;

    public EntryDetailViewMenu() {
        table = new FlexTable();
        permissionLink = new Label("Permissions");
        initWidget(table);
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("entry_view_left_menu");
        selectionModel = new SingleSelectionModel<MenuItem>(new ProvidesKey<MenuItem>() {

            @Override
            public Object getKey(MenuItem item) {
                return item.getMenu();
            }
        });
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
            if (menu == cell.getMenu())
                cell.setSelected(true);
            else
                cell.setSelected(false);
        }
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    public Label getPermissionLink() {
        return this.permissionLink;
    }

    public void updateMenuCount(Menu menu, int count) {

        switch (menu) {

            case GENERAL:
                return;

            // these are the only two that can be updated
            case SEQ_ANALYSIS:
                Widget widget = table.getWidget(1, 0);
                if (!(widget instanceof MenuCell))
                    return;

                MenuCell seqCell = ((MenuCell) widget);
                if (seqCell.getMenu() != Menu.SEQ_ANALYSIS)
                    return;

                seqCell.updateCount(count);
                table.setWidget(1, 0, seqCell);
                return;

            case SAMPLES:
                widget = table.getWidget(2, 0);
                if (!(widget instanceof MenuCell))
                    return;

                MenuCell sampleCell = ((MenuCell) widget);
                if (sampleCell.getMenu() != Menu.SAMPLES)
                    return;

                sampleCell.updateCount(count);
                table.setWidget(2, 0, sampleCell);
                return;

        }
    }

    void setMenuItems(ArrayList<MenuItem> items) {

        int row = 0;

        for (MenuItem item : items) {
            final MenuCell cell = new MenuCell(item);
            cell.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    currentSelected = cell.getMenuItem();
                    selectionModel.setSelected(currentSelected, true);
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

    // inner class
    private class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final MenuItem item;
        private final Label countLabel;

        public MenuCell(MenuItem item) {

            this.item = item;
            this.countLabel = new Label();
            this.countLabel.setStyleName("display-inline");

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + item.getMenu().toString()
                    + "</span><span class=\"menu_count\" id=\"collection_user_entry_count\">"
                    + "</span>";
            this.countLabel.setText(formatNumber(item.getCount()));
            panel = new HTMLPanel(html);
            panel.add(countLabel, "collection_user_entry_count");
            panel.setStyleName("entry_detail_view_row");
            initWidget(panel);
        }

        public Menu getMenu() {
            return item.getMenu();
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

        public void updateCount(int count) {
            this.countLabel.setText(formatNumber(count));
            this.item.setCount(count);
        }

        public MenuItem getMenuItem() {
            return this.item;
        }
    }

    public void addSelectionChangeHandler(MenuSelectionHandler menuSelectionHandler) {
        selectionModel.addSelectionChangeHandler(menuSelectionHandler);
    }
}
