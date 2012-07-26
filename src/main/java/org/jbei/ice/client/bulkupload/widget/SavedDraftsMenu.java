package org.jbei.ice.client.bulkupload.widget;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.bulkupload.BulkUploadMenuItem;
import org.jbei.ice.client.bulkupload.IDeleteMenuHandler;
import org.jbei.ice.client.common.util.ImageUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Menu for bulk import drafts and pending imports
 *
 * @author Hector Plahar
 */
public class SavedDraftsMenu extends Composite {

    private final FlexTable table;
    private int row;
    private final SingleSelectionModel<BulkUploadMenuItem> selectionModel;

    public SavedDraftsMenu(String header) {
        table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        initWidget(table);

        HTMLPanel menuHeaderPanel = new HTMLPanel("<span>" + header + "</span>");
        table.setWidget(row, 0, menuHeaderPanel);
        table.getFlexCellFormatter().setStyleName(row, 0, "collections_menu_header");

        table.setHTML(row, 0, header);
        table.getFlexCellFormatter().setStyleName(row, 0, "collections_menu_header");

        selectionModel = new SingleSelectionModel<BulkUploadMenuItem>();
    }

    public SingleSelectionModel<BulkUploadMenuItem> getSelectionModel() {
        return this.selectionModel;
    }

    public void setMenuItems(ArrayList<BulkUploadMenuItem> items, IDeleteMenuHandler handler) {
        if (items == null || items.isEmpty())
            return;

        for (BulkUploadMenuItem item : items) {
            addMenuItem(item, handler);
        }
    }

    public void addMenuItem(BulkUploadMenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        row += 1;
        table.setWidget(row, 0, cell);
    }

    public void updateMenuItem(BulkUploadMenuItem item) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (cell.getMenuItem().getId() != item.getId())
                continue;

            cell.updateCount(item.getCount());
            cell.updateDate(item.getDateTime());
        }
    }

    public boolean removeMenuItem(BulkUploadMenuItem item) {
        if (item == null)
            return false;

        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (cell.getMenuItem().getId() != item.getId())
                continue;

            table.remove(cell);
            row -= 1;
            return true;
        }

        return false;
    }

    // inner class
    // TODO : this needs to go into a presenter;
    class DeleteCallBack extends Callback<BulkUploadMenuItem> {

        @Override
        public void onSuccess(BulkUploadMenuItem item) {
            removeMenuItem(item);
        }

        @Override
        public void onFailure() {
            Window.alert("There was an error deleting. Please check the logs for details");
        }
    }

    class MenuCell extends Composite implements HasMouseOverHandlers, HasMouseOutHandlers,
                                                HasClickHandlers {

        private final HTMLPanel panel;
        private final BulkUploadMenuItem item;
        private final String html;

        private Label count;
        private Label nameLabel;
        private Label dateLabel;
        private final String folderId;
        private final Image delete;

        public MenuCell(final BulkUploadMenuItem item, final IDeleteMenuHandler deleteHandler) {

            this.item = item;
            delete = ImageUtil.getDeleteIcon();

            folderId = "menu_right_holder_" + item.getId();

            if (deleteHandler != null) {
                delete.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation();
                        Cell cell = table.getCellForEvent(event);
                        if (cell == null)
                            return;

                        if (Window.confirm("This action cannot be undone. Continue?")) {
//                            currentEditSelection = getMenuItem();
                            deleteHandler.delete(item.getId(), new DeleteCallBack());
                        }
                    }
                });
            }

            html = "<span class=\"collection_user_menu\" id=\"" + folderId + "_name\"></span>"
                    + "<span class=\"menu_count\" id=\"" + folderId + "\"></span> "
                    + "<div style=\"font-size: 10px; color: #999\"><span id=\"" + folderId + "_date\"></span>" +
                    "<span> | </span><span>" + item.getType() + "</span></div>";

            panel = new HTMLPanel(html);
            panel.setTitle(item.getName());

            count = new Label(formatNumber(item.getCount()));
            nameLabel = new Label(generateName());
            dateLabel = new Label(item.getDateTime());
            dateLabel.setStyleName("display-inline");

            panel.add(count, folderId);
            panel.add(nameLabel, folderId + "_name");
            panel.add(dateLabel, folderId + "_date");

            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);

            this.addMouseOverHandler(new MouseOverHandler() {

                @Override
                public void onMouseOver(MouseOverEvent event) {
//                    panel.clear();
                    panel.remove(count);
                    panel.add(delete, folderId);

                }
            });

            this.addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
//                    panel.clear();
                    panel.remove(delete);
                    panel.add(count, folderId);
                }
            });
        }

        private String generateName() {
            String name = item.getName();
            if (name.length() > 22)
                name = (name.substring(0, 22) + "...");
            return name;
        }

        public void setSelected(boolean selected) {
            if (selected)
                this.addStyleName("collection_user_menu_row_selected");
            else
                this.removeStyleName("collection_user_menu_row_selected");
        }

        public void updateCount(long newCount) {
            item.setCount(newCount);
            count.setText(formatNumber(item.getCount()));
        }

        public void updateDate(String dateTime) {

        }

        public BulkUploadMenuItem getMenuItem() {
            return this.item;
        }

        @Override
        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        @Override
        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }

        private String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }
    }
}
