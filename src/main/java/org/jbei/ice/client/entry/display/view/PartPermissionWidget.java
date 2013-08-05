package org.jbei.ice.client.entry.display.view;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.entry.display.handler.ReadBoxSelectionHandler;
import org.jbei.ice.client.entry.display.model.PermissionSuggestOracle;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * Widget for displaying/adding/removing part permissions
 *
 * @author Hector Plahar
 */
public class PartPermissionWidget extends Composite implements PermissionPresenter.IPermissionView {

    private FlexTable layout;
    private final PermissionPresenter presenter;
    private FlexTable readList;
    private FlexTable writeList;
    private HTML addReadPermission;
    private HTML addWritePermission;
    private HTML makePublic;
    private SuggestBox permissionSuggestions;
    private boolean isViewingWriteTab;
    private final ServiceDelegate<Boolean> removeAddPublicAccess;
    private boolean publicReadEnabled;

    public PartPermissionWidget(ServiceDelegate<Boolean> removeAddPublicAccess) {
        layout = new FlexTable();
        initWidget(layout);

        initComponents();

        this.removeAddPublicAccess = removeAddPublicAccess;

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_attribute");

        layout.setHTML(0, 0, "<i class=\"" + FAIconType.SHIELD.getStyleName() + " font-85em\"></i> &nbsp;Permissions");
        layout.getCellFormatter().setStyleName(0, 0, "entry_attributes_sub_header");
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);

        HTMLPanel readLabelPanel = new HTMLPanel(
                "Can Read <span style=\"float: right\" id=\"permission_read_add\"></span>");
        readLabelPanel.add(addReadPermission, "permission_read_add");
        readLabelPanel.setStyleName("permission_tab_active");
        layout.setWidget(1, 0, readLabelPanel);

        HTMLPanel writeLabelPanel = new HTMLPanel(
                "Can Edit <span style=\"float: right\" id=\"permission_write_add\"></span>");
        writeLabelPanel.add(addWritePermission, "permission_write_add");
        writeLabelPanel.setStyleName("permission_tab_inactive");
        layout.setWidget(1, 1, writeLabelPanel);

        layout.addClickHandler(new LayoutHeaderClickHandler(readLabelPanel, writeLabelPanel));

        // input suggest box for adding permissions
        createSuggestWidget();

        // handlers for the "+" icon used to show/hide the icons as well as make public
        setAddClickHandlers();

        // contents go here
        layout.setWidget(3, 0, readList);
        layout.getFlexCellFormatter().setColSpan(3, 0, 2);

        // footer
        layout.setWidget(4, 0, makePublic);
        layout.getFlexCellFormatter().setColSpan(4, 0, 2);
        layout.getFlexCellFormatter().setStyleName(4, 0, "permission_footer");

