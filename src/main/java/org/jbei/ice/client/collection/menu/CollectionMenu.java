package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.collection.widget.ShareCollectionWidget;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Left bar menu for showing user collections. Also adds widgets such as
 * an icon for adding a new user collection and edit/delete
 *
 * @author Hector Plahar
 */
public class CollectionMenu extends Composite implements CollectionMenuPresenter.IView {

    private final FlexTable layout;
    private MenuItem currentEditSelection;

    private int row;
    private final TextBox editCollectionNameBox;
    private int editRow = -1;
    private int editIndex = -1;
    private final SingleSelectionModel<MenuItem> selectionModel;
    private final CollectionMenuPresenter presenter;
    private final boolean hasQuickEdit;

    // quick add
    private QuickAddWidget quickAddWidget;

    public CollectionMenu(boolean addQuickEdit, String header) {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setStyleName("collection_menu_table");
        initWidget(layout);

        // quick add
        quickAddWidget = new QuickAddWidget();
        quickAddWidget.setVisible(false);

        // quick edit
        editCollectionNameBox = new TextBox();
        editCollectionNameBox.setStyleName("input_box");
        editCollectionNameBox.setWidth("99%");
        editCollectionNameBox.setVisible(false);

        hasQuickEdit = addQuickEdit;

        // header panel
        final MenuHeader menuHeaderPanel = new MenuHeader(header, addQuickEdit);
        menuHeaderPanel.addQuickAddHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                switchButton();
            }
        });
        layout.setWidget(row, 0, menuHeaderPanel);
        layout.getCellFormatter().setStyleName(row, 0, "collections_menu_header");
        if (addQuickEdit) {
            row += 1;
            layout.setWidget(row, 0, quickAddWidget);
        }

        menuHeaderPanel.addExpandCollapseHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int start = hasQuickEdit ? 2 : 1;
                for (int i = start; i < layout.getRowCount(); i += 1) {
                    layout.getWidget(i, 0).setVisible(!menuHeaderPanel.isCollapsed());
                }
            }
        });
        selectionModel = new SingleSelectionModel<MenuItem>();
        presenter = new CollectionMenuPresenter(this);
    }

    public void setEmptyCollectionMessage(String msg) {
        int messageRow = hasQuickEdit ? 2 : 1;
        HTML html = new HTML("<span style=\"font-size: 0.70em; color: #666; padding: 4px; font-style:italic\">" + msg +
                                     "</span>");
        layout.setWidget(messageRow, 0, html);
    }

    public String getQuickAddInputName() {
        return this.quickAddWidget.getInputName();
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

    public void setSelection(long id) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget w = layout.getWidget(i, 0);
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
     * replaces current edit cell (in menu)
     * with new cell with folder
     */
    public void setMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if ((this.editIndex == -1 && this.editRow == -1) || (item == null))
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        layout.setWidget(editRow, editIndex, cell);
        this.editCollectionNameBox.setVisible(false);
    }

    public void addMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        row += 1;
        layout.setWidget(row, 0, cell);
    }

    // currently this is being used for deleted cells only
    public void updateMenuItem(long id, MenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget w = layout.getWidget(i, 0);
            if (!(w instanceof DeletedCell))
                continue;

            DeletedCell cell = (DeletedCell) w;
            if (cell.getMenuItem().getId() != id)
                continue;

            final MenuCell newCell = new MenuCell(item, deleteHandler);
            newCell.addClickHandler(new CellSelectionHandler(selectionModel, newCell));
            layout.setWidget(i, 0, newCell);
            break;
        }
    }

    /**
     * sets the busy indicator where the folder counts are displayed
     * to indicate that some form of update is taking place
     */
    public void setBusyIndicator(Set<Long> ids) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget w = layout.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (ids.contains(cell.getMenuItem().getId()))
                cell.showBusyIndicator();
        }
    }

    public void updateCounts(ArrayList<MenuItem> items) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget w = layout.getWidget(i, 0);
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

    public MenuItem getCurrentEditSelection() {
        return currentEditSelection;
    }

    public void switchButton() {
        if (quickAddWidget == null)
            return;

        if (!quickAddWidget.isVisible()) {
            quickAddWidget.setVisible(true);
        }
    }

    public void hideQuickText() {
        quickAddWidget.setVisible(false);
    }

    public boolean getQuickAddVisibility() {
        return quickAddWidget.isVisible();
    }

    public void addQuickAddKeyPressHandler(KeyPressHandler handler) {
        this.quickAddWidget.addQuickAddKeyPressHandler(handler);
    }

    public void addSaveCollectionNameHandler(ClickHandler handler) {
        this.quickAddWidget.addSubmitHandler(handler);
    }

    // inner classes

    // TODO : this needs to go into a presenter;
    class DeleteCallBack extends Callback<MenuItem> {

        private final IDeleteMenuHandler deleteHandler;

        public DeleteCallBack(IDeleteMenuHandler deleteHandler) {
            this.deleteHandler = deleteHandler;
        }

        @Override
        public void onSuccess(MenuItem item) {
            MenuHiderTimer timer = new MenuHiderTimer(layout, editRow);
            DeletedCell deletedCell = new DeletedCell(currentEditSelection,
                                                      deleteHandler.getUndoHandler(item, CollectionMenu.this, timer));
            layout.setWidget(editRow, editIndex, deletedCell);
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

        private Label count;
        private Icon collectionIcon; // icon displayed before the collection name
        private final HoverCell action;
        private final String folderId;

        public MenuCell(final MenuItem item, final IDeleteMenuHandler deleteHandler) {

            this.item = item;
            folderId = "right" + item.getId();
            action = new HoverCell();
            action.getOptionSelection().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    HoverCell.HoverOptions selected = action.getOptionSelection().getSelectedObject();
                    if (selected == null)
                        return;

                    String name = getMenuItem().getName();
                    long id = getMenuItem().getId();

                    switch (selected) {
                        case EDIT:

                            for (int i = 0; i < layout.getRowCount(); i += 1) {
                                Widget widget = layout.getWidget(i, 0);
                                if (widget == null || !(widget instanceof MenuCell))
                                    continue;

                                MenuCell cell = (MenuCell) widget;
                                long cellId = cell.getMenuItem().getId();
                                String cellName = cell.getMenuItem().getName();

                                if (!cellName.equals(name) && id != cellId)
                                    continue;

                                // found cell (need a better way of looking this stuff up)
                                currentEditSelection = getMenuItem();
                                editRow = i;
                                editIndex = 0;
                                layout.setWidget(editRow, editIndex, editCollectionNameBox);
                                editCollectionNameBox.setVisible(true);
                                editCollectionNameBox.setFocus(true);
                                editCollectionNameBox.setText(name);
                                editCollectionNameBox.selectAll();
                                break;
                            }
                            break;

                        case DELETE:
                            if (deleteHandler == null)
                                break;

                            for (int i = 0; i < layout.getRowCount(); i += 1) {
                                Widget widget = layout.getWidget(i, 0);
                                if (widget == null || !(widget instanceof MenuCell))
                                    continue;

                                MenuCell cell = (MenuCell) widget;
                                long cellId = cell.getMenuItem().getId();
                                String cellName = cell.getMenuItem().getName();

                                if (!cellName.equals(name) && id != cellId)
                                    continue;

                                // found cell (need a better way of looking this stuff up)
                                currentEditSelection = getMenuItem();
                                editRow = i;
                                editIndex = 0;
                                deleteHandler.delete(item.getId(), new DeleteCallBack(deleteHandler));
                                break;
                            }
                            break;

                        case SHARE:
                            for (int i = 0; i < layout.getRowCount(); i += 1) {
                                Widget widget = layout.getWidget(i, 0);
                                if (widget == null || !(widget instanceof MenuCell))
                                    continue;

                                MenuCell cell = (MenuCell) widget;
                                long cellId = cell.getMenuItem().getId();
                                String cellName = cell.getMenuItem().getName();

                                if (!cellName.equals(name) && id != cellId)
                                    continue;

                                // found cell (need a better way of looking this stuff up)
                                currentEditSelection = getMenuItem();
                                editRow = i;
                                editIndex = 0;
                                ShareCollectionWidget shareCollectionWidget = new ShareCollectionWidget(id, name);
                                shareCollectionWidget.showDialog();
                            }
                            break;
                    }

                    action.getOptionSelection().setSelected(selected, false);
                    action.hideOptions();
                }
            });

            // close handler
            action.addOptionsCloseHandler(new CloseHandler<PopupPanel>() {
                @Override
                public void onClose(CloseEvent<PopupPanel> event) {
                    if (item.isSystem())
                        return;

                    setRightPanel(count);
                }
            });

            String name = item.getName();
            if (name.length() > 25)
                name = (name.substring(0, 22) + "...");

            String html = "<span id=\"collection_icon\"></span><span class=\"collection_user_menu\">" + name
                    + "</span><span class=\"menu_count\" id=\"" + folderId + "\"></span>";

            if (!item.isSystem())
                html += "<br><span style=\"color: #ccc; font-size: 9px\">Private</span>";

            panel = new HTMLPanel(html);
            panel.setTitle(item.getName());
            count = new Label(formatNumber(item.getCount()));
            panel.add(count, folderId);
            if (item.isSystem())
                panel.setStyleName("system_collection_user_menu_row");
            else
                panel.setStyleName("user_collection_user_menu_row");

            initWidget(panel);

            // mouse handlers
            addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    if (item.isSystem())
                        return;

                    if (action.optionsAreVisible())
                        return;

                    setRightPanel(count);
                }
            });

            addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    if (item.isSystem())
                        return;

                    setRightPanel(action);
                }
            });
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

        private void setRightPanel(Widget widget) {
            Widget toReplace = panel.getWidget(0);
            if (toReplace == null)
                return;

            panel.remove(0);
            panel.add(widget, folderId);
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        private String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }

        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }

        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }
    }
}
