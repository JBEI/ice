package org.jbei.ice.client.collection.widget;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.model.PropagateOption;
import org.jbei.ice.client.collection.model.ShareCollectionData;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.entry.display.handler.ReadBoxSelectionHandler;
import org.jbei.ice.client.entry.display.model.PermissionSuggestOracle;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
    private FlexTable permissionLayout;
    private HTML addReadPermission;
    private HTML addWritePermission;
    private HTML makePublic;
    private SuggestBox permissionSuggestions;
    private Delegate<ShareCollectionData> delegate;
    private Callback<ShareCollectionData> callback;
    private ServiceDelegate<Boolean> publicAccessDelegate;

    private boolean isViewingWriteTab;
    private final ArrayList<AccessPermission> readList;  // list of read permissions (includes groups)
    private final ArrayList<AccessPermission> writeList; // list of write permissions (includes groups)
    private final CheckBox propagateBox;
    private boolean isPublicReadEnabled;
    private final long folderId;

    private static final String groupIconStyle = FAIconType.GROUP.getStyleName() + " permission_group";
    private static final String profileIconStyle = FAIconType.USER.getStyleName() + " permission_user";

    public CollectionPermissionWidget(Delegate<ShareCollectionData> permissionDelegate,
            final ServiceDelegate<PropagateOption> propagate, Callback<ShareCollectionData> callback,
            final long folderId) {
        final FlexTable layout = new FlexTable();
        initWidget(layout);
        readList = new ArrayList<AccessPermission>();
        writeList = new ArrayList<AccessPermission>();
        this.delegate = permissionDelegate;
        this.callback = callback;

        initComponents();

        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        this.folderId = folderId;

        HTMLPanel panel = new HTMLPanel("<span id=\"propagate_checkbox\"></span> <span id=\"propagate_help\"></span>");
        String html = "<span class=\"font-80em\" style=\"color: #555\">Propagate permissions to parts</span>";
        propagateBox = new CheckBox(SafeHtmlUtils.fromSafeConstant(html));
        panel.add(propagateBox, "propagate_checkbox");
        propagateBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                propagate.execute(new PropagateOption(event.getValue(), folderId));
            }
        });

        // help icon to show information about what the propagate option does
        Icon helpIcon = new Icon(FAIconType.QUESTION_SIGN);
        helpIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                layout.getRowFormatter().setVisible(1, !layout.getRowFormatter().isVisible(1));
            }
        });
        helpIcon.removeStyleName("font-awesome");
        helpIcon.addStyleName("cursor_pointer");
        helpIcon.addStyleName("font-80em");
        panel.add(helpIcon, "propagate_help");
        layout.setWidget(0, 0, panel);

        // description on what propagating means
        String propagateDesc = "<p style=\"padding: 6px; border: 1px solid #bce8f1; background-color: #d9edf7;" +
                "font-size: 0.80em; border-radius: 6px; width: 89%; color: #3a87ad\">" +
                "Enabling this option allows permissions assigned to the folder to also be" +
                " set on the individual parts contained in it. This means that when the folder is deleted, the set" +
                " permissions are maintained at the part level.</p>";
        layout.setHTML(1, 0, propagateDesc);
        layout.getRowFormatter().setVisible(1, false);

        permissionLayout = createPermissionWidget();
        layout.setWidget(2, 0, permissionLayout);
        permissionLayout.getRowFormatter().setVisible(3, false);

        // input suggest box for adding permissions
        createSuggestWidget();
        setMakePublicHandler();
    }

    public void setPublicAccessDelegate(ServiceDelegate<Boolean> delegate) {
        publicAccessDelegate = delegate;
        permissionLayout.getRowFormatter().setVisible(3, (publicAccessDelegate != null));
    }

    public void reset() {
        propagateBox.setValue(false);
        readList.clear();
        writeList.clear();
        for (int i = 1; i < readListTable.getRowCount(); i += 1)
            readListTable.removeRow(i);
        writeListTable.removeAllRows();
    }

    protected void setMakePublicHandler() {
        makePublic.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean visible = permissionLayout.getRowFormatter().isVisible(3);
                if (!visible)
                    return;

                enablePublicAccess(true);
            }
        });
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
        LayoutHeaderClickHandler handler = new LayoutHeaderClickHandler(readLabelPanel, writeLabelPanel);
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

    protected void createSuggestWidget() {
        HTML deleteIcon = new HTML("<i class=\"delete_icon " + FAIconType.REMOVE.getStyleName() + "\"></i>");
        deleteIcon.setStyleName("display-inline");
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                permissionLayout.getRowFormatter().setVisible(1, false);
            }
        });

        HTMLPanel permissionAddPanel = new HTMLPanel("<span id=\"p_suggest_box\"></span> "
                                                             + "<span id=\"suggest_cancel\"></span>");
        permissionAddPanel.add(permissionSuggestions, "p_suggest_box");
        permissionAddPanel.add(deleteIcon, "suggest_cancel");
        permissionLayout.setWidget(1, 0, permissionAddPanel);
        permissionLayout.getFlexCellFormatter().setColSpan(1, 0, 2);
        permissionLayout.getRowFormatter().setVisible(1, false);
    }

    /**
     * shows/hides footer for "make public" as well as the icon
     * that indicates the permissions in the read table
     *
     * @param publicReadAccess whether to enable or disable public read access
     */
    protected void enablePublicAccess(boolean publicReadAccess) {
        permissionLayout.getFlexCellFormatter().setVisible(3, 0, !publicReadAccess);
        readListTable.getRowFormatter().setVisible(0, publicReadAccess);
        isPublicReadEnabled = publicReadAccess;
        publicAccessDelegate.execute(publicReadAccess);
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

        makePublic = new HTML("<i class=\"" + FAIconType.GLOBE.getStyleName() + "\"></i> Enable Public Read Access");
        makePublic.setStyleName("permission_footer_link");

        permissionSuggestions = new SuggestBox(new PermissionSuggestOracle());
        permissionSuggestions.setWidth("45%");
        permissionSuggestions.getValueBox().getElement().setAttribute("placeHolder", "Enter user/group name");
        permissionSuggestions.setLimit(7);

        // add read list public read access and hide by default
        String iconStyle = FAIconType.GLOBE.getStyleName() + " blue";
        readListTable.setHTML(0, 0, "<i class=\"" + iconStyle + "\"></i> Public");
        Icon deleteIcon = new Icon(FAIconType.REMOVE);
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                enablePublicAccess(false);
            }
        });
        deleteIcon.addStyleName("delete_icon");
        readListTable.setWidget(0, 1, deleteIcon);
        readListTable.getRowFormatter().setVisible(0, false);

        // handler for when user adds a permission on the ui
        permissionSuggestions.addSelectionHandler(new ReadBoxSelectionHandler() {
            @Override
            public void updatePermission(AccessPermission access) {
                access.setTypeId(folderId);
                if (isViewingWriteTab) {
                    access.setType(AccessPermission.Type.WRITE_FOLDER);
                    addWriteItem(access);
                } else {
                    access.setType(AccessPermission.Type.READ_FOLDER);
                    addReadItem(access);
                }

                permissionSuggestions.getValueBox().setText("");
                permissionSuggestions.getValueBox().setFocus(true);
                ShareCollectionData data = new ShareCollectionData(access, false, callback);
                delegate.execute(data);
            }
        });
    }

    public void addWriteItem(final AccessPermission item) {
        if (writeList.contains(item))
            return;

        writeList.add(item);
        addPermissionItem(writeListTable, item);
        if (isViewingWriteTab)
            addReadPermission.setHTML("<b>" + readList.size() + "</b>");
        else
            addWritePermission.setHTML("<b>" + writeList.size() + "</b>");
    }

    public void addReadItem(final AccessPermission item) {
        if (readList.contains(item))
            return;

        readList.add(item);
        addPermissionItem(readListTable, item);
        if (isViewingWriteTab)
            addReadPermission.setHTML("<span>" + readList.size() + "</span>");
        else
            addWritePermission.setHTML("<span>" + writeList.size() + "</span>");
    }

    protected void addPermissionItem(FlexTable table, final AccessPermission item) {
        int row = table.getRowCount();
        String iconStyle;
        String display;

        if (item.getArticle() == AccessPermission.Article.GROUP) {
            iconStyle = groupIconStyle;
            display = item.getDisplay();
        } else {
            iconStyle = profileIconStyle;
            display = "<a href=\"#" + Page.PROFILE.getLink() + ";id="
                    + item.getArticleId() + "\">" + item.getDisplay() + "</a>";
        }

        table.setHTML(row, 0, "<i class=\"" + iconStyle + "\"></i> " + display);
        table.getCellFormatter().setWidth(row, 0, "160px");
        Icon deleteIcon = new Icon(FAIconType.REMOVE);
        deleteIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.execute(new ShareCollectionData(item, true, callback));
            }
        });

        deleteIcon.addStyleName("delete_icon");
        table.setWidget(row, 1, deleteIcon);
    }

    public void removeItem(AccessPermission access) {
        if (access.isCanRead()) {
            removePermission(access, readListTable, 1);
            readList.remove(access);
        } else if (access.isCanWrite()) {
            removePermission(access, writeListTable, 0);
            writeList.remove(access);
        }
    }

    private void removePermission(AccessPermission item, FlexTable table, int rowStart) {
        for (int i = rowStart; i < table.getRowCount(); i += 1) {
            String html = table.getHTML(i, 0);
            if (item.getArticle() == AccessPermission.Article.GROUP) {
                if (html.contains(groupIconStyle) & html.contains(item.getDisplay())) {
                    table.removeRow(i);
                    break;
                }
                break;
            } else if (item.getArticle() == AccessPermission.Article.ACCOUNT) {
                if (html.contains(Page.PROFILE.getLink() + ";id=" + item.getArticleId())) {
                    table.removeRow(i);
                    break;
                }
            }
        }
    }

    public void setPermissionData(ArrayList<AccessPermission> listAccess) {
        if (listAccess == null)
            return;

        reset();

        for (AccessPermission access : listAccess) {
            // skip displaying permissions assigned to self
            if (access.getArticle() == AccessPermission.Article.ACCOUNT
                    && access.getArticleId() == ClientController.account.getId())
                continue;

            if (access.isCanWrite()) {
                addWriteItem(access);
            } else if (access.isCanRead()) {
                addReadItem(access);
            }
        }
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
            HTMLTable.Cell cell = permissionLayout.getCellForEvent(event);
            if (cell.getRowIndex() != 0)
                return;

            if (isViewingWriteTab) {
                if (cell.getCellIndex() == 1)
                    return;

                // switch to read tab
                isViewingWriteTab = false;
                readLabelPanel.setStyleName("permission_tab_active");
                writeLabelPanel.setStyleName("permission_tab_inactive");
                permissionLayout.setWidget(2, 0, readListTable);
                addWritePermission.setHTML("<span>" + writeList.size() + "</span>");
                addReadPermission.setHTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
            } else {
                if (cell.getCellIndex() == 0)
                    return;

                // switch to write tab
                isViewingWriteTab = true;
                readLabelPanel.setStyleName("permission_tab_inactive");
                writeLabelPanel.setStyleName("permission_tab_active");
                permissionLayout.setWidget(2, 0, writeListTable);
                int readSize = isPublicReadEnabled ? readList.size() + 1 : readList.size();
                addReadPermission.setHTML("<span>" + readSize + "</span>");
                addWritePermission.setHTML("<i class=\"" + FAIconType.PLUS_SIGN.getStyleName() + "\"></i>");
            }

            permissionLayout.getRowFormatter().setVisible(3, !isViewingWriteTab);
        }
    }
}
