package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.collection.ShareCollectionData;
import org.jbei.ice.client.collection.widget.ShareCollectionWidget;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.entry.view.handler.ReadBoxSelectionHandler;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
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
    private int editRow = -1;
    private int editIndex = -1;
    private final SingleSelectionModel<MenuItem> selectionModel;
    private final CollectionMenuPresenter presenter;
    private final boolean hasQuickEdit;
    private final QuickAddWidget editName;

    // quick add
    private QuickAddWidget quickAddWidget;
    private Delegate<ShareCollectionData> permissionInfoDelegate;

    public CollectionMenu(boolean addQuickEdit, String header) {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setStyleName("collection_menu_table");
        initWidget(layout);

        // quick add
        quickAddWidget = new QuickAddWidget(true);
        quickAddWidget.setVisible(false);

        // quick edit
        editName = new QuickAddWidget(false);
        editName.setVisible(false);

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

    public void setPermissionInfoDelegate(Delegate<ShareCollectionData> infoDelegate) {
        this.permissionInfoDelegate = infoDelegate;
    }

    public void setEmptyCollectionMessage(String msg) {
        int messageRow = hasQuickEdit ? 2 : 1;
        HTML html = new HTML("<span style=\"font-size: 0.70em; color: #666; padding: 4px; font-style:italic\">"
                                     + msg + "</span>");
        layout.setWidget(messageRow, 0, html);
    }

    public String getQuickAddInputName() {
        return this.quickAddWidget.getInputName();
    }

    public SingleSelectionModel<MenuItem> getSelectionModel() {
        return this.selectionModel;
    }

    public String getQuickEditText() {
        return editName.getInputName();
    }

    public boolean isQuickEditVisible() {
        return editName.isVisible();
    }

    public void addQuickEditKeyPressHandler(final KeyPressHandler handler) {
        editName.addQuickAddKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                    return;
                handler.onKeyPress(event);
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

    public void setPermissions(ArrayList<PermissionInfo> list) {
        presenter.setPermissions(list);

        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget w = layout.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            cell.setShared(presenter.getPermissionCount(cell.getMenuItem().getId()));
            // TODO : include the shared info in a tooltip. All the information is contained in here
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
        this.editName.setVisible(true);
    }

    public void addMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler);
        cell.setShared(0);
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
    public void setBusyIndicator(Set<Long> ids, boolean visible) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget w = layout.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (ids.contains(cell.getMenuItem().getId()))
                cell.setBusyIndicatorVisibility(visible);
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

    public void addQuickAddKeyPressHandler(final KeyPressHandler handler) {
        this.quickAddWidget.addQuickAddKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                    return;
                handler.onKeyPress(event);
            }
        });
    }

    // inner classes
    class DeleteCallBack extends Callback<MenuItem> {

        @Override
        public void onSuccess(MenuItem item) {
            if (layout == null || layout.getRowCount() <= editRow)
                return;

            layout.removeRow(editRow);
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
        private final HoverCell action;
        private final String folderId;
        private final Widget busyIndicator;
        private ShareCollectionWidget shareCollectionWidget;
        private final HTML shared;

        public MenuCell(final MenuItem item, final IDeleteMenuHandler handler) {
            this.item = item;
            folderId = "right" + item.getId();
            action = new HoverCell();
            shareCollectionWidget = new ShareCollectionWidget(item.getName(), new Delegate<PermissionInfo>() {
                @Override
                public void execute(PermissionInfo info) {
                    if (permissionInfoDelegate == null)
                        return;

                    presenter.removePermission(info);
                    ShareCollectionData data = new ShareCollectionData(info, shareCollectionWidget.getRemoveCallback());
                    data.setDelete(true);
                    permissionInfoDelegate.execute(data);
                }
            });

            shareCollectionWidget.getPermissionsPresenter().setWriteAddSelectionHandler(new ReadBoxSelectionHandler() {

                @Override
                public void updatePermission(PermissionInfo info) {
                    if (permissionInfoDelegate == null)
                        return;

                    presenter.addPermission(info);
                    info.setType(PermissionInfo.Type.WRITE_FOLDER);
                    info.setTypeId(item.getId());
                    ShareCollectionData data = new ShareCollectionData(info, shareCollectionWidget.getAddCallback());
                    permissionInfoDelegate.execute(data);
                }
            });

            shareCollectionWidget.getPermissionsPresenter().setReadAddSelectionHandler(new ReadBoxSelectionHandler() {

                @Override
                public void updatePermission(PermissionInfo info) {
                    if (permissionInfoDelegate == null)
                        return;

                    presenter.addPermission(info);
                    info.setType(PermissionInfo.Type.READ_FOLDER);
                    info.setTypeId(item.getId());
                    ShareCollectionData data = new ShareCollectionData(info, shareCollectionWidget.getAddCallback());
                    permissionInfoDelegate.execute(data);
                }
            });

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
                                layout.setWidget(editRow, editIndex, editName);
                                editName.setVisible(true);
                                editName.setInputName(name);
                                editName.setFocus(true);
                                break;
                            }
                            break;

                        case DELETE:
                            if (handler == null)
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
                                if (Window.confirm("Delete \"" + currentEditSelection.getName()
                                                           + "\"? This action cannot be undone")) {
                                    handler.delete(item.getId(), new DeleteCallBack());
                                }
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
                                shareCollectionWidget.showDialog(presenter.getFolderPermissions(cellId));
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
                    busyIndicator.setVisible(false);
                    action.setVisible(false);
                    count.setVisible(true);
                }
            });
            action.setVisible(false);

            String name = item.getName();
            if (name.length() > 25)
                name = (name.substring(0, 22) + "...");

            shared = new HTML("<span style=\"color: #999; font-size: 9px\">Private</span>");
            shared.setVisible(false);

            busyIndicator = ImageUtil.getBusyIcon();
            busyIndicator.setVisible(false);

            String html = "<span class=\"collection_user_menu\">" + name
                    + "</span><span class=\"menu_count\" id=\"count_" + folderId + "\"></span>"
                    + "<span class=\"menu_count\" " + "id=\"submenu_" + folderId + "\"></span>"
                    + "<span class=\"menu_count\" " + "id=\"busy_indicator_" + folderId + "\"></span>"
                    + "<span id=\"shared_row_info\">";

            panel = new HTMLPanel(html);
            panel.add(shared, "shared_row_info");
            panel.setTitle(item.getName());
            count = new Label(presenter.formatNumber(item.getCount()));
            panel.add(count, "count_" + folderId);
            panel.add(action, "submenu_" + folderId);
            panel.add(busyIndicator, "busy_indicator_" + folderId);

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
                    busyIndicator.setVisible(false);
                    action.setVisible(false);
                    count.setVisible(true);
                }
            });

            addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    if (item.isSystem())
                        return;
                    busyIndicator.setVisible(false);
                    action.setVisible(true);
                    count.setVisible(false);
                }
            });
        }

        public void setShared(int count) {
            if (item.getId() <= 0 || item.isSystem())
                return;

            item.setShared(count > 0);
            if (item.isShared()) {
                String html = "<span style=\"color: #999; font-size: 9px\">Shared&nbsp;";
                html += "<i class=\"" + FAIconType.EYE_OPEN.getStyleName() + "\"></i></span>";
                shared.setHTML(html);
            }
            shared.setVisible(true);
            action.setVisible(false);
            this.count.setVisible(true);
        }

        public void setSelected(boolean selected) {
            if (selected)
                this.addStyleName("collection_user_menu_row_selected");
            else
                this.removeStyleName("collection_user_menu_row_selected");
        }

        public void setBusyIndicatorVisibility(boolean visible) {
            busyIndicator.setVisible(visible);
        }

        public void showFolderCount() {
            busyIndicator.setVisible(false);
            action.setVisible(false);
            count.setVisible(true);
        }

        public void updateCount(long newCount) {
            item.setCount(newCount);
            count.setText(presenter.formatNumber(item.getCount()));
        }

        public MenuItem getMenuItem() {
            return this.item;
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }

        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }
    }
}
