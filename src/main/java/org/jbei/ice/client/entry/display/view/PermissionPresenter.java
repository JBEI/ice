package org.jbei.ice.client.entry.display.view;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * @author Hector Plahar
 */
public class PermissionPresenter {

    public interface IPermissionView {

        void setWriteBoxVisibility(boolean visible);

        void setReadBoxVisibility(boolean visible);

        HandlerRegistration addReadBoxSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler);

        HandlerRegistration addWriteBoxSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler);

        HandlerRegistration setReadAddClickHandler(ClickHandler handler);

        HandlerRegistration setWriteAddClickHandler(ClickHandler handler);

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
        this.view.setReadAddClickHandler(new ReadAddHandler());
        this.view.setWriteAddClickHandler(new WriteAddHandler());
        readList = new ArrayList<PermissionInfo>();
        writeList = new ArrayList<PermissionInfo>();
        this.view.setWidgetVisibility(false);
    }

    public void setReadAddSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        view.addReadBoxSelectionHandler(handler);
    }

    public void setWriteAddSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        view.addWriteBoxSelectionHandler(handler);
    }

    public void removeItem(PermissionInfo info) {
        if (info.isCanRead())
            view.removeReadItem(info);
        else if (info.isCanWrite())
            view.removeWriteItem(info);
    }

    public void addReadItem(PermissionInfo info, Delegate<PermissionInfo> deleteHandler) {
        view.setReadBoxVisibility(false);
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
        view.setWriteBoxVisibility(false);
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

    // inner classes
    private class WriteAddHandler implements ClickHandler {

        private boolean visible = false;

        @Override
        public void onClick(ClickEvent event) {
            visible = !visible;
            view.setWriteBoxVisibility(visible);
        }
    }

    private class ReadAddHandler implements ClickHandler {

        private boolean visible = false;

        @Override
        public void onClick(ClickEvent event) {
            visible = !visible;
            view.setReadBoxVisibility(visible);
        }
    }
}
