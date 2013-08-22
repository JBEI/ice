package org.jbei.ice.client.entry.display.view;

import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.entry.display.EntryPresenter.MenuSelectionHandler;
import org.jbei.ice.client.entry.display.view.MenuItem.Menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Handles menu options for viewing an entry
 *
 * @author Hector Plahar
 */
public class EntryViewMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private int currentRowSelection;
    private final SingleSelectionModel<MenuItem> selectionModel;
    private final HashMap<Integer, MenuItem> itemHashMap;
    private HandlerRegistration selectionRegistration;

    public EntryViewMenu() {
        table = new FlexTable();
        initWidget(table);
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("entry_detail_submenu");
        selectionModel = new SingleSelectionModel<MenuItem>();
        itemHashMap = new HashMap<Integer, MenuItem>();

        int row = 0;
        currentRowSelection = row;
        for (Menu menu : Menu.values()) {
            String html = menu.toString();
            MenuItem item;

            if (menu != Menu.GENERAL) {
                html += "<span style=\"float: right; color: #999;\">0</span>";
                item = new MenuItem(menu, 0);
            } else {
                item = new MenuItem(menu, -1);
            }
            table.setHTML(row, 0, html);
            itemHashMap.put(row, item);
            row += 1;
        }

        // set general by default
        table.getCellFormatter().setStyleName(Menu.GENERAL.ordinal(), 0, "selected");

        table.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HTMLTable.Cell cell = table.getCellForEvent(event);
                if (cell == null)
                    return;

                table.getCellFormatter().removeStyleName(currentRowSelection, 0, "selected");
                currentRowSelection = cell.getRowIndex();
                selectionModel.setSelected(itemHashMap.get(currentRowSelection), true);
                table.getCellFormatter().setStyleName(currentRowSelection, 0, "selected");
            }
        });
    }

    public void switchToEditMode(boolean performSwitch) {
        if (performSwitch)
            table.getCellFormatter().setStyleName(Menu.GENERAL.ordinal(), 0, "selected");

        for (Menu menu : Menu.values()) {
            if (menu == Menu.GENERAL)
                continue;

            table.getRowFormatter().setVisible(menu.ordinal(), !performSwitch);
        }
    }

    public Menu getCurrentSelection() {
        MenuItem item = itemHashMap.get(currentRowSelection);
        if (item == null)
            return Menu.GENERAL;

        return item.getMenu();
    }

    public void setSelection(Menu menu) {
        int i = -1;
        for (Map.Entry<Integer, MenuItem> row : itemHashMap.entrySet()) {
            if (row.getValue().getMenu() == menu) {
                i = row.getKey();
                break;
            }
        }

        if (i == -1)
            return;

        table.getCellFormatter().removeStyleName(currentRowSelection, 0, "selected");
        currentRowSelection = i;
        table.getCellFormatter().setStyleName(currentRowSelection, 0, "selected");
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * Increments specified menu's count by 1
     *
     * @param menu menu whose count is to be incremented
     */
    public void incrementMenuCount(Menu menu) {
        if (menu == Menu.GENERAL)
            return;

        MenuItem item = itemHashMap.get(menu.ordinal());
        if (item == null)
            return;
        item.setCount(item.getCount() + 1);
        String html = menu.toString() + "<span style=\"float: right; color: #999;\">" + formatNumber(item.getCount())
                + "</span>";
        table.setHTML(menu.ordinal(), 0, html);
    }

    public void updateMenuCount(Menu menu, int count) {
        if (menu == Menu.GENERAL)
            return;

        MenuItem item = itemHashMap.get(menu.ordinal());
        if (item != null) {
            item.setCount(count);
        }
        String html = menu.toString() + "<span style=\"float: right; color: #999;\">" + formatNumber(count) + "</span>";
        table.setHTML(menu.ordinal(), 0, html);
    }

    private String formatNumber(long l) {
        if (l < 0)
            return "";

        NumberFormat format = NumberFormat.getFormat("##,###");
        return format.format(l);
    }

    public void setSelectionHandler(MenuSelectionHandler menuSelectionHandler) {
        if (selectionRegistration != null)
            selectionRegistration.removeHandler();
        selectionRegistration = selectionModel.addSelectionChangeHandler(menuSelectionHandler);
    }

    public void reset() {
        setSelection(Menu.GENERAL);
        for (Menu menu : Menu.values())
            updateMenuCount(menu, 0);
    }
}
