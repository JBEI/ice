package org.jbei.ice.client.entry.display.view;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Presenter for {@link PartPermissionWidget}
 *
 * @author Hector Plahar
 */
public class PermissionPresenter {

    public interface IPermissionView {

        void setPermissionBoxVisibility(boolean visible);

        HandlerRegistration addPermissionBoxSelectionHandler(ServiceDelegate<AccessPermission> handler);

        void addWriteItem(AccessPermission item, Delegate<AccessPermission> deleteDelegate);

        void addReadItem(AccessPermission item, Delegate<AccessPermission> deleteDelegate);

        void removeReadItem(AccessPermission item);

        void removeWriteItem(AccessPermission item);

        void resetPermissionDisplay();

        void showPublicReadAccess(boolean publicReadAccess);

        void setWidgetVisibility(boolean visible);
    }

    private final IPermissionView view;
    private boolean canEdit;
    private final ArrayList<AccessPermission> readList; // list of read permissions (includes groups)
    private final ArrayList<AccessPermission> writeList; // list of write permissions (includes groups)

    public PermissionPresenter(final IPermissionView view) {
        this.view = view;
        readList = new ArrayList<AccessPermission>();
        writeList = new ArrayList<AccessPermission>();
        this.view.setWidgetVisibility(false);
    }

    public void setPermissionAddSelectionHandler(ServiceDelegate<AccessPermission> delegate) {
        view.addPermissionBoxSelectionHandler(delegate);
    }

    public void removeItem(AccessPermission access) {
        if (access.isCanRead())
            view.removeReadItem(access);
        else if (access.isCanWrite())
            view.removeWriteItem(access);
    }

    public void addReadItem(AccessPermission access, Delegate<AccessPermission> deleteHandler) {
        view.setPermissionBoxVisibility(false);
        if (inReadList(access))
            return;

        readList.add(access);
        if (!canEdit) {
            view.addReadItem(access, null);
            return;
        }
        view.addReadItem(access, deleteHandler);
    }

    public void addWriteItem(AccessPermission access, Delegate<AccessPermission> deleteHandler) {
        view.setPermissionBoxVisibility(false);
        if (inWriteList(access))
            return;

        writeList.add(access);
        if (!canEdit) {
            view.addWriteItem(access, null);
            return;
        }
        view.addWriteItem(access, deleteHandler);
    }

    private boolean inWriteList(AccessPermission access) {
        for (AccessPermission accessPermission : writeList) {
            if (access.getArticleId() == accessPermission.getArticleId() && access.getType() == accessPermission
                    .getType())
                return true;
        }
        return false;
    }

    private boolean inReadList(AccessPermission access) {
        for (AccessPermission accessPermission : readList) {
            if (access.getArticleId() == accessPermission.getArticleId() && access.getType() == accessPermission
                    .getType())
                return true;
        }
        return false;
    }

    public void reset() {
        view.resetPermissionDisplay();
    }

    public void setPermissionData(ArrayList<AccessPermission> listAccess, Delegate<AccessPermission> deleteHandler) {
        if (listAccess == null)
            return;

        view.resetPermissionDisplay();
        writeList.clear();
        readList.clear();

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

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        this.view.setWidgetVisibility(canEdit);
    }

    public void setPublicReadAccess(boolean publicAccess) {
        view.showPublicReadAccess(publicAccess);
    }
}
