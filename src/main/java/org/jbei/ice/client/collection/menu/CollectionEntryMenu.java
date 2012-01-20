package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;

import org.jbei.ice.shared.FolderDetails;

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
    private long currentSelected;

    public CollectionEntryMenu() {
        table = new FlexTable();
        initWidget(table);

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        table.setHTML(0, 0, "COLLECTIONS");
        table.getFlexCellFormatter().setStyleName(0, 0, "collections_menu_header");
    }

    public long getCurrentSelection() {
        return this.currentSelected;
    }

    public void setFolderDetails(ArrayList<FolderDetails> folders) {
        if (folders == null || folders.isEmpty())
            return;

        int row = 1;

        for (FolderDetails folder : folders) {
            final MenuCell cell = new MenuCell(folder);
            cell.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    currentSelected = cell.getFolderId();
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
        private final FolderDetails folder;

        public MenuCell(FolderDetails folder) {

            this.folder = folder;

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + folder.getName() + "</span><span class=\"menu_count\">"
                    + formatNumber(folder.getCount()) + "</span>";
            panel = new HTMLPanel(html);
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);
        }

        public long getFolderId() {
            return this.folder.getId();
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }
}
