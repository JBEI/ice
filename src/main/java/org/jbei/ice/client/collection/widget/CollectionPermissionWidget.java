package org.jbei.ice.client.collection.widget;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.entry.display.handler.ReadBoxSelectionHandler;
import org.jbei.ice.client.entry.display.model.PermissionSuggestOracle;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * Widget for changing/adding permissions for a collection
 *
 * @author Hector Plahar
 */
public class CollectionPermissionWidget extends Composite {

    private FlexTable readListTable;
    private FlexTable writeListTable;
    private HTML addReadPermission;
    private HTML addWritePermission;
    private CheckBox makePublic;
    private SuggestBox permissionSuggestions;
    private boolean isViewingWriteTab;
    private final ArrayList<AccessPermission> readList;  // list of read permissions (includes groups)
    private final ArrayList<AccessPermission> writeList; // list of write permissions (includes groups)

    public CollectionPermissionWidget() {
        FlexTable layout = new FlexTable();
        initWidget(layout);
        readList = new ArrayList<AccessPermission>();
        writeList = new ArrayList<AccessPermission>();

        initComponents();

        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        int row = 0;
        layout.setHTML(row, 0, "<br><i class=\"" + FAIconType.CHECK_EMPTY.getStyleName()
                + "\"></i> Propagate permissions to parts <i class=\""
                + FAIconType.QUESTION_SIGN.getStyleName() + "\"></i>");
        layout.getFlexCellFormatter().setColSpan(row, 0, 2);

        row += 1;
        FlexTable permission = createPermissionWidget();
        layout.setWidget(row, 0, permission);
        layout.getFlexCellFormatter().setColSpan(row, 0, 2);

        // input suggest box for adding permissions
        createSuggestWidget(permission);
    }

