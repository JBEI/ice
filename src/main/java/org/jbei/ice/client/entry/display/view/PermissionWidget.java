package org.jbei.ice.client.entry.display.view;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.entry.display.model.PermissionSuggestOracle;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Widget for displaying/adding/removing entry permissions
 *
 * @author Hector Plahar
 */
public class PermissionWidget extends Composite implements PermissionPresenter.IPermissionView {

    private FlexTable layout;
    private final PermissionPresenter presenter;
    private FlexTable readList;
    private FlexTable writeList;
    private Icon addReadPermission;
    private Icon addWritePermission;
    private HTML makePublic;
    private SuggestBox readSuggestBox;
    private SuggestBox writeSuggestBox;
    private boolean isViewingWriteTab;

    public PermissionWidget() {
        layout = new FlexTable();
        initWidget(layout);

        initComponents();

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_attribute");

        layout.setHTML(0, 0, "<i class=\"" + FAIconType.SHIELD.getStyleName()
                + " font-85em\"></i> &nbsp;Permissions");
        layout.getCellFormatter().setStyleName(0, 0, "entry_attributes_sub_header");
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);

        final HTMLPanel readLabel = new HTMLPanel(
                "Can Read <span style=\"float: right\" id=\"permission_read_add\"></span>");
        readLabel.add(addReadPermission, "permission_read_add");
        readLabel.setStyleName("permission_tab_active");
        layout.setWidget(1, 0, readLabel);

        final HTMLPanel writeLabel = new HTMLPanel(
                "Can Edit <span style=\"float: right\" id=\"permission_write_add\"></span>");
        writeLabel.add(addWritePermission, "permission_write_add");
        writeLabel.setStyleName("permission_tab_inactive");
        layout.setWidget(1, 1, writeLabel);

