package org.jbei.ice.client.bulkimport.events;

import com.google.gwt.event.shared.GwtEvent;

public class BulkImportSubmitEvent extends GwtEvent<BulkImportSubmitEventHandler> {

    public static Type<BulkImportSubmitEventHandler> TYPE = new Type<BulkImportSubmitEventHandler>();
    private final boolean success;

    public BulkImportSubmitEvent(boolean success) {
        this.success = success;
    }

    @Override
    public GwtEvent.Type<BulkImportSubmitEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(BulkImportSubmitEventHandler handler) {
        handler.onSubmit(this);
    }

    public boolean isSuccess() {
        return success;
    }
}
