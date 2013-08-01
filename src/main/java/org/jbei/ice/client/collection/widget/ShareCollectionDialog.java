package org.jbei.ice.client.collection.widget;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog widget for sharing collections
 *
 * @author Hector Plahar
 */
public class ShareCollectionDialog extends Composite {

    private final String collectionName;
    private HTML close;
    private PopupPanel box;
    private final CollectionPermissionWidget permissionsWidget;
    private final Callback<AccessPermission> addCallback;
    private final Callback<AccessPermission> removeCallback;
    private final Delegate<AccessPermission> deleteDelegate;
    private int userShareCount;
    private int groupShareCount;

    public ShareCollectionDialog(final CollectionMenu.MenuCell cell, String collectionName,
            final Delegate<AccessPermission> delegate) {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        layout.setStyleName("add_to_popup");
        layout.addStyleName("pad-4");
        layout.addStyleName("bg_white");

        this.collectionName = collectionName;
        initComponents();

        this.deleteDelegate = delegate;

        // set Widgets
        layout.setWidget(0, 0, createHeader());

        permissionsWidget = new CollectionPermissionWidget();
        permissionsWidget.setVisible(true);
        layout.setWidget(1, 0, permissionsWidget);

        addCallback = new Callback<AccessPermission>() {
            @Override
            public void onSuccess(AccessPermission access) {
                if (access.isCanWrite()) {
                    permissionsWidget.addWriteItem(access, delegate);
                } else if (access.isCanRead()) {
                    permissionsWidget.addReadItem(access, delegate);
                }

                if (access.getArticle() == AccessPermission.Article.ACCOUNT)
                    userShareCount += 1;
                else
                    groupShareCount += 1;

                cell.setShared(userShareCount, groupShareCount);
            }

            @Override
            public void onFailure() {
            }
        };

        removeCallback = new Callback<AccessPermission>() {
            @Override
            public void onSuccess(AccessPermission accessPermission) {
                permissionsWidget.removeItem(accessPermission);
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT)
                    userShareCount -= 1;
                else
                    groupShareCount -= 1;

                cell.setShared(userShareCount, groupShareCount);
            }

            @Override
            public void onFailure() {
            }
        };
    }

    public Callback<AccessPermission> getAddCallback() {
        return this.addCallback;
    }

    public Callback<AccessPermission> getRemoveCallback() {
        return this.removeCallback;
    }

    protected Widget createHeader() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        String shareHTML = "<b class=\"font-85em\" style=\"color: #c1c1c1\">SHARE</b> "
                + "<b font-style=\"italic\">" + collectionName + "</b> "
                + "<b class=\"font-85em\" style=\"color: #c1c1c1\">COLLECTION</b>";

        table.setHTML(0, 0, shareHTML);
        table.setWidget(0, 1, close);
        table.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);
        return table;
    }

    protected void initComponents() {
        ClickHandler closeHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                box.hide();
            }
        };

        close = new HTML("<i class=\"" + FAIconType.REMOVE_SIGN.getStyleName() + "\"></i> Close");
        close.setStyleName("opacity_hover");
        close.addStyleName("font-75em");
        close.addClickHandler(closeHandler);

        box = new PopupPanel();
        box.setWidth("600px");
        box.setModal(true);
        box.setGlassEnabled(true);
        box.setGlassStyleName("dialog_box_glass");
        box.setWidget(this);
    }

    public void showDialog(ArrayList<AccessPermission> accessPermissions) {
        permissionsWidget.setPermissionData(accessPermissions, deleteDelegate);
        box.center();
    }
}
