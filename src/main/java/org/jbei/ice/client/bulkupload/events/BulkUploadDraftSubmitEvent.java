package org.jbei.ice.client.bulkupload.events;

import org.jbei.ice.shared.dto.BulkUploadInfo;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class BulkUploadDraftSubmitEvent extends GwtEvent<BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler> {

    public static Type<BulkUploadDraftSubmitEventHandler> TYPE = new Type<BulkUploadDraftSubmitEventHandler>();
    private final BulkUploadInfo info;

    public BulkUploadDraftSubmitEvent(BulkUploadInfo info) {
        this.info = info;
    }

    @Override
    public GwtEvent.Type<BulkUploadDraftSubmitEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(BulkUploadDraftSubmitEventHandler handler) {
        handler.onSubmit(this);
    }

    public BulkUploadInfo getDraftInfo() {
        return this.info;
    }

    public interface BulkUploadDraftSubmitEventHandler extends EventHandler {
        void onSubmit(BulkUploadDraftSubmitEvent event);
    }
}
