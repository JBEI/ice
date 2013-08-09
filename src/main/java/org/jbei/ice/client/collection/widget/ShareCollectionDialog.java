package org.jbei.ice.client.collection.widget;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.model.PropagateOption;
import org.jbei.ice.client.collection.model.ShareCollectionData;
import org.jbei.ice.client.common.widget.GenericPopup;
import org.jbei.ice.client.common.widget.ICanReset;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.user.client.ui.Composite;

/**
 * Dialog widget for sharing collections
 *
 * @author Hector Plahar
 */
public class ShareCollectionDialog extends Composite implements ICanReset {

    private final CollectionPermissionWidget permissionsWidget;

    private int userShareCount;
    private int groupShareCount;
    private GenericPopup popup;

    public ShareCollectionDialog(final CollectionMenu.MenuCell cell, String collectionName,
            Delegate<ShareCollectionData> delegate, ServiceDelegate<PropagateOption> propagate) {
        String shareHTML = "<b class=\"font-85em\" style=\"color: #c1c1c1\">SHARE</b> "
                + "<b font-style=\"italic\">" + collectionName + "</b> "
                + "<b class=\"font-85em\" style=\"color: #c1c1c1\">COLLECTION</b>";

        // callback that updates the menu in real time
        Callback<ShareCollectionData> callback = new ShareActionCallback(cell);
        permissionsWidget = new CollectionPermissionWidget(delegate, propagate, callback, cell.getMenuItem().getId());

        initWidget(permissionsWidget);
        popup = new GenericPopup(this, shareHTML);
    }

    /**
     * Initializes the share counts used for visual feedback to the user on how
     * many permissions have been set.
     * Displays dialog that allows user to update the permissions
     *
     * @param accessPermissions list of currently set permissions for folder
     */
    public void showDialog(ArrayList<AccessPermission> accessPermissions) {
        userShareCount = 0;
        groupShareCount = 0;

        if (accessPermissions != null) {
            for (AccessPermission permission : accessPermissions) {
                if (permission.getArticle() == AccessPermission.Article.GROUP)
                    groupShareCount += 1;
                else if (permission.getArticle() == AccessPermission.Article.ACCOUNT)
                    userShareCount += 1;
            }
        }
        permissionsWidget.setPermissionData(accessPermissions);
        popup.showDialog();
    }

    @Override
    public void reset() {
        permissionsWidget.reset();
    }

    /**
     * Call back from server response as that was initiated as a result of user action within
     * the share collection dialog e.g. add/remove permission
     */
    private class ShareActionCallback extends Callback<ShareCollectionData> {

        private final CollectionMenu.MenuCell cell;

        public ShareActionCallback(CollectionMenu.MenuCell cell) {
            this.cell = cell;
        }

        @Override
        public void onSuccess(ShareCollectionData data) {
            AccessPermission accessPermission = data.getAccess();

            if (data.isDelete()) {
                permissionsWidget.removeItem(accessPermission);
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT)
                    userShareCount -= 1;
                else
                    groupShareCount -= 1;
            } else {
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT)
                    userShareCount += 1;
                else
                    groupShareCount += 1;
            }
            cell.setShared(userShareCount, groupShareCount);
        }

        @Override
        public void onFailure() {
        }
    }
}
