package org.jbei.ice.client.admin.bulkimport;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

public class DeleteBulkImportHandler implements IDeleteMenuHandler {

    private final RegistryServiceAsync service;

    public DeleteBulkImportHandler(RegistryServiceAsync service) {
        this.service = service;
    }

    @Override
    public void delete(long draftId, final Callback<BulkImportMenuItem> deleteCallback) {

        try {
            service.deleteDraftPendingVerification(AppController.sessionId, draftId,
                                                   new AsyncCallback<BulkImportDraftInfo>() {

                                                       @Override
                                                       public void onSuccess(BulkImportDraftInfo result) {
                                                           if (result == null) {
                                                               deleteCallback.onFailure();
                                                               return;
                                                           }
                                                           String name = result.getName();
                                                           String dateTime = DateUtilities.formatShorterDate(
                                                                   result.getCreated());
                                                           BulkImportMenuItem item = new BulkImportMenuItem(
                                                                   result.getId(), name, result
                                                                   .getCount(), dateTime, result.getType().toString(),
                                                                   result.getAccount()
                                                                         .getEmail());
                                                           deleteCallback.onSuccess(item);
                                                       }

                                                       @Override
                                                       public void onFailure(Throwable caught) {
                                                           deleteCallback.onFailure();
                                                       }
                                                   });
        } catch (org.jbei.ice.client.exception.AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}