package org.jbei.ice.client.bulkupload.events;

import com.google.gwt.event.shared.GwtEvent;

public class BulkUploadSubmitEvent extends GwtEvent<BulkUploadSubmitEventHandler> {

    public static final Type<BulkUploadSubmitEventHandler> TYPE = new Type<BulkUploadSubmitEventHandler>();
    private final boolean success;

    public BulkUploadSubmitEvent(boolean success) {
        this.success = success;
    }

    @Override
    public GwtEvent.Type<BulkUploadSubmitEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(BulkUploadSubmitEventHandler handler) {
        handler.onSubmit(this);
    }

    public boolean isSuccess() {
        return success;
    }
}
