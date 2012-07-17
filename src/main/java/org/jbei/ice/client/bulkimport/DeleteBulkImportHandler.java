package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeleteBulkImportHandler implements IDeleteMenuHandler {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public DeleteBulkImportHandler(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    public void delete(final long draftId, final Callback<BulkImportMenuItem> deleteCallback) {

        new IceAsyncCallback<BulkImportInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkImportInfo> callback) {
                try {
                    service.deleteDraftPendingVerification(AppController.sessionId, draftId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkImportInfo result) {
                if (result == null) {
                    deleteCallback.onFailure();
                    return;
                }
                String name = result.getName();
                String dateTime = DateUtilities.formatShorterDate(result.getCreated());
                BulkImportMenuItem item = new BulkImportMenuItem(
                        result.getId(),
                        name,
                        result.getCount(),
                        dateTime,
                        result.getType().toString(),
                        result.getAccount().getEmail());
                deleteCallback.onSuccess(item);
            }
        }.go(eventBus);
    }
}