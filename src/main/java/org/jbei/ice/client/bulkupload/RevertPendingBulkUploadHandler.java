package org.jbei.ice.client.bulkupload;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class RevertPendingBulkUploadHandler implements IRevertBulkUploadHandler {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public RevertPendingBulkUploadHandler(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    public void revert(final long draftId, final Callback<Long> revertCallback) {
        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.revertedSubmittedBulkUpload(ClientController.sessionId, draftId, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                if (result == null) {
                    revertCallback.onFailure();
                    return;
                }

                revertCallback.onSuccess(draftId);
            }
        }.go(eventBus);
    }
}
