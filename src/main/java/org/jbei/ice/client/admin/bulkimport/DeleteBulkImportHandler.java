package org.jbei.ice.client.admin.bulkimport;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeleteBulkImportHandler implements IDeleteMenuHandler {

    private final RegistryServiceAsync service;

    public DeleteBulkImportHandler(RegistryServiceAsync service) {
        this.service = service;
    }

    @Override
    public void delete(long draftId, final Callback<BulkImportMenuItem> deleteCallback) {

        service.deleteDraftPendingVerification(AppController.sessionId, draftId,
            new AsyncCallback<BulkImportDraftInfo>() {

                @Override
                public void onSuccess(BulkImportDraftInfo result) {
                    if (result == null) {
                        deleteCallback.onFailure();
                        return;
                    }
                    String name = result.getName();
                    String dateTime = DateUtilities.formatShorterDate(result.getCreated());
                    BulkImportMenuItem item = new BulkImportMenuItem(result.getId(), name, result
                            .getCount(), dateTime, result.getType().toString(), result.getAccount()
                            .getEmail());
                    deleteCallback.onSucess(item);
                }

                @Override
                public void onFailure(Throwable caught) {
                    deleteCallback.onFailure();
                }
            });
    }
}