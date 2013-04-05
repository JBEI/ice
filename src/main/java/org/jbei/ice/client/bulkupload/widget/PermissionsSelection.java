package org.jbei.ice.client.bulkupload.widget;

import java.util.HashSet;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.client.entry.view.handler.ReadBoxSelectionHandler;
import org.jbei.ice.client.entry.view.view.PermissionsWidget;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that allows user to select the global readable permissions for
 * groups
 *
 * @author Hector Plahar
 */
public class PermissionsSelection implements IsWidget {

    private final FocusPanel parent;
    private final PermissionsWidget permissionsWidget;
    private final HashSet<PermissionInfo> permissions;

    public PermissionsSelection() {
        Icon icon = new Icon(FAIconType.KEY);
        icon.setTitle("Click to set permissions");
        icon.addStyleName("display-inline");
        icon.removeStyleName("font-awesome");

        HTMLPanel panel = new HTMLPanel("<span id=\"creator_icon\"></span> Permissions");
        panel.add(icon, "creator_icon");
        panel.setStyleName("display-inline");

        parent = new FocusPanel(panel);
        parent.setStyleName("bulk_upload_visibility");
        parent.addStyleName("opacity_hover");

        permissionsWidget = new PermissionsWidget();
        permissionsWidget.setStyleName("bg_fc");
        permissions = new HashSet<PermissionInfo>();
        permissionsWidget.getPresenter().setCanEdit(true);
        permissionsWidget.getPresenter().setReadAddSelectionHandler(new PermissionReadBoxHandler(false));
        permissionsWidget.getPresenter().setWriteAddSelectionHandler(new PermissionReadBoxHandler(true));

        final PopupHandler popUp = new PopupHandler(this.permissionsWidget, icon.getElement(), false);
        popUp.setCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                // TODO : when closing the permission widget
            }
        });
        parent.addClickHandler(popUp);
    }

    @Override
    public Widget asWidget() {
        return parent;
    }

    //
    // inner classes
    //
    private class PermissionReadBoxHandler extends ReadBoxSelectionHandler {

        private final boolean isWrite;

        public PermissionReadBoxHandler(boolean isWrite) {
            this.isWrite = isWrite;
        }

        @Override
        public void updatePermission(final PermissionInfo info) {
            if (isWrite) {
                info.setType(PermissionInfo.Type.WRITE_ENTRY);
            } else
                info.setType(PermissionInfo.Type.READ_ENTRY);

            permissions.add(info);
            displayPermission(info);
        }

        protected void displayPermission(PermissionInfo info) {
            DeletePermission deletePermission = new DeletePermission();
            if (isWrite) {
                permissionsWidget.addWriteItem(info, deletePermission);
            } else {
                permissionsWidget.addReadItem(info, deletePermission);
            }
        }
    }

    private class DeletePermission implements Delegate<PermissionInfo> {

        @Override
        public void execute(final PermissionInfo info) {
            permissions.remove(info);
            permissionsWidget.removeReadItem(info);
        }
    }
}
