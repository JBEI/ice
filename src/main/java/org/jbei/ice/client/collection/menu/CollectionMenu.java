package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Left bar menu for showing user collections. Also adds widgets such as
 * an icon for adding a new user collection and edit/delete
 * 
 * @author Hector Plahar
 */
public class CollectionMenu extends Composite {

    private final FlexTable table;
    private MenuItem currentEditSelection;

    private int row;
    private final TextBox editCollectionNameBox;
    private int editRow = -1;
    private int editIndex = -1;
    private final SingleSelectionModel<MenuItem> selectionModel;

    // quick add
    private TextBox quickAddBox;
    private Image quickAddButton;

    public CollectionMenu(boolean addQuickEdit, String header) {
        table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");
        initWidget(table);

        // quick edit
        editCollectionNameBox = new TextBox();
        editCollectionNameBox.setStyleName("input_box");
        editCollectionNameBox.setWidth("99%");
        editCollectionNameBox.setVisible(false);

        if (addQuickEdit) {
            // quick add widgets
            quickAddBox = new TextBox();
            quickAddBox.setStyleName("input_box");
            quickAddBox.setWidth("99%");
            quickAddButton = ImageUtil.getPlusIcon();
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.addFocusHandler(new FocusHandler() {

                @Override
                public void onFocus(FocusEvent event) {
                    quickAddBox.setText("");
                }
            });

            quickAddButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    switchButton();
                }
            });

            HTMLPanel menuHeaderPanel = new HTMLPanel("<span>" + header
                    + "</span><span style=\"float: right\" id=\"quick_add\"></span>");
            menuHeaderPanel.add(quickAddButton, "quick_add");
            table.setWidget(row, 0, menuHeaderPanel);
            table.getFlexCellFormatter().setStyleName(row, 0, "collections_menu_header");

            row += 1;
            table.setWidget(row, 0, quickAddBox);

        } else {
            table.setHTML(row, 0, header);
            table.getFlexCellFormatter().setStyleName(row, 0, "collections_menu_header");
        }

        selectionModel = new SingleSelectionModel<MenuItem>();
    }

    // todo : move to model/presenter/handler
    protected boolean validate() {
        if (quickAddBox != null && quickAddBox.getText().trim().isEmpty()) {
            quickAddBox.setStyleName("entry_input_error");
            return false;
        }
        return true;
    }

    public SingleSelectionModel<MenuItem> getSelectionModel() {
        return this.selectionModel;
    }

    public String getQuickEditText() {
        return this.editCollectionNameBox.getText();
    }

    public boolean isQuickEditVisible() {
        return this.editCollectionNameBox.isVisible();
    }

    public void addQuickEditBlurHandler(BlurHandler handler) {
        this.editCollectionNameBox.addBlurHandler(handler);
    }

    public void addQuickEditKeyDownHandler(final KeyDownHandler handler) {
        this.editCollectionNameBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() != KeyCodes.KEY_ENTER)
                    return;
                handler.onKeyDown(event);
            }
        });
    }

    public void showFolderCount(FolderDetails details) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (details.getId() == cell.getMenuItem().getId()) {
                table.setWidget(i, 0, cell);
                cell.showFolderCount();
            }
        }
    }

    public void addQuickAddKeyPressHandler(final KeyPressHandler handler) {
        if (quickAddBox == null)
            return;

        quickAddBox.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                    return;

                if (!validate())
                    return;

                quickAddBox.setVisible(false);
                handler.onKeyPress(event);
            }
        });
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

    public void setMenuItems(ArrayList<MenuItem> items, IDeleteMenuHandler handler) {
        if (items == null || items.isEmpty())
            return;

        for (MenuItem item : items) {
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
    public boolean isValidClick(ClickEvent event) {
        if (event == null)
            return false;

        Cell cell = this.table.getCellForEvent(event);
        if (cell == null)
            return false;

        boolean isValid = (cell.getCellIndex() != 0 || cell.getRowIndex() != 0);
        if (!isValid)
            return isValid;

        if (quickAddBox != null && quickAddBox.isVisible())
            isValid = (cell.getRowIndex() != 1);

        return isValid;
    }

    /**
     * replaces current edit cell (in menu)
     * with new cell with folder
     * 
     * @param folder
     *            new folder for cell
     */
    public void setMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if ((this.editIndex == -1 && this.editRow == -1) || (item == null))
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        table.setWidget(editRow, editIndex, cell);
        this.editCollectionNameBox.setVisible(false);
    }

    public void addMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        row += 1;
        table.setWidget(row, 0, cell);
    }

    public boolean removeMenuItem(MenuItem item) {
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

    // currently this is being used for deleted cells only
    public void updateMenuItem(long id, MenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof DeletedCell))
                continue;

            DeletedCell cell = (DeletedCell) w;
            if (cell.getMenuItem().getId() != id)
                continue;

            final MenuCell newCell = new MenuCell(item, deleteHandler);
            newCell.addClickHandler(new CellSelectionHandler(selectionModel, newCell));
            table.setWidget(i, 0, newCell);
            break;
        }
    }

    /**
     * sets the busy indicator where the folder counts are displayed
     * to indicate that some form of update is taking place
     * 
     * @param folders
     */
    public void setBusyIndicator(Set<Long> ids) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (ids.contains(cell.getMenuItem().getId()))
                cell.showBusyIndicator();
        }
    }

    public void updateCounts(ArrayList<MenuItem> items) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;

            for (MenuItem item : items) {
                if (item.getId() == cell.getMenuItem().getId()) {
                    cell.updateCount(item.getCount());
                    cell.showFolderCount();
                    break;
                }
            }
        }
    }

    public Widget getQuickAddButton() {
        return this.quickAddButton;
    }

    public TextBox getQuickAddBox() {
        return this.quickAddBox;
    }

    public MenuItem getCurrentEditSelection() {
        return currentEditSelection;
    }

    public void switchButton() {
        if (quickAddBox == null)
            return;

        if (quickAddBox.isVisible()) {
            quickAddButton.setUrl(ImageUtil.getPlusIcon().getUrl());
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.setVisible(false);
            quickAddBox.setStyleName("input_box");
        } else {
            quickAddButton.setUrl(ImageUtil.getMinusIcon().getUrl());
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.setText("");
            quickAddBox.setVisible(true);
            quickAddBox.setFocus(true);
        }
    }

    public void hideQuickText() {
        if (quickAddBox == null)
            return;

        quickAddButton.setUrl(ImageUtil.getPlusIcon().getUrl());
        quickAddButton.setStyleName("collection_quick_add_image");
        quickAddBox.setVisible(false);
        quickAddBox.setStyleName("input_box");
    }

    // inner class

    // TODO : this needs to go into a presenter;
    class DeleteCallBack extends Callback<MenuItem> {

        private final IDeleteMenuHandler deleteHandler;

        public DeleteCallBack(IDeleteMenuHandler deleteHandler) {
            this.deleteHandler = deleteHandler;
        }

        @Override
        public void onSucess(MenuItem item) {
            MenuHiderTimer timer = new MenuHiderTimer(table, editRow);
            DeletedCell deletedCell = new DeletedCell(currentEditSelection,
                    deleteHandler.getUndoHandler(item, CollectionMenu.this, timer));
            table.setWidget(editRow, editIndex, deletedCell);
            timer.schedule(6000);
        }

        @Override
        public void onFailure() {
            // do nothing on failure since an error msg will be shown to the user
        }
    }

    class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final MenuItem item;
        private final String html;

        private Label count;
        private final HoverCell action;
        private final String folderId;

        public MenuCell(final MenuItem item, final IDeleteMenuHandler deleteHandler) {

            super.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
            // text box used when user wishes to edit a collection name

            this.item = item;
            folderId = "right" + item.getId();
            action = new HoverCell();
            action.getEdit().addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();
                    Cell cell = table.getCellForEvent(event);
                    editCollectionNameBox.setText(getMenuItem().getName());
                    editRow = cell.getRowIndex();
                    editIndex = cell.getCellIndex();
                    currentEditSelection = getMenuItem();
                    table.setWidget(editRow, editIndex, editCollectionNameBox);
                    editCollectionNameBox.setVisible(true);
                    editCollectionNameBox.setFocus(true);
                    editCollectionNameBox.selectAll();
                }
            });

            if (deleteHandler != null) {
                action.getDelete().addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation();
                        Cell cell = table.getCellForEvent(event);
                        if (cell == null)
                            return;

                        editRow = cell.getRowIndex();
                        editIndex = cell.getCellIndex();
                        currentEditSelection = getMenuItem();
                        deleteHandler.delete(item.getId(), new DeleteCallBack(deleteHandler));
                    }
                });
            }

            String name = item.getName();
            if (name.length() > 22)
                name = (name.substring(0, 22) + "...");

            html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">" + name
                    + "</span><span class=\"menu_count\" id=\"" + folderId + "\"></span>";

            panel = new HTMLPanel(html);
            panel.setTitle(item.getName());

            count = new Label(formatNumber(item.getCount()));

            panel.add(count, folderId);
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);
        }

        public void setSelected(boolean selected) {
            if (selected)
                this.addStyleName("collection_user_menu_row_selected");
            else
                this.removeStyleName("collection_user_menu_row_selected");
        }

        public void showBusyIndicator() {
            setRightPanel(ImageUtil.getBusyIcon());
        }

        public void showFolderCount() {
            setRightPanel(count);
        }

        public void updateCount(long newCount) {
            item.setCount(newCount);
            count = new Label(formatNumber(item.getCount()));
        }

        public MenuItem getMenuItem() {
            return this.item;
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);

            if (item.isSystem())
                return;

            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER:
                setRightPanel(action);
                break;

            case Event.ONMOUSEOUT:
                EventTarget target = event.getRelatedEventTarget(); // image

                if (Element.is(target)) {
                    Element element = Element.as(target);
                    Element eDelete = action.getDelete().getElement();
                    Element eEdit = action.getEdit().getElement();

                    if (element.equals(eEdit) || element.equals(eDelete))
                        break;
                }
                setRightPanel(count);
                break;
            }
        }

        private void setRightPanel(Widget widget) {
            Widget toReplace = panel.getWidget(0);
            if (toReplace == null)
                GWT.log("Cannot replace widget"); // TODO
            else {
                panel.remove(0);
                panel.add(widget, folderId);
            }
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        private String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }
    }

}
