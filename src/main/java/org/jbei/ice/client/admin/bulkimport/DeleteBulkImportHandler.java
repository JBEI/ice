package org.jbei.ice.client.admin.bulkimport;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

public class DeleteBulkImportHandler implements IDeleteMenuHandler {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public DeleteBulkImportHandler(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    public void delete(final long draftId, final Callback<BulkImportMenuItem> deleteCallback) {

        new IceAsyncCallback<BulkImportDraftInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkImportDraftInfo> callback) {
                try {
                    service.deleteDraftPendingVerification(AppController.sessionId, draftId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(BulkImportDraftInfo result) {
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