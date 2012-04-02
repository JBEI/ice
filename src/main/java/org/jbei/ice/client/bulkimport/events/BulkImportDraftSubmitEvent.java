package org.jbei.ice.client.bulkimport.events;

import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent.BulkImportDraftSubmitEventHandler;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class BulkImportDraftSubmitEvent extends GwtEvent<BulkImportDraftSubmitEventHandler> {

    public static Type<BulkImportDraftSubmitEventHandler> TYPE = new Type<BulkImportDraftSubmitEventHandler>();
    private final BulkImportDraftInfo info;

    public BulkImportDraftSubmitEvent(BulkImportDraftInfo info) {
        this.info = info;
    }

    @Override
    public GwtEvent.Type<BulkImportDraftSubmitEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(BulkImportDraftSubmitEventHandler handler) {
        handler.onSubmit(this);
    }

    public BulkImportDraftInfo getDraftInfo() {
        return this.info;
    }

    public interface BulkImportDraftSubmitEventHandler extends EventHandler {
        void onSubmit(BulkImportDraftSubmitEvent event);
    }
}