        presenter = new PermissionPresenter(this);
    }

    protected void initComponents() {
        readList = new FlexTable();
        readList.setCellPadding(3);
        readList.setCellSpacing(0);
        readList.setStyleName("permission_list");

        writeList = new FlexTable();
        writeList.setCellPadding(3);
        writeList.setCellSpacing(0);
        writeList.setStyleName("permission_list");

        addReadPermission = new HTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
        addReadPermission.addStyleName("edit_icon");
        addReadPermission.addStyleName("font-12em");

        addWritePermission = new HTML("<b>0</b>");
        addWritePermission.addStyleName("edit_icon");
        addWritePermission.addStyleName("font-12em");

        makePublic = new HTML("<i class=\"" + FAIconType.GLOBE.getStyleName() + "\"></i> Enable Public Read Access");
        makePublic.setStyleName("permission_footer_link");

        permissionSuggestions = new SuggestBox(new PermissionSuggestOracle());
        permissionSuggestions.setWidth("160px");
        permissionSuggestions.setStyleName("permission_input_suggest");
        permissionSuggestions.getValueBox().getElement().setAttribute("placeHolder", "Enter user/group name");
        permissionSuggestions.setLimit(7);

        // add read list public read access and hide by default
        String iconStyle = FAIconType.GLOBE.getStyleName() + " blue";
        readList.setHTML(0, 0, "<i class=\"" + iconStyle + "\"></i> Public");
        Icon deleteIcon = new Icon(FAIconType.REMOVE);
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeAddPublicAccess.execute(true);
            }
        });
        deleteIcon.addStyleName("delete_icon");
        readList.setWidget(0, 1, deleteIcon);
    }

    protected void createSuggestWidget() {
        HTML deleteIcon = new HTML("<i class=\"delete_icon " + FAIconType.REMOVE.getStyleName() + "\"></i>");
        deleteIcon.setStyleName("display-inline");
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                layout.getRowFormatter().setVisible(2, false);
            }
        });
        HTMLPanel permissionAddPanel = new HTMLPanel("<span id=\"p_suggest_box\"></span> "
                                                             + "<span id=\"suggest_cancel\"></span>");
        permissionAddPanel.add(permissionSuggestions, "p_suggest_box");
        permissionAddPanel.add(deleteIcon, "suggest_cancel");
        layout.setWidget(2, 0, permissionAddPanel);
        layout.getFlexCellFormatter().setColSpan(2, 0, 2);
        layout.getRowFormatter().setVisible(2, false);
    }

    public PermissionPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public void setPermissionBoxVisibility(boolean visible) {
        layout.getRowFormatter().setVisible(2, visible);
        if (visible) {
            permissionSuggestions.setText("");
            permissionSuggestions.getValueBox().setFocus(true);
        }
    }

    public void setAddClickHandlers() {
        addReadPermission.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isViewingWriteTab)
                    return;
                layout.getRowFormatter().setVisible(2, (!layout.getRowFormatter().isVisible(2)));
            }
        });
        addWritePermission.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!isViewingWriteTab)
                    return;
                layout.getRowFormatter().setVisible(2, (!layout.getRowFormatter().isVisible(2)));
            }
        });
        makePublic.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeAddPublicAccess.execute(false);
            }
        });
    }

    @Override
    public HandlerRegistration addPermissionBoxSelectionHandler(final ServiceDelegate<AccessPermission> handler) {
        return permissionSuggestions.addSelectionHandler(new ReadBoxSelectionHandler() {
            @Override
            public void updatePermission(AccessPermission access) {
                if (isViewingWriteTab)
                    access.setType(AccessPermission.Type.WRITE_ENTRY);
                else
                    access.setType(AccessPermission.Type.READ_ENTRY);
                handler.execute(access);
            }
        });
    }

    @Override
    public void addWriteItem(final AccessPermission item, final Delegate<AccessPermission> deleteDelegate) {
        addPermissionItem(writeList, item, deleteDelegate);
        if (isViewingWriteTab)
            addReadPermission.setHTML("<b>" + readList.getRowCount() + "</b>");
        else
            addWritePermission.setHTML("<b>" + writeList.getRowCount() + "</b>");
    }

    @Override
    public void addReadItem(final AccessPermission item, final Delegate<AccessPermission> deleteDelegate) {
        addPermissionItem(readList, item, deleteDelegate);
        if (isViewingWriteTab)
            addReadPermission.setHTML("<span>" + readList.getRowCount() + "</span>");
        else
            addWritePermission.setHTML("<span>" + writeList.getRowCount() + "</span>");
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

    @Override
    public void removeReadItem(AccessPermission item) {
        removePermissionItem(readList, item);
    }

    @Override
    public void removeWriteItem(AccessPermission item) {
        removePermissionItem(writeList, item);
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

    @Override
    public void resetPermissionDisplay() {
        writeList.removeAllRows();
        for (int i = 1; i < readList.getRowCount(); i += 1)
            readList.removeRow(i);
    }

    @Override
    public void setWidgetVisibility(boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void showPublicReadAccess(boolean publicReadAccess) {
        layout.getFlexCellFormatter().setVisible(4, 0, !publicReadAccess);
        readList.getRowFormatter().setVisible(0, publicReadAccess);
        publicReadEnabled = publicReadAccess;
    }

    /**
     * ClickHandler for Permissions header
     */
    private class LayoutHeaderClickHandler implements ClickHandler {

        private HTMLPanel readLabelPanel;
        private HTMLPanel writeLabelPanel;

        public LayoutHeaderClickHandler(HTMLPanel readPanel, HTMLPanel writePanel) {
            this.readLabelPanel = readPanel;
            this.writeLabelPanel = writePanel;
        }

        @Override
        public void onClick(ClickEvent event) {
            HTMLTable.Cell cell = layout.getCellForEvent(event);
            if (cell.getRowIndex() != 1)
                return;

            if (isViewingWriteTab) {
                if (cell.getCellIndex() == 1)
                    return;

                // switch to read tab
                isViewingWriteTab = false;
                readLabelPanel.setStyleName("permission_tab_active");
                writeLabelPanel.setStyleName("permission_tab_inactive");
                layout.setWidget(3, 0, readList);
                addWritePermission.setHTML("<span>" + writeList.getRowCount() + "</span>");
                addReadPermission.setHTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
            } else {
                if (cell.getCellIndex() == 0)
                    return;

                // switch to write tab
                isViewingWriteTab = true;
                readLabelPanel.setStyleName("permission_tab_inactive");
                writeLabelPanel.setStyleName("permission_tab_active");
                layout.setWidget(3, 0, writeList);
                int readCount = publicReadEnabled ? readList.getRowCount() : readList.getRowCount() - 1;
                addReadPermission.setHTML("<span>" + readCount + "</span>");
                addWritePermission.setHTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
            }

            layout.getRowFormatter().setVisible(4, !isViewingWriteTab);
        }
    }
}
