package org.jbei.ice.client.entry.display.view;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * @author Hector Plahar
 */
public class PermissionPresenter {

    public interface IPermissionView {

        void setPermissionBoxVisibility(boolean visible);

        HandlerRegistration addPermissionBoxSelectionHandler(ServiceDelegate<PermissionInfo> handler);

        void addWriteItem(PermissionInfo item, Delegate<PermissionInfo> deleteDelegate);

        void addReadItem(PermissionInfo item, Delegate<PermissionInfo> deleteDelegate);

        void removeReadItem(PermissionInfo item);

        void removeWriteItem(PermissionInfo item);

        void resetPermissionDisplay();

        void setWidgetVisibility(boolean visible);
    }

    private final IPermissionView view;
    private boolean canEdit;
    private final ArrayList<PermissionInfo> readList; // list of read permissions (includes groups)
    private final ArrayList<PermissionInfo> writeList; // list of write permissions (includes groups)

    public PermissionPresenter(final IPermissionView view) {
        this.view = view;
        readList = new ArrayList<PermissionInfo>();
        writeList = new ArrayList<PermissionInfo>();
        this.view.setWidgetVisibility(false);
    }

    public void setPermissionAddSelectionHandler(ServiceDelegate<PermissionInfo> delegate) {
        view.addPermissionBoxSelectionHandler(delegate);
    }

    public void removeItem(PermissionInfo info) {
        if (info.isCanRead())
            view.removeReadItem(info);
        else if (info.isCanWrite())
            view.removeWriteItem(info);
    }

    public void addReadItem(PermissionInfo info, Delegate<PermissionInfo> deleteHandler) {
        view.setPermissionBoxVisibility(false);
        if (inReadList(info))
            return;

        readList.add(info);
        if (!canEdit) {
            view.addReadItem(info, null);
            return;
        }
        view.addReadItem(info, deleteHandler);
    }

    public void addWriteItem(PermissionInfo info, Delegate<PermissionInfo> deleteHandler) {
        view.setPermissionBoxVisibility(false);
        if (inWriteList(info))
            return;

        writeList.add(info);
        if (!canEdit) {
            view.addWriteItem(info, null);
            return;
        }
        view.addWriteItem(info, deleteHandler);
    }

    private boolean inWriteList(PermissionInfo info) {
        for (PermissionInfo permission : writeList) {
            if (info.getArticleId() == permission.getArticleId() && info.getType() == permission.getType())
                return true;
        }
        return false;
    }

    private boolean inReadList(PermissionInfo info) {
        for (PermissionInfo permission : readList) {
            if (info.getArticleId() == permission.getArticleId() && info.getType() == permission.getType())
                return true;
        }
        return false;
    }

    public void reset() {
        view.resetPermissionDisplay();
    }

    public void setPermissionData(ArrayList<PermissionInfo> infoList, Delegate<PermissionInfo> deleteHandler) {
        if (infoList == null)
            return;

        view.resetPermissionDisplay();
        writeList.clear();
        readList.clear();

        for (PermissionInfo info : infoList) {
            // skip displaying permissions assigned to self
            if (info.getArticle() == PermissionInfo.Article.ACCOUNT
                    && info.getArticleId() == ClientController.account.getId())
                continue;

            if (info.isCanWrite()) {
                addWriteItem(info, deleteHandler);
            } else if (info.isCanRead()) {
                addReadItem(info, deleteHandler);
            }
        }
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        this.view.setWidgetVisibility(canEdit);
    }
}
