package org.jbei.ice.client.entry.view;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeletePermissionHandler implements ClickHandler {

    private PermissionInfo info;
    private final RegistryServiceAsync service;
    private final long entryId;
    private Callback<PermissionInfo> callback;
    private final HandlerManager eventBus;

    public DeletePermissionHandler(RegistryServiceAsync service, HandlerManager eventBus, PermissionInfo info,
            long entryId, Callback<PermissionInfo> callback) {
        this.info = info;
        this.service = service;
        this.eventBus = eventBus;
        this.entryId = entryId;
        this.callback = callback;
    }

    @Override
    public void onClick(ClickEvent event) {

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.removePermission(AppController.sessionId, entryId, info, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                if (callback != null && result)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Throwable caught) {
                if (callback != null)
                    callback.onFailure();
            }
        }.go(eventBus);
    }
}
