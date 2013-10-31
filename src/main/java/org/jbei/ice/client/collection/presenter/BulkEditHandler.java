package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEvent;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Click Handler for bulk edit
 *
 * @author Hector Plahar
 */
public class BulkEditHandler implements ClickHandler {

    private final IHasEntryId dataTable;
    private final HandlerManager eventBus;
    private final RegistryServiceAsync service;

    public BulkEditHandler(RegistryServiceAsync service, HandlerManager eventBus, IHasEntryId dataTable) {
        this.eventBus = eventBus;
        this.dataTable = dataTable;
        this.service = service;
    }

    @Override
    public void onClick(ClickEvent event) {
        if (dataTable == null || eventBus == null)
            return;

        new IceAsyncCallback<BulkUploadInfo>() {

            @Override
            protected void callService(AsyncCallback<BulkUploadInfo> callback) throws AuthenticationException {
                service.getBulkEditData(ClientController.sessionId,
                                        new ArrayList<Long>(dataTable.getSelectedEntrySet()), callback);
            }

            @Override
            public void onSuccess(BulkUploadInfo result) {
                ArrayList<BulkUploadInfo> data = new ArrayList<BulkUploadInfo>();
                data.add(result);
                eventBus.fireEvent(new SavedDraftsEvent(data));
            }
        }.go(eventBus);
    }
}
