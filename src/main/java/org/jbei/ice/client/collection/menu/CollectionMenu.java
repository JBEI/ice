package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.ShareCollectionData;
import org.jbei.ice.client.collection.widget.ShareCollectionWidget;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.entry.display.handler.ReadBoxSelectionHandler;
import org.jbei.ice.lib.shared.dto.folder.FolderType;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
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
public class CollectionMenu extends Composite {

    private final FlexTable layout;
    private MenuItem currentEditSelection;

    private int row;
    private int editRow = -1;
    private int editIndex = -1;
    private final SingleSelectionModel<MenuItem> selectionModel;
    private final boolean hasQuickEdit;
    private final QuickAddWidget editName;
    private List<HoverOption> cellHoverOptions;
    private ServiceDelegate<MenuItem> promotionDelegate;
    private ServiceDelegate<MenuItem> demotionDelegate;

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
    }

    public void setPromotionDelegate(ServiceDelegate<MenuItem> serviceDelegate) {
        this.promotionDelegate = serviceDelegate;
    }

    public void setDemotionDelegate(ServiceDelegate<MenuItem> serviceDelegate) {
        this.demotionDelegate = serviceDelegate;
    }

    public void setCellHoverOptions(List<HoverOption> options) {
        cellHoverOptions = options;
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

    /**
     * replaces current edit cell (in menu)
     * with new cell with folder
     */
    public void setMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if ((this.editIndex == -1 && this.editRow == -1) || (item == null))
            return;

        final MenuCell cell = new MenuCell(item, deleteHandler, editRow);
        cell.setHoverOptions(cellHoverOptions);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
        layout.setWidget(editRow, editIndex, cell);
        this.editName.setVisible(true);
    }

    public void addMenuItem(MenuItem item, IDeleteMenuHandler deleteHandler) {
        if (item == null)
            return;

        row += 1;
        final MenuCell cell = new MenuCell(item, deleteHandler, row);
        int userCount = 0;
        int groupCount = 0;

        // display counts of who private folders have been shared with
        // permissions are only set for user private folder. shared folders have owners
        if (item.getPermissions() != null && !item.getPermissions().isEmpty()) {
            for (PermissionInfo info : item.getPermissions()) {
                if (!info.isFolder())
                    continue;

                if (info.getArticle() == PermissionInfo.Article.GROUP)
                    groupCount += 1;
                if (info.getArticle() == PermissionInfo.Article.ACCOUNT)
                    userCount += 1;
            }
        }

        if (hasQuickEdit)
            cell.setShared(userCount, groupCount);
        cell.setSharerInfo();
        cell.setHoverOptions(cellHoverOptions);
        cell.addClickHandler(new CellSelectionHandler(selectionModel, cell));
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

            final MenuCell newCell = new MenuCell(item, deleteHandler, i);
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

    public void addQuickAddHandler(final ClickHandler handler) {
        this.quickAddWidget.addQuickAddHandler(handler);
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

    public class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final MenuItem item;

        private Label count;
        private final HoverCell action;
        private final String folderId;
        private final Widget busyIndicator;
        private ShareCollectionWidget shareCollectionWidget;
        private final HTML shared;
        private final int row;

        public MenuCell(final MenuItem item, final IDeleteMenuHandler handler, int row) {
            this.item = item;
            this.row = row;
            folderId = "right" + item.getId();
            action = new HoverCell();
            shareCollectionWidget = new ShareCollectionWidget(this, item.getName(), new Delegate<PermissionInfo>() {

                @Override
                public void execute(PermissionInfo info) {
                    if (permissionInfoDelegate == null)
                        return;

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

                    info.setType(PermissionInfo.Type.READ_FOLDER);
                    info.setTypeId(item.getId());
                    ShareCollectionData data = new ShareCollectionData(info, shareCollectionWidget.getAddCallback());
                    permissionInfoDelegate.execute(data);
                }
            });

            action.getOptionSelection().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    HoverOption selected = action.getOptionSelection().getSelectedObject();
                    if (selected == null)
                        return;

                    switch (selected) {
                        case EDIT:
                            editAction();
                            break;
                        case DELETE:
                            deleteAction(handler);
                            break;
                        case SHARE:
                            shareAction();
                            break;
                        case PIN:
                            pinAction();
                            break;
                        case UNPIN:
                            unpinAction();
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
            count = new Label(formatNumber(item.getCount()));
            panel.add(count, "count_" + folderId);
            panel.add(action, "submenu_" + folderId);
            panel.add(busyIndicator, "busy_indicator_" + folderId);

            if (!item.hasSubMenu() || (item.getOwner() == null && (item.getPermissions() == null || item
                    .getPermissions().isEmpty())) || item.getType() == FolderType.PUBLIC)
                panel.setStyleName("system_collection_user_menu_row");
            else
                panel.setStyleName("user_collection_user_menu_row");

            initWidget(panel);

            // mouse handlers
            addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    if (!item.hasSubMenu() || cellHoverOptions == null || cellHoverOptions.isEmpty())
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
                    if (!item.hasSubMenu() || cellHoverOptions == null || cellHoverOptions.isEmpty())
                        return;
                    busyIndicator.setVisible(false);
                    action.setVisible(true);
                    count.setVisible(false);
                }
            });
        }

        protected String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }

        protected int row() {
            return this.row;
        }

        protected void editAction() {
            currentEditSelection = getMenuItem();
            editRow = row;
            editIndex = 0;
            layout.setWidget(editRow, editIndex, editName);
            editName.setVisible(true);
            editName.setInputName(currentEditSelection.getName());
            editName.setFocus(true);
        }

        protected void deleteAction(IDeleteMenuHandler handler) {
            if (handler == null)
                return;

            currentEditSelection = getMenuItem();
            editRow = row;
            editIndex = 0;
            if (Window.confirm("Delete \"" + currentEditSelection.getName() + "\"? This action cannot be undone")) {
                handler.delete(item.getId(), new DeleteCallBack());
            }
        }

        protected void shareAction() {
            currentEditSelection = getMenuItem();
            editRow = row;
            editIndex = 0;
            shareCollectionWidget.showDialog(currentEditSelection.getPermissions());
        }

        protected void pinAction() {
            if (promotionDelegate == null)
                return;
            promotionDelegate.execute(getMenuItem());
        }

        protected void unpinAction() {
            if (demotionDelegate == null)
                return;
            demotionDelegate.execute(getMenuItem());
        }

        /**
         * show the information for the person doing the sharing
         * this is typically the owner of the folder. this method is meant to be called
         * by the owner
         */
        public void setSharerInfo() {
            if (item.getOwner() == null)
                return;

            shared.setVisible(true);
            String html = "<span style=\"color:#999; font-size: 9px\">Shared by ";
            html += ("<a class=\"opacity_hover\" href=\"#" + Page.PROFILE.getLink() + ";id="
                    + item.getOwner().getId() + "\">"
                    + item.getOwner().getFullName() + "</a></span>");
            shared.setHTML(html);
        }

        public void setShared(int userCount, int groupCount) {
            int count = userCount + groupCount;
            if (item.getId() <= 0)
                return;

            panel.setStyleName("user_collection_user_menu_row");
            String html = "<span style=\"color: #999; font-size: 9px\">";

            if (count <= 0) {
                html += "Private</span>";
            } else {
                String userString = "<b>" + userCount + "</b> user";
                userString += (userCount != 1 ? "s" : "");
                String groupString = "<b>" + groupCount + "</b> group";
                groupString += (groupCount != 1 ? "s" : "");
                html += "Shared with ";
                if (userCount > 0 && groupCount > 0)
                    html += (userString + " & " + groupString + " </span>");
                else if (userCount > 0)
                    html += (userString + " </span>");
                else if (groupCount > 0)
                    html += (groupString + " </span>");
            }

            shared.setHTML(html);
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
            count.setText(formatNumber(item.getCount()));
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

        public void setHoverOptions(List<HoverOption> cellHoverOptions) {
            if (cellHoverOptions == null)
                return;
            action.setHoverOptions(cellHoverOptions);
        }
    }
}
