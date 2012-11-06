package org.jbei.ice.client.bulkupload.events;

import com.google.gwt.event.shared.EventHandler;

public interface BulkUploadSubmitEventHandler extends EventHandler {
    void onSubmit(BulkUploadSubmitEvent event);
}
