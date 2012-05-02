package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Label;

public class SavedDraftsMenu extends Composite implements HasClickHandlers {

    private final FlexTable table;
    private BulkImportDraftInfo currentSelected;
    private int row = 1;

    public SavedDraftsMenu() {
        table = new FlexTable();
        initWidget(table);

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        table.setHTML(0, 0, "SAVED DRAFTS");
        table.getFlexCellFormatter().setStyleName(0, 0, "collections_menu_header");
    }

    public void setData(ArrayList<BulkImportDraftInfo> data) { // TODO : use some sort of delegate instead of passing the model since this is part of the view

        // set menu options
        for (BulkImportDraftInfo info : data) {
            final MenuCell cell = new MenuCell(info);
            cell.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    currentSelected = cell.getDraftInfo();
                }
            });
            table.setWidget(row, 0, cell);
            row += 1;
        }
    }

    public void addMenuData(BulkImportDraftInfo info) {
        final MenuCell cell = new MenuCell(info);
        cell.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                currentSelected = cell.getDraftInfo();
            }
        });
        table.setWidget(row, 0, cell);
        row += 1;
    }

    public BulkImportDraftInfo getCurrentSelection() {
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
        private final BulkImportDraftInfo draftInfo;
        private Label count;
        private String countElementId;

        public MenuCell(BulkImportDraftInfo draftInfo) {

            this.draftInfo = draftInfo;
            countElementId = "draft_count_" + draftInfo.getId();

            String html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + draftInfo.getName() + "</span><span class=\"menu_count\" id=\""
                    + countElementId + "\"></span>";

            panel = new HTMLPanel(html);
            panel.setStyleName("collection_user_menu_row");
            count = new Label(formatNumber(draftInfo.getCount()));
            panel.add(count, countElementId);

            initWidget(panel);
        }

        private String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }

        public BulkImportDraftInfo getDraftInfo() {
            return this.draftInfo;
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }
}
