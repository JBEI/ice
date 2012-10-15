package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.entry.view.DeletePermissionHandler;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
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

        void setWidgetVisibility(boolean visible);
    }

    private final IPermissionsView view;
    private boolean canEdit;
    private final ArrayList<PermissionInfo> readList; // list of read permissions (includes groups)
    private final ArrayList<PermissionInfo> writeList; // list of write permissions (includes groups)

    public PermissionsPresenter(final IPermissionsView view) {
        this.view = view;
        this.view.setReadAddClickHandler(new ReadAddHandler());
        this.view.setWriteAddClickHandler(new WriteAddHandler());
        readList = new ArrayList<PermissionInfo>();
        writeList = new ArrayList<PermissionInfo>();
        this.view.setWidgetVisibility(false);
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

    public void addReadItem(PermissionInfo info, RegistryServiceAsync service, HandlerManager eventBus, long entryId,
            boolean canEdit) {
        view.setReadBoxVisibility(false);
        if (inReadList(info))
            return;

        DeletePermissionHandler handler = null;
        if (canEdit) {
            handler = new DeletePermissionHandler(service, eventBus, info, entryId, new DeletePermissionCallback());
        }

        boolean isGroup = (info.getType() == PermissionType.READ_GROUP || info.getType() == PermissionType.WRITE_GROUP);
        boolean isWrite = (info.getType() == PermissionType.WRITE_GROUP || info
                .getType() == PermissionType.WRITE_ACCOUNT);
        PermissionItem item = new PermissionItem(info.getId(), info.getDisplay(), isGroup, isWrite);
        view.addReadItem(item, handler);
        readList.add(info);
    }

    public void addWriteItem(PermissionInfo info, RegistryServiceAsync service, HandlerManager eventBus, long entryId,
            boolean canEdit) {

        view.setWriteBoxVisibility(false);
        addReadItem(info, service, eventBus, entryId, canEdit);

        if (inWriteList(info))
            return;

        DeletePermissionHandler handler = null;
        if (canEdit) {
            handler = new DeletePermissionHandler(service, eventBus, info, entryId, new DeletePermissionCallback());
        }

        boolean isGroup = (info.getType() == PermissionType.READ_GROUP || info.getType() == PermissionType.WRITE_GROUP);
        boolean isWrite = (info.getType() == PermissionType.WRITE_GROUP || info
                .getType() == PermissionType.WRITE_ACCOUNT);
        PermissionItem item = new PermissionItem(info.getId(), info.getDisplay(), isGroup, isWrite);
        view.addWriteItem(item, handler);
        writeList.add(info);
    }

    private boolean inWriteList(PermissionInfo info) {
        for (PermissionInfo permission : writeList) {
            if (info.getId() == permission.getId() && info.getType() == permission.getType())
                return true;
        }
        return false;
    }

    private boolean inReadList(PermissionInfo info) {
        for (PermissionInfo permission : readList) {
            if (info.getId() == permission.getId() && info.getType() == permission.getType())
                return true;
        }
        return false;
    }

    public void setPermissionData(ArrayList<PermissionInfo> infoList, RegistryServiceAsync service,
            HandlerManager eventBus, long entryId) {
        if (infoList == null)
            return;

        view.resetPermissionDisplay();

        ArrayList<PermissionItem> itemList = new ArrayList<PermissionItem>();

        for (PermissionInfo info : infoList) {
            PermissionItem item = null;
            DeletePermissionHandler handler = null;
            if (isCanEdit())
                handler = new DeletePermissionHandler(service, eventBus, info, entryId, new DeletePermissionCallback());

            switch (info.getType()) {
                case READ_ACCOUNT:
                    item = new PermissionItem(info.getId(), info.getDisplay(), false, false);
                    view.addReadItem(item, handler);
                    readList.add(info);
                    break;

                case READ_GROUP:
                    item = new PermissionItem(info.getId(), info.getDisplay(), true, false);
                    view.addReadItem(item, handler);
                    readList.add(info);
                    break;

                case WRITE_ACCOUNT:
                    item = new PermissionItem(info.getId(), info.getDisplay(), false, true);
                    view.addWriteItem(item, handler);
                    writeList.add(info);
                    break;

                case WRITE_GROUP:
                    item = new PermissionItem(info.getId(), info.getDisplay(), true, true);
                    view.addWriteItem(item, handler);
                    writeList.add(info);
                    break;
            }

            if (item != null)
                itemList.add(item);
        }
    }

    public void setVisible(boolean visible) {
        this.view.setWidgetVisibility(visible);
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        this.view.setWidgetVisibility(canEdit);
    }

    //
    // Inner classes
    //
    private class DeletePermissionCallback extends Callback<PermissionInfo> {

        @Override
        public void onSuccess(PermissionInfo info) {

            PermissionItem item = null;

            switch (info.getType()) {
                case READ_ACCOUNT:
                    item = new PermissionItem(info.getId(), info.getDisplay(), false, false);
                    view.removeReadItem(item);
                    removeFromList(readList, info);
                    break;

                case READ_GROUP:
                    item = new PermissionItem(info.getId(), info.getDisplay(), true, false);
                    view.removeReadItem(item);
                    removeFromList(readList, info);
                    break;

                case WRITE_ACCOUNT:
                    item = new PermissionItem(info.getId(), info.getDisplay(), false, true);
                    view.removeWriteItem(item);
                    removeFromList(writeList, info);
                    break;

                case WRITE_GROUP:
                    item = new PermissionItem(info.getId(), info.getDisplay(), true, true);
                    view.removeWriteItem(item);
                    removeFromList(writeList, info);
                    break;
            }
        }

        private void removeFromList(ArrayList<PermissionInfo> list, PermissionInfo info) {
            if (list == null || list.isEmpty() || info == null)
                return;

            Iterator<PermissionInfo> iter = list.iterator();
            while (iter.hasNext()) {
                PermissionInfo next = iter.next();
                if (next.getId() == info.getId() && next.getType() == info.getType())
                    iter.remove();
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
