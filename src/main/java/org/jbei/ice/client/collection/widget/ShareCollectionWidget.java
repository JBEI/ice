package org.jbei.ice.client.collection.widget;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter;
import org.jbei.ice.client.entry.view.view.PermissionsWidget;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog widget for sharing collections
 *
 * @author Hector Plahar
 */
public class ShareCollectionWidget extends Composite {

    private final String collectionName;
    private Button close;
    private Icon closeIcon;
    private PopupPanel box;
    private final PermissionsWidget permissionsWidget;
    private final Callback<PermissionInfo> addCallback;
    private final Callback<PermissionInfo> removeCallback;
    private final Delegate<PermissionInfo> deleteDelegate;

    public ShareCollectionWidget(String collectionName, final Delegate<PermissionInfo> delegate) {
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

        permissionsWidget = new PermissionsWidget();
        permissionsWidget.setWidgetVisibility(true);
        permissionsWidget.getPresenter().setCanEdit(true);
        layout.setWidget(1, 0, permissionsWidget);

        layout.setWidget(2, 0, close);
        layout.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);

        addCallback = new Callback<PermissionInfo>() {
            @Override
            public void onSuccess(PermissionInfo info) {
                if (info.isCanWrite()) {
                    permissionsWidget.getPresenter().addWriteItem(info, delegate);
                } else if (info.isCanRead()) {
                    permissionsWidget.getPresenter().addReadItem(info, delegate);
                }
            }

            @Override
            public void onFailure() {
            }
        };

        removeCallback = new Callback<PermissionInfo>() {
            @Override
            public void onSuccess(PermissionInfo permissionInfo) {
                permissionsWidget.getPresenter().removeItem(permissionInfo);
            }

            @Override
            public void onFailure() {
            }
        };
    }

    public PermissionsPresenter getPermissionsPresenter() {
        return permissionsWidget.getPresenter();
    }

    public Callback<PermissionInfo> getAddCallback() {
        return this.addCallback;
    }

    public Callback<PermissionInfo> getRemoveCallback() {
        return this.removeCallback;
    }

    protected Widget createHeader() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        table.setHTML(0, 0, "Share <b><i>" + collectionName + "</b></i>");
        table.setWidget(0, 1, closeIcon);
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

        close = new Button("<i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i> Close");
        closeIcon = new Icon(FAIconType.REMOVE);
        closeIcon.addClickHandler(closeHandler);
        close.addClickHandler(closeHandler);

        box = new PopupPanel();
        box.setWidth("600px");
        box.setModal(true);
        box.setGlassEnabled(true);
        box.setGlassStyleName("dialog_box_glass");
        box.setWidget(this);
    }

    public void showDialog(ArrayList<PermissionInfo> permissions) {
        permissionsWidget.getPresenter().setPermissionData(permissions, deleteDelegate);
        box.center();
    }
}