        layout.addClickHandler(new ClickHandler() {
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
                    readLabel.setStyleName("permission_tab_active");
                    writeLabel.setStyleName("permission_tab_inactive");
                    layout.setWidget(3, 0, readList);
                } else {
                    if (cell.getCellIndex() == 0)
                        return;

                    // switch to write tab
                    isViewingWriteTab = true;
                    readLabel.setStyleName("permission_tab_inactive");
                    writeLabel.setStyleName("permission_tab_active");
                    layout.setWidget(3, 0, writeList);
                }

                layout.getRowFormatter().setVisible(4, !isViewingWriteTab);
                addWritePermission.setVisible(isViewingWriteTab);
                addReadPermission.setVisible(!isViewingWriteTab);
            }
        });

        // placeholder for add input box
        layout.setHTML(2, 0, "");
        layout.getFlexCellFormatter().setColSpan(2, 0, 2);
        layout.getRowFormatter().setVisible(2, false);

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

        addReadPermission = new Icon(FAIconType.PLUS_SIGN);
        addReadPermission.addStyleName("edit_icon");
        addReadPermission.addStyleName("font-12em");
        addWritePermission = new Icon(FAIconType.PLUS_SIGN);
        addWritePermission.addStyleName("edit_icon");
        addWritePermission.addStyleName("font-12em");

        makePublic = new HTML("Enable Public Read Access");
        makePublic.setStyleName("permission_footer_link");

        readSuggestBox = new SuggestBox(new PermissionSuggestOracle());
        readSuggestBox.setWidth("150px");
        readSuggestBox.setStyleName("permission_input_suggest");
        readSuggestBox.getValueBox().getElement().setAttribute("placeHolder", "Enter user/group name");
        readSuggestBox.setLimit(7);

        writeSuggestBox = new SuggestBox(new PermissionSuggestOracle());
        writeSuggestBox.setWidth("150px");
        writeSuggestBox.setStyleName("permission_input_suggest");
        writeSuggestBox.getValueBox().getElement().setAttribute("placeHolder", "Enter user/group name");
        writeSuggestBox.setLimit(7);
    }

    /**
     * Adds links that allows user to modify permissions.
     * User should have write access
     *
     * @param canAdd allow permissions edit
     */
    public void canAddPermission(boolean canAdd) {
        addReadPermission.setVisible(canAdd);
        addWritePermission.setVisible(canAdd);
    }

    public PermissionPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public void setWriteBoxVisibility(boolean visible) {
        layout.getRowFormatter().setVisible(2, visible);
        if (visible) {
            writeSuggestBox.setText("");
            writeSuggestBox.getValueBox().setFocus(true);
            layout.setWidget(2, 0, writeSuggestBox);
        }
    }

    @Override
    public void setReadBoxVisibility(boolean visible) {
        layout.getRowFormatter().setVisible(2, visible);
        if (visible) {
            readSuggestBox.setText("");
            readSuggestBox.getValueBox().setFocus(true);
            layout.setWidget(2, 0, readSuggestBox);
        }
    }

    @Override
    public HandlerRegistration addReadBoxSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        return readSuggestBox.addSelectionHandler(handler);
    }

    @Override
    public HandlerRegistration addWriteBoxSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        return writeSuggestBox.addSelectionHandler(handler);
    }

    @Override
    public HandlerRegistration setReadAddClickHandler(ClickHandler handler) {
        return addReadPermission.addClickHandler(handler);
    }

    @Override
    public HandlerRegistration setWriteAddClickHandler(ClickHandler handler) {
        return addWritePermission.addClickHandler(handler);
    }

    @Override
    public void addWriteItem(final PermissionInfo item, final Delegate<PermissionInfo> deleteDelegate) {
        int row = writeList.getRowCount();
        String iconStyle;
        String display;

        if (item.getArticle() == PermissionInfo.Article.GROUP) {
            iconStyle = FAIconType.GROUP.getStyleName() + " permission_group";
            display = item.getDisplay();
        } else {
            iconStyle = FAIconType.USER.getStyleName() + " permission_user";
            display = "<a href=\"#" + Page.PROFILE.getLink() + ";id="
                    + item.getArticleId() + "\">" + item.getDisplay() + "</a>";
        }

        writeList.setHTML(row, 0, "<i class=\"font-85em " + iconStyle + "\"></i> " + display);
        Icon deleteIcon = new Icon(FAIconType.REMOVE);
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deleteDelegate.execute(item);
            }
        });
        deleteIcon.addStyleName("delete_icon");
        writeList.setWidget(row, 1, deleteIcon);
    }

    @Override
    public void addReadItem(final PermissionInfo item, final Delegate<PermissionInfo> deleteDelegate) {
        int row = readList.getRowCount();
        String iconStyle;
        String display;
        String permissionName = item.getDisplay().length() <= 22 ? item.getDisplay() :
                item.getDisplay().substring(0, 18) + "...";

        if (item.getArticle() == PermissionInfo.Article.GROUP) {
            iconStyle = FAIconType.GROUP.getStyleName() + " permission_group";
            display = permissionName;
        } else {
            iconStyle = FAIconType.USER.getStyleName() + " permission_user";
            display = "<a href=\"#" + Page.PROFILE.getLink() + ";id="
                    + item.getArticleId() + "\">" + permissionName + "</a>";
        }

        readList.setHTML(row, 0, "<i class=\"font-85em " + iconStyle + "\"></i> " + display);
        Icon deleteIcon = new Icon(FAIconType.REMOVE);
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deleteDelegate.execute(item);
            }
        });
        deleteIcon.addStyleName("delete_icon");
        readList.setWidget(row, 1, deleteIcon);
    }

    @Override
    public void removeReadItem(PermissionInfo item) {
        for (int i = 0; i < readList.getRowCount(); i += 1) {
            String html = readList.getHTML(i, 0);
            if (html.contains(Page.PROFILE.getLink() + ";id=" + item.getArticleId())) {
                readList.removeRow(i);
                break;
            }
        }
    }

    @Override
    public void removeWriteItem(PermissionInfo item) {
        for (int i = 0; i < writeList.getRowCount(); i += 1) {
            String html = writeList.getHTML(i, 0);
            if (html.contains(Page.PROFILE.getLink() + ";id=" + item.getArticleId())) {
                writeList.removeRow(i);
                break;
            }
        }
    }

    @Override
    public void resetPermissionDisplay() {
        writeList.removeAllRows();
        readList.removeAllRows();
    }

    @Override
    public void setWidgetVisibility(boolean visible) {
        this.setVisible(visible);
    }
}