    protected FlexTable createPermissionWidget() {
        final FlexTable permissionLayout = new FlexTable();
        permissionLayout.setCellPadding(0);
        permissionLayout.setCellSpacing(0);
        permissionLayout.setWidth("90%");
        permissionLayout.setStyleName("collection_permission_widget_border");

        HTMLPanel readLabelPanel = new HTMLPanel(
                "Can Read <span style=\"float: right\" id=\"permission_read_add\"></span>");
        readLabelPanel.add(addReadPermission, "permission_read_add");
        readLabelPanel.setStyleName("permission_tab_active");

        permissionLayout.setWidget(0, 0, readLabelPanel);
        permissionLayout.getCellFormatter().setWidth(0, 0, "50%");

        HTMLPanel writeLabelPanel = new HTMLPanel(
                "Can Edit <span style=\"float: right\" id=\"permission_write_add\"></span>");
        writeLabelPanel.add(addWritePermission, "permission_write_add");
        writeLabelPanel.setStyleName("permission_tab_inactive");

        permissionLayout.setWidget(0, 1, writeLabelPanel);
        permissionLayout.getCellFormatter().setWidth(0, 1, "50%");
        LayoutHeaderClickHandler handler = new LayoutHeaderClickHandler(permissionLayout, readLabelPanel,
                                                                        writeLabelPanel);
        permissionLayout.addClickHandler(handler);

        // contents
        permissionLayout.setWidget(2, 0, readListTable);
        permissionLayout.getFlexCellFormatter().setColSpan(2, 0, 2);

        // make public footer
        permissionLayout.setWidget(3, 0, makePublic);
        permissionLayout.getFlexCellFormatter().setColSpan(3, 0, 2);
        permissionLayout.getFlexCellFormatter().setStyleName(3, 0, "permission_footer");

        // click handlers
        addReadPermission.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isViewingWriteTab)
                    return;
                permissionLayout.getRowFormatter().setVisible(1, (!permissionLayout.getRowFormatter().isVisible(1)));
            }
        });
        addWritePermission.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!isViewingWriteTab)
                    return;
                permissionLayout.getRowFormatter().setVisible(1, (!permissionLayout.getRowFormatter().isVisible(1)));
            }
        });

        return permissionLayout;
    }

    protected void createSuggestWidget(final FlexTable table) {
        HTML deleteIcon = new HTML("<i class=\"delete_icon " + FAIconType.REMOVE.getStyleName() + "\"></i>");
        deleteIcon.setStyleName("display-inline");
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                table.getRowFormatter().setVisible(1, false);
            }
        });

        HTMLPanel permissionAddPanel = new HTMLPanel("<span id=\"p_suggest_box\"></span> "
                                                             + "<span id=\"suggest_cancel\"></span>");
        permissionAddPanel.add(permissionSuggestions, "p_suggest_box");
        permissionAddPanel.add(deleteIcon, "suggest_cancel");
        table.setWidget(1, 0, permissionAddPanel);
        table.getFlexCellFormatter().setColSpan(1, 0, 2);
        table.getRowFormatter().setVisible(1, false);
    }

    protected void initComponents() {
        readListTable = new FlexTable();
        readListTable.setCellPadding(3);
        readListTable.setCellSpacing(0);
        readListTable.setStyleName("permission_list");

        writeListTable = new FlexTable();
        writeListTable.setCellPadding(3);
        writeListTable.setCellSpacing(0);
        writeListTable.setStyleName("permission_list");

        addReadPermission = new HTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
        addReadPermission.addStyleName("edit_icon");
        addReadPermission.addStyleName("font-14em");

        addWritePermission = new HTML("<b>0</b>");
        addWritePermission.addStyleName("edit_icon");
        addWritePermission.addStyleName("font-14em");

        makePublic = new CheckBox("Enable Public Read Access");
        makePublic.setStyleName("permission_footer_link");

        permissionSuggestions = new SuggestBox(new PermissionSuggestOracle());
        permissionSuggestions.setWidth("45%");
        permissionSuggestions.getValueBox().getElement().setAttribute("placeHolder", "Enter user/group name");
        permissionSuggestions.setLimit(7);


        permissionSuggestions.addSelectionHandler(new ReadBoxSelectionHandler() {
            @Override
            public void updatePermission(AccessPermission access) {
                if (isViewingWriteTab) {
                    access.setType(AccessPermission.Type.WRITE_ENTRY);
                    writeList.add(access);
                    addWriteItem(access, null);
                } else {
                    access.setType(AccessPermission.Type.READ_ENTRY);
                    readList.add(access);
                    addReadItem(access, null);
                }
                permissionSuggestions.getValueBox().setText("");
                permissionSuggestions.getValueBox().setFocus(true);
            }
        });
    }

    public void addWriteItem(final AccessPermission item, final Delegate<AccessPermission> deleteDelegate) {
        addPermissionItem(writeListTable, item, deleteDelegate);
        if (isViewingWriteTab)
            addReadPermission.setHTML("<b>" + readList.size() + "</b>");
        else
            addWritePermission.setHTML("<b>" + writeList.size() + "</b>");
    }

    public void addReadItem(final AccessPermission item, final Delegate<AccessPermission> deleteDelegate) {
        addPermissionItem(readListTable, item, deleteDelegate);
        if (isViewingWriteTab)
            addReadPermission.setHTML("<span>" + readList.size() + "</span>");
        else
            addWritePermission.setHTML("<span>" + writeList.size() + "</span>");
    }

    protected void addPermissionItem(FlexTable table, final AccessPermission item,
            final Delegate<AccessPermission> deleteDelegate) {
        int row = table.getRowCount();
        String iconStyle;
        String display;

        if (item.getArticle() == AccessPermission.Article.GROUP) {
            iconStyle = FAIconType.GROUP.getStyleName() + " permission_group";
            display = item.getDisplay();
        } else {
            iconStyle = FAIconType.USER.getStyleName() + " permission_user";
            display = "<a href=\"#" + Page.PROFILE.getLink() + ";id="
                    + item.getArticleId() + "\">" + item.getDisplay() + "</a>";
        }

        table.setHTML(row, 0, "<i class=\"" + iconStyle + "\"></i> " + display);
        table.getCellFormatter().setWidth(row, 0, "160px");
        Icon deleteIcon = new Icon(FAIconType.REMOVE);
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deleteDelegate.execute(item);
            }
        });
        deleteIcon.addStyleName("delete_icon");
        table.setWidget(row, 1, deleteIcon);
    }

    public void removeItem(AccessPermission access) {
        if (access.isCanRead())
            removePermissionItem(readListTable, access);
        else if (access.isCanWrite())
            removePermissionItem(writeListTable, access);
    }

    protected void removePermissionItem(FlexTable table, AccessPermission item) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            String html = table.getHTML(i, 0);
            if (html.contains(Page.PROFILE.getLink() + ";id=" + item.getArticleId())) {
                table.removeRow(i);
                break;
            }
        }
    }

    public void setPermissionData(ArrayList<AccessPermission> listAccess, Delegate<AccessPermission> deleteHandler) {
        if (listAccess == null)
            return;

        resetPermissionDisplay();

        for (AccessPermission access : listAccess) {
            // skip displaying permissions assigned to self
            if (access.getArticle() == AccessPermission.Article.ACCOUNT
                    && access.getArticleId() == ClientController.account.getId())
                continue;

            if (access.isCanWrite()) {
                addWriteItem(access, deleteHandler);
            } else if (access.isCanRead()) {
                addReadItem(access, deleteHandler);
            }
        }
    }

    public void resetPermissionDisplay() {
        writeListTable.removeAllRows();
        readListTable.removeAllRows();
        writeList.clear();
        readList.clear();
    }

    /**
     * ClickHandler for Permissions header
     */
    private class LayoutHeaderClickHandler implements ClickHandler {

        private HTMLPanel readLabelPanel;
        private HTMLPanel writeLabelPanel;
        private final FlexTable table;

        public LayoutHeaderClickHandler(FlexTable table, HTMLPanel readPanel, HTMLPanel writePanel) {
            this.readLabelPanel = readPanel;
            this.writeLabelPanel = writePanel;
            this.table = table;
        }

        @Override
        public void onClick(ClickEvent event) {
            HTMLTable.Cell cell = table.getCellForEvent(event);
            if (cell.getRowIndex() != 0)
                return;

            if (isViewingWriteTab) {
                if (cell.getCellIndex() == 1)
                    return;

                // switch to read tab
                isViewingWriteTab = false;
                readLabelPanel.setStyleName("permission_tab_active");
                writeLabelPanel.setStyleName("permission_tab_inactive");
                table.setWidget(2, 0, readListTable);
                addWritePermission.setHTML("<span>" + writeList.size() + "</span>");
                addReadPermission.setHTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
            } else {
                if (cell.getCellIndex() == 0)
                    return;

                // switch to write tab
                isViewingWriteTab = true;
                readLabelPanel.setStyleName("permission_tab_inactive");
                writeLabelPanel.setStyleName("permission_tab_active");
                table.setWidget(2, 0, writeListTable);
                addReadPermission.setHTML("<span>" + readList.size() + "</span>");
                addWritePermission.setHTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
            }

            table.getRowFormatter().setVisible(3, !isViewingWriteTab);
        }
    }
}
