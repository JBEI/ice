package org.jbei.ice.client.entry.view.view;

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

        void addReadItem(PermissionItem item);

        void addWriteItem(PermissionItem item);
    }

    private final IPermissionsView view;

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

    public void addReadItem(PermissionInfo info) {
        boolean isGroup = (info.getType() == PermissionType.READ_GROUP || info.getType() == PermissionType.WRITE_GROUP);
        boolean isWrite = (info.getType() == PermissionType.WRITE_GROUP || info.getType() == PermissionType.WRITE_ACCOUNT);
        PermissionItem item = new PermissionItem(info.getId(), info.getDisplay(), isGroup, isWrite);
        view.addReadItem(item);
        view.setReadBoxVisibility(false);
    }

    public void addWriteItem(PermissionInfo info) {
        boolean isGroup = (info.getType() == PermissionType.READ_GROUP || info.getType() == PermissionType.WRITE_GROUP);
        boolean isWrite = (info.getType() == PermissionType.WRITE_GROUP || info.getType() == PermissionType.WRITE_ACCOUNT);
        PermissionItem item = new PermissionItem(info.getId(), info.getDisplay(), isGroup, isWrite);
        view.addWriteItem(item);
        view.setWriteBoxVisibility(false);
        addReadItem(info);
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
