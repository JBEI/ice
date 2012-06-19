package org.jbei.ice.client.entry.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

public class DeletePermissionHandler implements ClickHandler {

    private PermissionInfo info;
    private final RegistryServiceAsync service;
    private final long entryId;
    private Callback<PermissionInfo> callback;

    public DeletePermissionHandler(RegistryServiceAsync service, PermissionInfo info, long entryId) {
        this.info = info;
        this.service = service;
        this.entryId = entryId;
    }

    public DeletePermissionHandler(RegistryServiceAsync service, PermissionInfo info, long entryId,
            Callback<PermissionInfo> callback) {
        this(service, info, entryId);
        this.callback = callback;
    }

    public void setPermissionInfo(PermissionInfo info) {
        this.info = info;
    }

    public Callback<PermissionInfo> getCallback() {
        return this.callback;
    }

    public void setCallback(Callback<PermissionInfo> callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(ClickEvent event) {
        try {
            service.removePermission(AppController.sessionId, entryId, info,
                                     new AsyncCallback<Boolean>() {

                                         @Override
                                         public void onSuccess(Boolean result) {
                                             if (callback != null)
                                                 callback.onSuccess(info);
                                         }

                                         @Override
                                         public void onFailure(Throwable caught) {
                                             if (callback != null)
                                                 callback.onFailure();
                                         }
                                     });
        } catch (org.jbei.ice.client.exception.AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
