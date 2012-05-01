package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.entry.view.DeletePermissionHandler;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SuggestOracle;

public class PermissionsPresenter {

    public interface IPermissionsView {

        void setWriteBoxVisibility(boolean visible);

        void setReadBoxVisibility(boolean visible);

        HandlerRegistration addReadBoxSelectionHandler(
                SelectionHandler<SuggestOracle.Suggestion> handler);

        HandlerRegistration addWriteBoxSelectionHandler(
                SelectionHandler<SuggestOracle.Suggestion> handler);

        HandlerRegistration setReadAddClickHandler(ClickHandler handler);

        HandlerRegistration setWriteAddClickHandler(ClickHandler handler);

        void addWriteItem(PermissionItem item, ClickHandler deleteHandler);

        void addReadItem(PermissionItem item, ClickHandler deleteHandler);

        void removeReadItem(PermissionItem item);

        void removeWriteItem(PermissionItem item);

        void resetPermissionDisplay();
    }

    private final IPermissionsView view;
    private boolean canEdit;

    public PermissionsPresenter(final IPermissionsView view) {
        this.view = view;
        this.view.setReadAddClickHandler(new ReadAddHandler());
        this.view.setWriteAddClickHandler(new WriteAddHandler());
    }

    public void onErrRetrievingExistingPermissions() {
        // TODO :
    }

    public void setReadAddSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        view.addReadBoxSelectionHandler(handler);
    }

    public void setWriteAddSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        view.addWriteBoxSelectionHandler(handler);
    }

    public void addReadItem(PermissionInfo info, RegistryServiceAsync service, long entryId,
            boolean canEdit) {
        boolean isGroup = (info.getType() == PermissionType.READ_GROUP || info.getType() == PermissionType.WRITE_GROUP);
        boolean isWrite = (info.getType() == PermissionType.WRITE_GROUP || info.getType() == PermissionType.WRITE_ACCOUNT);
        PermissionItem item = new PermissionItem(info.getId(), info.getDisplay(), isGroup, isWrite);
        DeletePermissionHandler handler = null;
        if (canEdit)
            handler = new DeletePermissionHandler(service, info, entryId,
                    new DeletePermissionCallback());
        view.addReadItem(item, handler);
        view.setReadBoxVisibility(false);
    }

    public void addWriteItem(PermissionInfo info, RegistryServiceAsync service, long entryId,
            boolean canEdit) {
        boolean isGroup = (info.getType() == PermissionType.READ_GROUP || info.getType() == PermissionType.WRITE_GROUP);
        boolean isWrite = (info.getType() == PermissionType.WRITE_GROUP || info.getType() == PermissionType.WRITE_ACCOUNT);
        PermissionItem item = new PermissionItem(info.getId(), info.getDisplay(), isGroup, isWrite);
        DeletePermissionHandler handler = null;
        if (canEdit)
            handler = new DeletePermissionHandler(service, info, entryId,
                    new DeletePermissionCallback());
        view.addWriteItem(item, handler);
        view.setWriteBoxVisibility(false);
        addReadItem(info, service, entryId, canEdit);
    }

    public void setPermissionData(ArrayList<PermissionInfo> infoList, RegistryServiceAsync service,
            long entryId) {
        if (infoList == null)
            return;

        view.resetPermissionDisplay();

        ArrayList<PermissionItem> itemList = new ArrayList<PermissionItem>();

        for (PermissionInfo info : infoList) {
            PermissionItem item = null;
            DeletePermissionHandler handler = null;
            if (isCanEdit())
                handler = new DeletePermissionHandler(service, info, entryId,
                        new DeletePermissionCallback());

            switch (info.getType()) {
            case READ_ACCOUNT:
                item = new PermissionItem(info.getId(), info.getDisplay(), false, false);
                view.addReadItem(item, handler);
                break;

            case READ_GROUP:
                item = new PermissionItem(info.getId(), info.getDisplay(), true, false);
                view.addReadItem(item, handler);
                break;

            case WRITE_ACCOUNT:
                item = new PermissionItem(info.getId(), info.getDisplay(), false, true);
                view.addWriteItem(item, handler);
                break;

            case WRITE_GROUP:
                item = new PermissionItem(info.getId(), info.getDisplay(), true, true);
                view.addWriteItem(item, handler);
                break;
            }

            if (item != null)
                itemList.add(item);
        }
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    private class DeletePermissionCallback extends Callback<PermissionInfo> {

        @Override
        public void onSucess(PermissionInfo info) {

            PermissionItem item = null;

            switch (info.getType()) {
            case READ_ACCOUNT:
                item = new PermissionItem(info.getId(), info.getDisplay(), false, false);
                view.removeReadItem(item);
                break;

            case READ_GROUP:
                item = new PermissionItem(info.getId(), info.getDisplay(), true, false);
                view.removeReadItem(item);
                break;

            case WRITE_ACCOUNT:
                item = new PermissionItem(info.getId(), info.getDisplay(), false, true);
                view.removeWriteItem(item);
                view.removeReadItem(item);
                break;

            case WRITE_GROUP:
                item = new PermissionItem(info.getId(), info.getDisplay(), true, true);
                view.removeWriteItem(item);
                view.removeReadItem(item);
                break;
            }
        }

        @Override
        public void onFailure() {
            // TODO Auto-generated method stub
        }
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
