package org.jbei.ice.client.bulkimport.events;

import com.google.gwt.event.shared.EventHandler;

public interface BulkImportSubmitEventHandler extends EventHandler {
    void onSubmit(BulkImportSubmitEvent event);
}
