package org.jbei.ice.client.admin.bulkimport;

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
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.common.util.ImageUtil;

import java.util.ArrayList;

public class SavedDraftsMenu extends Composite {

    private final FlexTable table;
    private BulkImportMenuItem currentEditSelection;

    private int row;
    private final SingleSelectionModel<BulkImportMenuItem> selectionModel;

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

        selectionModel = new SingleSelectionModel<BulkImportMenuItem>();
    }

    public SingleSelectionModel<BulkImportMenuItem> getSelectionModel() {
        return this.selectionModel;
    }

    public void setSelection(long id) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (id == cell.getMenuItem().getId())
                cell.setSelected(true);
            else
                cell.setSelected(false);
        }
    }

    public void setMenuItems(ArrayList<BulkImportMenuItem> items, IDeleteMenuHandler handler) {
        if (items == null || items.isEmpty())
            return;

        for (BulkImportMenuItem item : items) {
            addMenuItem(item, handler);
        }
    }

    /**
     * checks if the user clicked within the menu contents
     * and not, for eg. the header
     *
     * @param event
     *            user click event
     * @return true if a response is required for user selection
     */
//    public boolean isValidClick(ClickEvent event) {
//        if (event == null)
//            return false;
//
//        Cell cell = this.table.getCellForEvent(event);
//        if (cell == null)
//            return false;
//
//        boolean isValid = (cell.getCellIndex() != 0 || cell.getRowIndex() != 0);
//        if (!isValid)
//            return isValid;
//
//        return isValid;
//    }

    /**
     * replaces current edit cell (in menu)
     * with new cell with folder
     * <p/>
     * new folder for cell
     */
    //    public void setMenuItem(BulkImportMenuItem item, IDeleteMenuHandler deleteHandler) {
    //
    //        final MenuCell cell = new MenuCell(item, deleteHandler);
    //        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
    //        table.setWidget(editRow, editIndex, cell); // TODO
    //    }
    public void addMenuItem(BulkImportMenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        row += 1;
        table.setWidget(row, 0, cell);
    }

    public void updateMenuItem(BulkImportMenuItem item) {
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

    public boolean removeMenuItem(BulkImportMenuItem item) {
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


    /**
     * sets the busy indicator where the folder counts are displayed
     * to indicate that some form of update is taking place
     */

//    public BulkImportMenuItem getCurrentEditSelection() {
//        return currentEditSelection;
//    }

    // inner class
    // TODO : this needs to go into a presenter;
    class DeleteCallBack extends Callback<BulkImportMenuItem> {

        @Override
        public void onSuccess(BulkImportMenuItem item) {
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
        private final BulkImportMenuItem item;
        private final String html;

        private Label count;
        private Label nameLabel;
        private Label dateLabel;
        private Label type;
        private final String folderId;
        private final Image delete;

        public MenuCell(final BulkImportMenuItem item, final IDeleteMenuHandler deleteHandler) {

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
                            currentEditSelection = getMenuItem();
                            deleteHandler.delete(item.getId(), new DeleteCallBack());
                        }
                    }
                });
            }

//            html = "<span class=\"collection_user_menu\">" + name
//                    + "</span><span class=\"menu_count\" id=\"" + folderId
//                    + "\"></span><br><span style=\"font-size: 10px; color: #999\">"
//                    + item.getDateTime() + " | " + item.getType() + "</span>";

            html = "<span class=\"collection_user_menu\" id=\"" + folderId + "_name\"></span>"
                    + "<span class=\"menu_count\" id=\"" + folderId + "\"></span> "
                    + "<div style=\"font-size: 10px; color: #999\"><span id=\"" + folderId + "_date\"></span>" +
                    "<span> | </span><span>" + item.getType() + "</span></div>";

            panel = new HTMLPanel(html);
            panel.setTitle(item.getEmail());

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
            count = new Label(formatNumber(item.getCount()));
        }

        public void updateDate(String dateTime) {

        }

        public BulkImportMenuItem getMenuItem() {
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
